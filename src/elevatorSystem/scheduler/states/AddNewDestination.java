package elevatorSystem.scheduler.states;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;
import elevatorSystem.scheduler.communication.SendToElevator;
import elevatorSystem.scheduler.helpers.DropOff;
import elevatorSystem.scheduler.helpers.ElevatorStatus;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.RequestData;
import elevatorSystem.scheduler.helpers.VariableChangedCode;

/**
 * Process to Add New Destination to floorsToVisit
 *
 * @author L4G3
 * @version 2.0
 */
public class AddNewDestination extends Observable implements Runnable {

	/**
	 * Outgoing header to indicate start for Start Elevator process
	 */
	private static final byte[] START_ELEVATOR_HEADER = Header.START_ELEVATOR.getHeader();

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * New floor to add to the queue
	 */
	private int newFloor;

	/**
	 * New floor to add to the queue
	 */
	private int fault;
	
	/**
	 * Key of drop off request to service
	 */
	private int key;

	/**
	 * Elevator that request came from
	 */
	private int elevatorID;

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * Reference to the elevator's statuses
	 */
	private ElevatorStatus[] elevators;

	/**
	 * data to decode from dispatcher
	 */
	private byte[] data;

	/**
	 * Reference to the scheduler's senders to send to elevator subsystem
	 */
	private SendToElevator[] senders;

	/**
	 * Start time of potential measurement
	 */
	private Long startTime;

	/**
	 * Reference to scheduler subsystem
	 */
	private Scheduler scheduler;
	
	/**
	 * Reference to GUI
	 */
	private GUIApplication gui = null;

	/**
	 * Creates new addNewDestination
	 * 
	 * @param s   is a reference to the Scheduler subsystem
	 * @param msg is the data from dispatcher
	 */
	public AddNewDestination(Scheduler s, byte[] msg) {
		//super("Add New Destination");
		data = msg;
		scheduler = s;
		gui = s.getGUI();
		if(gui!=null) {
			this.addObserver(gui);
		}
	}

	/**
	 * Decodes data and adds destination to queue
	 */
	public void run() {
		elevators = scheduler.getElevators();
		senders = scheduler.getElevatorSenders();
		startTime = null;

		/*
		 * Decodes the data received from elevator subsystem
		 */
		decodeData();

		/*
		 * Add request to elevator's floors to visit list
		 */
		synchronized (elevators[elevatorID - 1]) {
			ElevatorStatus current = elevators[elevatorID - 1];
			StringData newDirection = current.getElevatorDirection();
			if (current.getElevatorDirection() == StringData.IDLE) {
				/*
				 * Measure from start time if not already moving
				 */
				startTime = System.nanoTime();
				if (newFloor > current.getElevatorFloor()) {
					newDirection = StringData.UP;
				} else if (newFloor < current.getElevatorFloor()) {
					newDirection = StringData.DOWN;
				}
			}
			addRequest(current, current.getFloorsToVisit(), new DropOff(newFloor, newDirection, key));
			current.setElevatorDirection(newDirection);
		}

		/*
		 * Starts the elevator if not already moving
		 */
		startElevator();
	}

	/**
	 * Adds the current request to the elevator's floors to visit list, in order
	 * 
	 * @param floorsToVisit is the floors to visit list for the elevator chosen
	 * @param r             is the request to place in the list
	 */
	private void addRequest(ElevatorStatus current, ArrayList<RequestData> floorsToVisit, DropOff request) {
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
			notifyObservers(new GUIPacket(current, VariableChangedCode.ELEVATOR_FLOORSTOVISIT));
		}
	}

	/**
	 * Start elevator of destination
	 */
	private void startElevator() {
		/*
		 * Send in format of START_ELEVATOR_HEADER, ElevatorID, 0, faultData, 0
		 */
		byte[] newData = ("" + elevatorID).getBytes();
		byte[] faultData = ("" + fault).getBytes();
		int index = 0;
		byte[] msg = new byte[newData.length + START_ELEVATOR_HEADER.length + 2 * ZERO_PARSER.length + faultData.length];
		System.arraycopy(START_ELEVATOR_HEADER, 0, msg, index, START_ELEVATOR_HEADER.length);
		index += START_ELEVATOR_HEADER.length;
		System.arraycopy(newData, 0, msg, index, newData.length);
		index += newData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(faultData, 0, msg, index, faultData.length);
		index += faultData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		senders[elevatorID - 1].send(msg);

		/*
		 * Measure end time and add measurement. This will show how long it takes for
		 * the scheduler to tell the elevator subsystem to start its motor, turn on
		 * button, start the arrival sensor
		 */
		if (startTime != null) {
			Long endTime = System.nanoTime();
			scheduler.getArrivalSensorsInterfaceCalc().addMeasurement((endTime - startTime) / 1000000F);
		}
	}

	/**
	 * Decodes the data received to retrieve floor number, direction and floor
	 * destination info. Receives data in format: "elevatorId, 0 ,floorNum, 0,
	 * fault, 0, key"
	 */
	private void decodeData() {
		int len = 0;
		int index = TEMP_LENGTH;

		// Find out the floor number that request occurred at
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		elevatorID = Integer.valueOf(new String(data, index, len)); // Find floor num

		// Find out destination that request wants
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		newFloor = Integer.valueOf(new String(data, index, len));

		// Find out the fault
		index += len + 1;
		len = 0; // needs to be 0 if uncomment for loop
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		fault = Integer.valueOf(new String(data, index, len));
		
		// Find out the fault
		index += len + 1;
		len = 0; // needs to be 0 if uncomment for loop
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		key = Integer.valueOf(new String(data, index, len));
	}
}