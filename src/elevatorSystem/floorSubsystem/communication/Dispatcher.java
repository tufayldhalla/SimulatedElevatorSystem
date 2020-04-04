package elevatorSystem.floorSubsystem.communication;

import java.util.Arrays;

import elevatorSystem.floorSubsystem.FloorSubsystem;
import elevatorSystem.floorSubsystem.states.ElevatorArrived;
import elevatorSystem.floorSubsystem.states.ServiceRequest;
import elevatorSystem.floorSubsystem.states.TransientFaultHandler;
import elevatorSystem.miscellaneous.Header;

/**
 * Dispatches requests received by Floor Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class Dispatcher extends Thread {

	/**
	 * Reference to the Floor Subsystem
	 */
	private FloorSubsystem floorSubsystem;

	/**
	 * Stores data received from SchedulerListener or InputFileReader
	 */
	private byte[] data;


	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();
	
	/**
	 * Car id that arrived
	 */
	private int ID;

	/**
	 * Incoming header to indicate start for ElevatorArrived process
	 */
	private static final byte[] ELEVATOR_ARRIVED_HEADER = Header.ELEVATOR_ARRIVED.getHeader();

	/**
	 * Incoming header to indicate start for ServiceRequest process
	 */
	private static final byte[] SERVICE_REQUEST_HEADER = Header.SERVICE_REQUEST.getHeader();

	/**
	 * 
	 */
	/**
	 * Incoming header to indicate start for ServiceRequest process
	 */
	private static final byte[] TRANSIENT_FAULT_HEADER = Header.TRANSIENT_FAULT.getHeader();

	
	/**
	 * Creates new dispatcher (for external requests use)
	 * 
	 * @param s   is the reference to the sender to Scheduler Subsystem from Floor
	 *            Subsystem
	 * @param arg is the data received from SchedulerListener or InputFileReader
	 * @param id is the id of the associated elevator with the request
	 */
	public Dispatcher(FloorSubsystem s, Object arg, int id) {
		ID = id;
		floorSubsystem = s;
		data = new byte[100]; // initialize
		if (arg instanceof byte[]) {
			data = (byte[]) arg;
		}
	}
	
	/**
	 * Creates new dispatcher (for internal use)
	 * 
	 * @param s   is the reference to the sender to Scheduler Subsystem from Floor
	 *            Subsystem
	 * @param arg is the data received from SchedulerListener or InputFileReader
	 */
	public Dispatcher(FloorSubsystem s, Object arg) {
		floorSubsystem = s;
		data = new byte[100]; // initialize
		if (arg instanceof byte[]) {
			data = (byte[]) arg;
		}
	}

	/**
	 * Dispatches requests to new Threads
	 */
	public void run() {
		System.out.println("Floor: Dispatching request...");

		/*
		 * Retrieve only header from data received
		 */
		byte[] temp = new byte[TEMP_LENGTH];
		System.arraycopy(data, 0, temp, 0, TEMP_LENGTH);

		if (Arrays.equals(temp, ELEVATOR_ARRIVED_HEADER)) {
			System.out.println("Floor: Dispatched to Elevator Arrived Process ");
			Thread thread = new ElevatorArrived(floorSubsystem, data, ID);
			thread.start();
		} else if (Arrays.equals(temp, SERVICE_REQUEST_HEADER)) {
			System.out.println("Floor: Dispatched to Service Request Process ");
			Thread thread = new ServiceRequest(floorSubsystem, data);
			thread.start();
		}
		else if(Arrays.equals(temp,TRANSIENT_FAULT_HEADER)) {
			System.out.println("Floor: Dispatched to Non Transient Fault Process ");
			Thread thread = new TransientFaultHandler(floorSubsystem, data);
			thread.start();
		}
		
	}

}
