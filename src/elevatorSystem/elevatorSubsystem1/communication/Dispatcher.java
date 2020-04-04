package elevatorSystem.elevatorSubsystem1.communication;

import java.util.Arrays;

import elevatorSystem.elevatorSubsystem1.ElevatorSubsystem;
import elevatorSystem.elevatorSubsystem1.states.DestinationSelected;
import elevatorSystem.elevatorSubsystem1.states.StartElevator;
import elevatorSystem.elevatorSubsystem1.states.StopElevator;
import elevatorSystem.miscellaneous.Header;

/**
 * Dispatches requests received by Elevator Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class Dispatcher extends Thread {

	/**
	 * Reference to ElevatorSubsystem
	 */
	private ElevatorSubsystem elevatorSubsystem;

	/**
	 * Data to be received from SchedulerListener
	 */
	private byte[] data;

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * Incoming header to indicate start for ElevatorArrived process
	 */
	private static final byte[] START_ELEVATOR_HEADER = Header.START_ELEVATOR.getHeader();

	/**
	 * Incoming header to indicate start for ServiceRequest process
	 */
	private static final byte[] STOP_ELEVATOR_HEADER = Header.STOP_ELEVATOR.getHeader();

	/**
	 * Incoming header to indicate start for ServiceRequest process
	 */
	private static final byte[] DEST_SELECTED_HEADER = Header.DESTINATION_SELECTED.getHeader();

	/**
	 * Creates new sender to Scheduler Subsystem from Elevator Subsystem
	 * 
	 * @param elevs is a reference to ElevatorSubsystem's elevators
	 * @param arg   is the data received from SchedulerListener
	 */
	public Dispatcher(ElevatorSubsystem elev, Object arg) {
		elevatorSubsystem = elev;
		data = new byte[100]; // initialize
		if (arg instanceof byte[]) {
			data = (byte[]) arg;
		}
	}

	/**
	 * Dispatches requests to new Threads
	 */
	public void run() {

		System.out.println("Elevator: Dispatching request...");

		/*
		 * Retrieve only header from data received (all headers are of length
		 * TEMP_LENGTH)
		 */
		byte[] temp = new byte[TEMP_LENGTH];
		System.arraycopy(data, 0, temp, 0, TEMP_LENGTH);

		if (Arrays.equals(temp, START_ELEVATOR_HEADER)) {
			System.out.println("Elevator 1: Dispatched to Start Elevator Process ");
			StartElevator newStartThread = new StartElevator(elevatorSubsystem, data);
			newStartThread.start();
		} else if (Arrays.equals(temp, STOP_ELEVATOR_HEADER)) {
			System.out.println("Elevator 1 : Dispatched to Stop Elevator Process ");
			StopElevator newStopThread = new StopElevator(elevatorSubsystem, data);
			newStopThread.start();
		} else if (Arrays.equals(temp, DEST_SELECTED_HEADER)) {
			System.out.println("Elevator 1 : Dispatched to Destination Selected Process ");
			DestinationSelected newDestThread = new DestinationSelected(elevatorSubsystem, data);
			newDestThread.start();
		}
	}
}
