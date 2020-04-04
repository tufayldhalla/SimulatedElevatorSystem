package elevatorSystem.scheduler.states;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;
import elevatorSystem.scheduler.communication.SendToElevator;
import elevatorSystem.scheduler.helpers.ElevatorStatus;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.PickUp;
import elevatorSystem.scheduler.helpers.RequestData;
import elevatorSystem.scheduler.helpers.VariableChangedCode;

/**
 * Process to SelectElevator to service requests
 *
 * @author L4G3
 * @version 2.0
 */
public class SelectElevator extends Observable implements Runnable {

	/**
	 * Queue of requests to sort within this state
	 */
	private Queue<PickUp> requests;

	/**
	 * List of working elevators (elevators that are not out of order)
	 */
	private ArrayList<ElevatorStatus> workingElevators;

	/**
	 * Reference to the scheduler subsystem
	 */
	private Scheduler scheduler;

	/**
	 * Reference to elevatorSubsystems' senders
	 */
	private SendToElevator[] senders;

	/**
	 * Start times of potential measurements
	 */
	private Long startTime;
	
	/**
	 * Reference to GUI
	 */
	private GUIApplication gui = null;

	/**
	 * Outgoing header to indicate start for Start Elevator process
	 */
	private static final byte[] START_ELEVATOR_HEADER = Header.START_ELEVATOR.getHeader();

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * Constructor for the select elevator 'process'. Obtains references to the
	 * scheduler
	 */
	public SelectElevator(Scheduler s) {
		//super("Service Request");
		scheduler = s;
		gui = s.getGUI();
		if(gui!=null) {
			this.addObserver(gui);
		}
	}

	/**
	 * Selects elevators to service each request and notifies elevators to start
	 */
	public void run() {
		senders = scheduler.getElevatorSenders();
		workingElevators = scheduler.getWorkingElevators();
		requests = new LinkedList<PickUp>();
		startTime = System.nanoTime();

		/*
		 * Measure from start time. Start time is here because it is decided that a car
		 * will start if not yet already started, it just depends on which car
		 */

		/*
		 * Move the buffer of pending requests here to sort. Also adds the starting time
		 * of this request to the calculator list
		 */
		synchronized (scheduler.getPendingRequests()) {
			while (!scheduler.getPendingRequests().isEmpty()) {
				PickUp req = scheduler.getPendingRequests().remove();
				scheduler.notifyDequeue();	// for GUI
				requests.add(req);
			}
		}
		/*
		 * Algorithm to choose elevator to service requests and add to each elevator car
		 * list of floors to visit
		 */
		chooseElevator();

		/*
		 * Print to console the floors to visit of all elevators and to simulation file
		 */
		String s = scheduler.elevatorsToString();
		System.out.println(s);
		new Thread() {
			public void run() {
				try {
					scheduler.getOutput().simulate(s);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}.start();
		/*
		 * Start all the elevators that contain requests
		 */
		startElevators();
	}

	/**
	 * Algorithm to choose elevator to service all requests
	 */
	private void chooseElevator() {
		/*
		 * CASE 1: Check if any of the requests come from floors with a car idle at
		 * those floors
		 */
		Iterator<PickUp> itr = requests.iterator();
		while (itr.hasNext()) {
			PickUp currentRequest = itr.next();
			if (checkIdle(currentRequest)) {
				itr.remove();
			}
		}

		/*
		 * Loop through the rest of the requests if there are more to deal with
		 */
		while (!requests.isEmpty()) {
			PickUp currentRequest = requests.remove();
			/*
			 * CASE 2: check for min distance from car going towards this floor. If this
			 * case fails, check next case
			 */
			if (!checkDistance(currentRequest)) {
				/*
				 * CASE 3: all cars are going away from this floor. Put this request back in the
				 * pending queue to be placed later
				 */
				scheduler.enqueuePendingRequests(currentRequest);
			}
		}

	}

	/**
	 * Case 1: Checks if there exists a working elevator idle on the floor of the
	 * pickup request. Note that if exists, sets direction to no longer idle.
	 * 
	 * @param r is the request being placed
	 * @return if successfully placed in this case or not
	 */
	private boolean checkIdle(PickUp r) {
		int floorOfRequest = r.getFloorToService();

		/*
		 * Check each elevator to see if idle and if floor matches the floor of the
		 * request
		 */
		for (ElevatorStatus e : workingElevators) {
			synchronized (e) {
				if (e.getElevatorDirection() == StringData.IDLE && e.getElevatorFloor() == floorOfRequest) {
					addRequest(e,e.getFloorsToVisit(), r);
					e.setElevatorDirection(r.getDirectionTraveling());
					e.setElevatorStarted(false);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Case 2: Checks if there exists an elevator going towards this pickup request
	 * 
	 * @param r is the request being placed
	 * @return if successfully placed in this case or not
	 */
	private boolean checkDistance(PickUp r) {
		ArrayList<ElevatorStatus> options = findOptions(r);
		int floorOfRequest = r.getFloorToService();

		/*
		 * If there exists at least one option
		 */
		if (!options.isEmpty()) {
			ElevatorStatus min;
			int minDistance;
			synchronized (options.get(0)) {
				min = options.get(0);
				minDistance = Math.abs(floorOfRequest - min.getElevatorFloor());
			}

			/*
			 * Check each elevator floor to request floor distance to see if its smaller
			 * than current min distance
			 */
			for (int i = 1; i < options.size(); i++) {
				synchronized (options.get(i)) {
					ElevatorStatus e = options.get(i);
					int distance = Math.abs(floorOfRequest - e.getElevatorFloor());
					if (distance < minDistance) {
						min = e;
					}
				}
			}

			/*
			 * Add request to elevator's list. Check to make sure its still the minimum
			 * floor
			 */
			synchronized (min) {
				if (min.getElevatorDirection() == StringData.IDLE) {
					addRequest(min, min.getFloorsToVisit(), r);
					if (min.getElevatorFloor() < r.getFloorToService()) {
						min.setElevatorDirection(StringData.UP);
					} else if (min.getElevatorFloor() > r.getFloorToService()) {
						min.setElevatorDirection(StringData.DOWN);
					}
					return true;
				} else if (r.getDirectionTraveling() == StringData.UP && min.getElevatorFloor() <= floorOfRequest) {
					addRequest(min, min.getFloorsToVisit(), r);
					return true;
				} else if (r.getDirectionTraveling() == StringData.DOWN && min.getElevatorFloor() >= floorOfRequest) {
					addRequest(min, min.getFloorsToVisit(), r);
					return true;
				}

			}

		}
		/*
		 * No options exist or failed to add...
		 */

		return false;

	}

	/**
	 * Adds the current request to the elevator's floors to visit list, in order
	 * 
	 * @param floorsToVisit is the floors to visit list for the elevator chosen
	 * @param r             is the request to place in the list
	 */
	private void addRequest(ElevatorStatus elevator, ArrayList<RequestData> floorsToVisit, RequestData request) {
		StringData direction = request.getDirectionTraveling();
		int floorOfRequest = request.getFloorToService();

		/*
		 * List not empty: order matters
		 */
		if (!floorsToVisit.isEmpty()) {
			// save direction of first request in list
			StringData firstDirection = floorsToVisit.get(0).getDirectionTraveling();
			Iterator<RequestData> itr = floorsToVisit.iterator(); // iterator for iterating through list

			/*
			 * Iterates through the floors to visit list of the elevator and places in order
			 */
			while (itr.hasNext()) {
				RequestData compare = itr.next(); // obtain request at this index

				/*
				 * Check if direction of request in the floors to visit list matches the
				 * direction of the request to place.
				 */
				if (compare.getDirectionTraveling() == direction) {
					/*
					 * If direction is up & the request to place is less than or equal to the
					 * request in the floors to visit list, then add in that place
					 */
					if (direction == StringData.UP && floorOfRequest <= compare.getFloorToService()) {
						int index = floorsToVisit.indexOf(compare);
						floorsToVisit.add(index, request);
						return;
					}
					/*
					 * If direction is down & the request to place is greater than or equal to the
					 * request in the floors to visit list, then add in that place
					 */
					else if (direction == StringData.DOWN && floorOfRequest >= compare.getFloorToService()) {
						int index = floorsToVisit.indexOf(compare);
						floorsToVisit.add(index, request);
						return;
					}
				}

				/*
				 * If the request was not yet inserted yet, but the current request has the
				 * opposite direction of the one it started with, add it here. (For example, say
				 * the request is going up but this method has already iterated through all the
				 * requests going up without finding one less than the floors of the requests
				 * currently going up then add before the first request going down)
				 */
				if (firstDirection != compare.getDirectionTraveling()) {
					int index = floorsToVisit.indexOf(compare);
					floorsToVisit.add(index, request);
					return;
				}
			}
		}

		/*
		 * If list empty, does not matter where to add. Or, if it made it here, that
		 * means it could not be placed within the list and is to be enqueued to the end
		 */
		floorsToVisit.add(request);
		if (gui != null) {
			setChanged();
			notifyObservers(new GUIPacket(elevator, VariableChangedCode.ELEVATOR_FLOORSTOVISIT));
		}
	}

	/**
	 * Finds the possible elevator options for this request
	 * 
	 * @param r is the request to find options for
	 * @return list of elevators that are options
	 */
	private ArrayList<ElevatorStatus> findOptions(PickUp r) {
		StringData desiredDirection = r.getDirectionTraveling();
		int floorOfRequest = r.getFloorToService();

		ArrayList<ElevatorStatus> options = new ArrayList<ElevatorStatus>(); // list to save options

		/*
		 * For each working elevator, check if the direction is idle or matches the
		 * direction of the request and then check if the car has not yet passed the
		 * floor
		 */
		for (ElevatorStatus e : workingElevators) {
			synchronized (e) {
				StringData elevatorDirection = e.getElevatorDirection();
				int elevatorFloor = e.getElevatorFloor();

				/*
				 * If elevator is idle, it is an option
				 */
				if (elevatorDirection == StringData.IDLE) {
					options.add(e);
				}
				/*
				 * If elevator direction matches the direction of the request, move on to next
				 * check
				 */
				else if (elevatorDirection == desiredDirection) {
					/*
					 * If the request's direction is up, check that the car has not yet passed this
					 * floor (check that the elevator car floor is less than the request's pick up
					 * floor). Then it is an option
					 */
					if (desiredDirection == StringData.UP && !e.getElevatorStarted()) {
						if (elevatorFloor < floorOfRequest) {
							options.add(e);
						} else if (elevatorFloor == floorOfRequest && !e.getElevatorStarted()) {
							options.add(e);
						}
					}

					/*
					 * If the request's direction is down, check that the car has not yet passed
					 * this floor (check that the elevator car floor is greater than the request's
					 * pick up floor). Then it is an option
					 */
					else if (desiredDirection == StringData.DOWN) {
						if (elevatorFloor > floorOfRequest) {
							options.add(e);
						}
					}

				}
			}
		}
		return options;
	}

	/**
	 * starts the elevators that have floors to visit
	 */
	private void startElevators() {
		for (ElevatorStatus elevator : workingElevators) {
			boolean start = true;
			synchronized (elevator) {
				if (elevator.getFloorsToVisit().isEmpty()) {
					elevator.setElevatorStarted(true);
					start = false;
				}
			}
			if (start) {
				// START_ELEVATOR_HEADER, elevatorID, 0, dummy fault (-1), 0
				byte[] newData = ("" + elevator.getID()).getBytes();
				byte[] dummy = ("" + 1).getBytes();
				int index = 0;

				byte[] msg = new byte[newData.length + START_ELEVATOR_HEADER.length + dummy.length + ZERO_PARSER.length];
				System.arraycopy(START_ELEVATOR_HEADER, 0, msg, index, START_ELEVATOR_HEADER.length);
				index += START_ELEVATOR_HEADER.length;
				System.arraycopy(newData, 0, msg, index, newData.length);
				index += newData.length;
				System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
				index += ZERO_PARSER.length;
				System.arraycopy(dummy, 0, msg, index, dummy.length);
				senders[elevator.getID() - 1].send(msg);

				/*
				 * Measure end time and add measurement. This will show how long it takes for
				 * the scheduler to tell the elevator subsystem to start its motor, turn on
				 * button, start the arrival sensor
				 */
				Long endTime = System.nanoTime();
				scheduler.getArrivalSensorsInterfaceCalc().addMeasurement((endTime - startTime) / 1000000F);

			}
			/*
			 * Sleep to allow for previous request to be sent over UDP
			 */
			try {
				Thread.sleep(IntData.SEND_TIME.getTime(scheduler.getSpeed()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
