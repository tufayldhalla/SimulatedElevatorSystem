package elevatorSystem.scheduler.states;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;
import elevatorSystem.scheduler.communication.SendToElevator;
import elevatorSystem.scheduler.communication.SendToFloor;
import elevatorSystem.scheduler.helpers.ElevatorStatus;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.RequestData;
import elevatorSystem.scheduler.helpers.VariableChangedCode;

/**
 * Process to deal with when an elevator arrives at a floor
 *
 * @author L4G3
 * @version 2.0
 */
public class ArrivedAtFloor extends Observable implements Runnable {

	/**
	 * Reference to the elevator statuses
	 */
	private ElevatorStatus[] elevators;

	/**
	 * direction of first removed request
	 */
	private StringData direction;

	/**
	 * Reference to the floorSender
	 */
	private SendToFloor floorSender;

	/**
	 * Reference to the elevatorSender
	 */
	private SendToElevator[] elevatorSenders;

	/**
	 * To alert the Elevator to start or not
	 */
	private String continueStatus;

	/**
	 * List of destinations to simulate to the elevator subsystem
	 */
	private ArrayList<Integer> destinations;

	/**
	 * List of keys to access stop timing pickup to drop off
	 */
	private ArrayList<Integer> endTimeKeys;

	/**
	 * List of keys to pass on to elevator subsystem
	 */
	private ArrayList<Integer> keys;

	/**
	 * Time elevator stopped
	 */
	private long endTime;

	/**
	 * List of faults to simulate to the elevator subsystem
	 */
	private ArrayList<Integer> faults;

	/**
	 * Current floor of elevator that arrived
	 */
	private ElevatorStatus currentElevator;

	/**
	 * Floor of elevator upon arrival
	 */
	private int elevatorFloor;
	
	/**
	 * Reference to GUI
	 */
	private GUIApplication gui = null;

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * data received from dispatcher to be decoded
	 */
	private byte[] data;

	/**
	 * id of elevator that arrived
	 */
	private int elevatorID;

	/**
	 * Reference to the Scheduler subsystem
	 */
	private Scheduler schedulerSubsystem;

	/**
	 * Outgoing header to indicate start for Stop Elevator process
	 */
	private static final byte[] STOP_ELEVATOR_HEADER = Header.STOP_ELEVATOR.getHeader();

	/**
	 * Outgoing header to indicate start for inform Elevator of car button press
	 */
	private static final byte[] DEST_SELECTED_HEADER = Header.DESTINATION_SELECTED.getHeader();

	/**
	 * Outgoing header to indicate start for Stop Elevator process
	 */
	private static final byte[] ELEVATOR_ARRIVED_HEADER = Header.ELEVATOR_ARRIVED.getHeader();

	/**
	 * Start time of potential measurement
	 */
	private Long startTime;

	/**
	 * Creates new ArrivedAtFloor process
	 * 
	 * @param s   is a reference to the Scheduler subsystem
	 * @param msg is the data passed from the dispatcher to be decoded
	 */
	public ArrivedAtFloor(Scheduler s, byte[] msg) {
		//super("Arrived At Floor");
		schedulerSubsystem = s;
		gui = s.getGUI();
		if(gui!=null) {
			this.addObserver(gui);
		}
		data = msg;
	}

	/**
	 * Decode data and figure out if elevator should stop here
	 */
	public void run() {
		this.elevators = schedulerSubsystem.getElevators();
		elevatorSenders = schedulerSubsystem.getElevatorSenders();
		destinations = new ArrayList<Integer>();
		endTimeKeys = new ArrayList<Integer>();
		keys = new ArrayList<Integer>();
		faults = new ArrayList<Integer>();
		direction = StringData.IDLE;

		decodeData(); // decode data
		/*
		 * Measure start time (decision to stop here has been made)
		 */
		startTime = System.nanoTime();
		floorSender = schedulerSubsystem.getFloorSender(elevatorID);

		synchronized (elevators[elevatorID - 1]) {
			currentElevator = elevators[elevatorID - 1];
			elevatorFloor = elevators[elevatorID - 1].getElevatorFloor();
			/*
			 * Output for simulating on console and to output file
			 */
			String s = "Scheduler: Elevator #ID " + elevatorID + " - arrived at floor " + elevatorFloor
					+ "... check if stop";
			System.out.println(s);
			new Thread() {
				public void run() {
					try {
						schedulerSubsystem.getOutput().write("\n" + s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			/*
			 * Elevator has no more requests, stop it
			 */
			if (currentElevator.getFloorsToVisit().isEmpty()) {
				startTime = System.nanoTime();
				continueStatus = StringData.DONE.getString();
				stopElevator();
				currentElevator.setElevatorStarted(false);
				currentElevator.setElevatorDirection(StringData.IDLE);
				return;
			}
			/*
			 * If car should stop at this floor, notify the floor subsystem and tell the
			 * elevator subsystem to stop the car
			 */
			if (stopOrNot()) {

				/*
				 * Output for simulating on console and to output file
				 */
				String s2 = "Scheduler: Elevator #ID " + elevatorID + " - stopped at floor "
						+ currentElevator.getElevatorFloor();
				System.out.println(s2);
				new Thread() {
					public void run() {
						try {
							schedulerSubsystem.getOutput().write("\n" + s2);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.start();

				checkIfBeatOthers();

				if (!currentElevator.getFloorsToVisit().isEmpty()) {
					continueStatus = StringData.CONTINUE.getString();
					// If at bottom floor, reset direction
					if (currentElevator.getElevatorFloor() == 1) {
						currentElevator.setElevatorDirection(StringData.UP);
					}
					// if at top floor, reset direction
					else if (currentElevator.getElevatorFloor() == IntData.NUM_FLOORS.getNum()) {
						currentElevator.setElevatorDirection(StringData.DOWN);
					}
				} else {
					continueStatus = StringData.DONE.getString();
					if (destinations.isEmpty()) {
						currentElevator.setElevatorDirection(StringData.IDLE);
					} else {
						// If at bottom floor, reset direction
						if (currentElevator.getElevatorFloor() == 1) {
							currentElevator.setElevatorDirection(StringData.UP);
						}
						// if at top floor, reset direction
						else if (currentElevator.getElevatorFloor() == IntData.NUM_FLOORS.getNum()) {
							currentElevator.setElevatorDirection(StringData.DOWN);
						} else {
							/*
							 * No more requests in current direction. However, just picked someone up and
							 * now need to go in direction they wanted
							 */
							if (destinations.get(0) > currentElevator.getElevatorFloor()) {
								currentElevator.setElevatorDirection(StringData.UP);
							} else if (destinations.get(0) < currentElevator.getElevatorFloor()) {
								currentElevator.setElevatorDirection(StringData.DOWN);
							}
						}
					}
				}

				stopElevator();

				notifyFloor();

				/*
				 * Print to console the floors to visit of current elevator
				 */
				System.out.println("\n----------------------------------\n" + currentElevator.requestsToString()
						+ "\n----------------------------------\n");
			}

			else if (direction == StringData.UP || currentElevator.getElevatorDirection() == StringData.UP) {
				/*
				 * Print to console the floors to visit of current elevator
				 */
				System.out.println("\n----------------------------------\n" + currentElevator.requestsToString()
						+ "\n----------------------------------\n");
				/*
				 * Reset just in case
				 */
				if (currentElevator.getElevatorFloor() == IntData.NUM_FLOORS.getNum()) {
					currentElevator.setElevatorDirection(StringData.DOWN);
					currentElevator.decrementFloor();
				} else {
					currentElevator.incrementFloor();
				}
			} else if (direction == StringData.DOWN || currentElevator.getElevatorDirection() == StringData.DOWN) {
				/*
				 * Print to console the floors to visit of current elevator
				 */
				System.out.println("\n----------------------------------\n" + currentElevator.requestsToString()
						+ "\n----------------------------------\n");
				/*
				 * Reset just in case
				 */
				if (currentElevator.getElevatorFloor() == 1) {
					currentElevator.setElevatorDirection(StringData.UP);
					currentElevator.incrementFloor();
				} else {
					currentElevator.decrementFloor();
				}
			}
		}

		/*
		 * If the floor arrived at contains a pick up request, find out what the
		 * destination for all are and simulate to the elevator subsystem
		 */
		while (!destinations.isEmpty()) {
			simulateCarButton(destinations.remove(0), faults.remove(0), keys.remove(0));
			try {
				Thread.sleep(IntData.SEND_TIME.getTime(schedulerSubsystem.getSpeed()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		String str = schedulerSubsystem.elevatorsToString();

		new Thread() {
			public void run() {
				/*
				 * Output to simulation file to prove that it works
				 */
				try {
					schedulerSubsystem.getOutput().simulate(str);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}.start();
		startCalcs();
		signalDone();

	}

	/**
	 * Determines end time for all drop off requests finally serviced
	 */
	private void startCalcs() {
		for (Integer key : endTimeKeys) {
			schedulerSubsystem.getDropOffCalc().calculateTime(key, endTime);
		}
	}

	/**
	 * Decides if the car should stop or not
	 * 
	 * @return true if the car should stop
	 */
	private boolean stopOrNot() {
		boolean stop = false;
		ArrayList<RequestData> removed = new ArrayList<RequestData>();
		/*
		 * Check if next floor to stop at is this current floor. If so, stop is true.
		 */
		if (currentElevator.getFloorsToVisit().get(0).getFloorToService() == currentElevator.getElevatorFloor()) {
			stop = true;
			RequestData temp = currentElevator.getFloorsToVisit().remove(0);
			direction = temp.getDirectionTraveling();
			/*
			 * If the request serviced is a pickup request, save for later to deal with its
			 * destination
			 */
			if (temp.getType() == StringData.PICK_UP) {
				removed.add(temp);
			} else if (temp.getType() == StringData.DROP_OFF) {
				endTimeKeys.add(temp.getKey());
			}
		}

		/*
		 * Check if more requests at this removed request floor exist in this car.
		 * Should all be in order after the first request
		 */
		for (int i = 0; i < currentElevator.getFloorsToVisit().size(); i++) {
			RequestData req = currentElevator.getFloorsToVisit().get(i);
			if (req.getFloorToService() == currentElevator.getElevatorFloor()) {
				RequestData temp = currentElevator.getFloorsToVisit().remove(0);
				/*
				 * If the request serviced is a pickup request, save for later to deal with its
				 * destination
				 */
				if (temp.getType() == StringData.PICK_UP) {
					removed.add(temp);
				} else if (temp.getType() == StringData.DROP_OFF) {
					endTimeKeys.add(temp.getKey());
				}
			} else {
				break;
			}
		}

		for (RequestData req : removed) {
			destinations.add(req.getDestinationFloor());
			faults.add(req.getFault());
			keys.add(req.getKey());
		}

		if (gui != null) {
			setChanged();
			notifyObservers(new GUIPacket(currentElevator, VariableChangedCode.ELEVATOR_FLOORSTOVISIT));
		}
		
		return stop;
	}

	/**
	 * Checks if this car beat another car to service the pickup request or check if
	 * it beat a pending request
	 */
	private void checkIfBeatOthers() {
		/*
		 * Check other car's floors to visit list
		 */
		for (ElevatorStatus e : schedulerSubsystem.getWorkingElevators()) {
			if (!e.equals(currentElevator)) {
				synchronized (e) {
					ArrayList<RequestData> requestsBeaten = e.containsPickUp(currentElevator.getElevatorFloor(),
							currentElevator.getElevatorDirection());
					while (!requestsBeaten.isEmpty()) {
						destinations.add(requestsBeaten.get(0).getDestinationFloor());
						faults.add(requestsBeaten.get(0).getFault());
						keys.add(requestsBeaten.get(0).getKey());
						requestsBeaten.remove(0);
					}
				}
			}
		}
		/*
		 * Check pending request queue
		 */
		ArrayList<RequestData> requestsBeaten = schedulerSubsystem.containsPickUp(currentElevator.getElevatorFloor(),
				currentElevator.getElevatorDirection());
		while (!requestsBeaten.isEmpty()) {
			destinations.add(requestsBeaten.get(0).getDestinationFloor());
			faults.add(requestsBeaten.get(0).getFault());
			keys.add(requestsBeaten.get(0).getKey());
			requestsBeaten.remove(0);
		}
	}

	/**
	 * Tells the elevator what button is being pressed by the user (header,
	 * elevatorID, 0, destination, 0, fault, 0, key)
	 */
	private void simulateCarButton(int destination, int fault, int key) {
		byte[] faultData = ("" + fault).getBytes();
		byte[] idData = ("" + elevatorID).getBytes();
		byte[] destData = ("" + destination).getBytes();
		byte[] keyData = ("" + key).getBytes();
		int index = 0;
		byte[] msg = new byte[idData.length + destData.length + DEST_SELECTED_HEADER.length + 4 * ZERO_PARSER.length
				+ keyData.length];
		System.arraycopy(DEST_SELECTED_HEADER, 0, msg, index, DEST_SELECTED_HEADER.length);
		index += DEST_SELECTED_HEADER.length;
		System.arraycopy(idData, 0, msg, index, idData.length);
		index += idData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(destData, 0, msg, index, destData.length);
		index += destData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(faultData, 0, msg, index, faultData.length);
		index += faultData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(keyData, 0, msg, index, keyData.length);
		elevatorSenders[elevatorID - 1].send(msg);
	}

	/**
	 * Notifies the floor subsystem of the car arrival
	 */
	private void notifyFloor() {
		/*
		 * Packet that is being sent to the floorSubsystem. ElevatorArrived is expecting
		 * ELEVATOR_ARRIVED_HEADER, floorNumber, 0, direction, 0
		 */
		byte[] floorData = ("" + currentElevator.getElevatorFloor()).getBytes();
		byte[] dirStatus = currentElevator.getElevatorDirection().getString().getBytes();
		int index = 0;
		byte[] msg = new byte[floorData.length + ELEVATOR_ARRIVED_HEADER.length + dirStatus.length + ZERO_PARSER.length];
		System.arraycopy(ELEVATOR_ARRIVED_HEADER, 0, msg, index, ELEVATOR_ARRIVED_HEADER.length);
		index += ELEVATOR_ARRIVED_HEADER.length;
		System.arraycopy(floorData, 0, msg, index, floorData.length);
		index += floorData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(dirStatus, 0, msg, index, dirStatus.length);
		floorSender.send(msg);

		/*
		 * Measure end time for floor buttons interface. This will determine the time it
		 * takes for the scheduler to decide to stop and notify the floor subsystem to
		 * turn off/on lamps, etc.
		 */
		Long endTime = System.nanoTime();
		schedulerSubsystem.getFloorButtonsInterfaceCalc().addMeasurement((endTime - startTime) / 1000000F);

	}

	/**
	 * Notifies the elevator subsystem to stop
	 */
	private void stopElevator() {
		/*
		 * Packet that is being sent to elevator subsystem (StopElevator is expecting
		 * STOP_ELEVATOR_HEADER, elevatorID, 0, currentElevator number, 0,
		 * continueStatus (done/continue)
		 */
		byte[] idData = ("" + elevatorID).getBytes();
		byte[] elevData = ("" + currentElevator.getElevatorFloor()).getBytes();
		byte[] contData = continueStatus.getBytes();
		int index = 0;
		byte[] msg = new byte[idData.length + elevData.length + contData.length + STOP_ELEVATOR_HEADER.length
				+ 2 * ZERO_PARSER.length];
		System.arraycopy(STOP_ELEVATOR_HEADER, 0, msg, index, STOP_ELEVATOR_HEADER.length);
		index += STOP_ELEVATOR_HEADER.length;
		System.arraycopy(idData, 0, msg, index, idData.length);
		index += idData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(elevData, 0, msg, index, elevData.length);
		index += elevData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(contData, 0, msg, index, contData.length);
		elevatorSenders[elevatorID - 1].send(msg);

		/*
		 * Measure end time for elevator buttons interface. This will determine the time
		 * it takes for the scheduler to decide to stop and notify the elevator
		 * subsystem to stop motor, turn off buttons, etc.
		 */
		endTime = System.nanoTime();
		schedulerSubsystem.getElevatorButtonsInterfaceCalc().addMeasurement((endTime - startTime) / 1000000F);
	}

	/**
	 * Check and output if simulation is done. If so, output that and stop all
	 * classes and begin performance calculation
	 */
	private void signalDone() {
		int iteration = schedulerSubsystem.getIteration();
		boolean done = true;
		try {
			Thread.sleep(IntData.CHECK_DONE.getTime(schedulerSubsystem.getSpeed()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * Check that all working elevators have no more floors to visit
		 */
		ArrayList<ElevatorStatus> workingElevators = schedulerSubsystem.getWorkingElevators();
		for (ElevatorStatus elevator : workingElevators) {
			if (!elevator.getFloorsToVisit().isEmpty()) {
				done = false;
				break;
			}
		}
		if (done && !schedulerSubsystem.getSimDone() && iteration == schedulerSubsystem.getIteration()) {
			schedulerSubsystem.setSimDone(true);
			String s = schedulerSubsystem.elevatorsToString();
			new Thread() {
				public void run() {
					/*
					 * Print to console the floors to visit of all elevators
					 */
					System.out.println(s);
					/*
					 * Output to simulation file to prove that it works
					 */
					try {
						schedulerSubsystem.getOutput().simulate(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			performCalculations();
		}
	}

	/**
	 * Performs iteration 4's performance calculations
	 */
	private void performCalculations() {
		System.out.println("\nPerforming calculations....\n");
		System.out.println(schedulerSubsystem.getArrivalSensorsInterfaceCalc().calculationsToString());
		System.out.println(schedulerSubsystem.getElevatorButtonsInterfaceCalc().calculationsToString());
		System.out.println(schedulerSubsystem.getFloorButtonsInterfaceCalc().calculationsToString());
		System.out.println(schedulerSubsystem.getDropOffCalc().calculationsToString());
		System.out.println("\nCalculations done!\n");
	}

	/**
	 * Decodes data...Data received (or expected) is "elevatorID, 0"
	 */
	public void decodeData() {

		int len = 0;
		int index = TEMP_LENGTH;

		// Find out the floor number that request occurred at
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		elevatorID = Integer.valueOf(new String(data, index, len)); // Find elevator id

	}
}
