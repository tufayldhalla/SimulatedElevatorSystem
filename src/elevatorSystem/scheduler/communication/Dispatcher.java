package elevatorSystem.scheduler.communication;

import java.util.Arrays;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.scheduler.Scheduler;
import elevatorSystem.scheduler.states.AddNewDestination;
import elevatorSystem.scheduler.states.ArrivedAtFloor;
import elevatorSystem.scheduler.states.FaultHandler;


/**
 * Dispatches requests received by Scheduler Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class Dispatcher extends Thread {

	/**
	 * Sender to Floor Subsystem from Scheduler Subsystem
	 */
	private Scheduler schedulerSubsystem;

	/**
	 * Incoming header to indicate start for Add New Destination process
	 */
	private static final byte[] ADD_NEW_DEST_HEADER = Header.ADD_NEW_DESTINATION.getHeader();

	/**
	 * Incoming header to indicate start for ElevatorArrived process
	 */
	private static final byte[] SELECT_ELEVATOR_HEADER = Header.SELECT_ELEVATOR.getHeader();

	/**
	 * Incoming header to indicate start for ServiceRequest process
	 */
	private static final byte[] ARRIVED_AT_FLOOR_HEADER = Header.ARRIVED_AT_FLOOR.getHeader();

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();
	
	/**
	 * Faults (2)
	 */
	private static final byte[] SYSTEM_FAULT_HEADER = Header.SYSTEM_FAULT.getHeader();
	
	/**
	 * Faults (3)
	 */
	private static final byte[] DOOR_FAULT_HEADER = Header.DOOR_FAULT.getHeader();
	
	/**
	 * Transient fault fixed
	 */
	private static final byte[] DOOR_FIXED_HEADER = Header.DOOR_FIXED.getHeader();
	
	/**
	 * Data received to be decoded
	 */
	private byte data[];

	/**
	 * Creates new senders to Floor and Elevator Subsystems from Scheduler Subsystem
	 * 
	 * @param s   is a reference to the Scheduler subsystem
	 * @param arg is the packet data received
	 */
	public Dispatcher(Scheduler s, Object arg) {
		schedulerSubsystem = s;
		data = new byte[100]; // initialize
		if (arg instanceof byte[]) {
			data = (byte[]) arg;
		}
	}

	/**
	 * Dispatches requests to new Threads
	 */
	public void run() {
		System.out.println("Scheduler: Dispatching request...");
		byte[] temp = new byte[TEMP_LENGTH];
		System.arraycopy(data, 0, temp, 0, TEMP_LENGTH);
		if (Arrays.equals(temp, SELECT_ELEVATOR_HEADER)) {
			System.out.println("Scheduler: Adding request to the pending request queue");
			schedulerSubsystem.addRequest(data);
		} else if (Arrays.equals(temp, ADD_NEW_DEST_HEADER)) {
			System.out.println("Scheduler: dispatching to Select Elevator Process");
			AddNewDestination addToList = new AddNewDestination(schedulerSubsystem, data);
			addToList.run();
		} else if (Arrays.equals(temp, ARRIVED_AT_FLOOR_HEADER)) {
			System.out.println("Scheduler: Received a notification from an elevator sensor");
			ArrivedAtFloor checkFloor = new ArrivedAtFloor(schedulerSubsystem, data);
			checkFloor.run();
		} else if (Arrays.equals(temp, SYSTEM_FAULT_HEADER)) {
			System.out.println("Scheduler: Hard fault!"); 
			FaultHandler handleFault = new FaultHandler(schedulerSubsystem, data, false);
			handleFault.run();
		} else if (Arrays.equals(temp, DOOR_FAULT_HEADER)) {
			System.out.println("Scheduler: Transient fault!"); 
			FaultHandler handleFault = new FaultHandler(schedulerSubsystem, data, true);
			handleFault.run();
		}
		 else if (Arrays.equals(temp, DOOR_FIXED_HEADER)) {
				System.out.println("Scheduler: Transient fault fixed :)"); 
				FaultHandler handleFault = new FaultHandler(schedulerSubsystem, data);
				// no need to start this process... it's constructor will!
				handleFault.toString();
			}
	}

}
