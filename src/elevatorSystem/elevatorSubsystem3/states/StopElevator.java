package elevatorSystem.elevatorSubsystem3.states;

import java.io.IOException;

import elevatorSystem.elevatorSubsystem3.helpers.Elevator;
import elevatorSystem.elevatorSubsystem3.ElevatorSubsystem;
import elevatorSystem.elevatorSubsystem3.communication.Dispatcher;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Status;
import elevatorSystem.miscellaneous.StringData;

/**
 * Process to stop elevator
 *
 * @author L4G3
 * @version 2.0
 */
public class StopElevator extends Thread {

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * Stores data received from SchedulerListener or InputFileReader
	 */
	private byte[] data;

	/**
	 * Reference to ElevatorSystem's elevators
	 */
	private Elevator elevator;

	/**
	 * ID of Elevator to stop
	 */
	private int elevatorID;

	/**
	 * Whether or not there are more requests in elevator queue
	 */
	private String continueStatus;

	/**
	 * Reference to ElevatorSubsystem
	 */
	private ElevatorSubsystem elevatorSubsystem;

	/**
	 * Floor that elevator stopped at
	 */
	private int floorNum;

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * Outgoing header to indicate start for ElevatorArrived process
	 */
	private static final byte[] START_ELEVATOR_HEADER = Header.START_ELEVATOR.getHeader();

	/**
	 * Creates new StopElevator process
	 * 
	 * 
	 * @param elevs is the reference to the Elevator Subsystem's elevators
	 * @param msg   is the data received by SchedulerListener
	 */
	public StopElevator(ElevatorSubsystem elevs, byte[] msg) {
		super("Stop Elevator");
		elevatorSubsystem = elevs;
		elevator = elevs.getElevator();
		data = msg;
	}

	/**
	 * Decodes data and changes state of doors, motor, etc.
	 */
	public void run() {

		decodeData(); // decode data
		elevator.getFloorSensor().stopSensing();// stop sensing
		elevator.setFloor(floorNum);
		elevator.setMotor(Status.OFF); // motor off
		elevator.setDoors(Status.OPEN); // doors open

		new Thread() {
			public void run() {
				String s = "Elevator: ID# " + elevatorID + " - stopped at floor " + floorNum + "\nElevator: ID# "
						+ elevatorID + " -  Motor = " + elevator.getMotor() + "\nElevator: ID# " + elevatorID + " - Doors = "
						+ elevator.getDoors();
				System.out.println(s);
				
				/*
				 * Output for simulating on console and to output file
				 */
				try {
					elevatorSubsystem.getOutput().write("\n" + s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

		/*
		 * check if the current floor is a car button request. turn off light if so
		 * 
		 */
		if (elevator.getButtonLamps()[floorNum - 1] == Status.ON) {
			elevator.setButtonLamp(floorNum, Status.OFF); // car button lamp off
			String s3 = "\nElevator: ID# " + elevatorID + " - Car Button " + floorNum + " Car button = "
					+ elevator.getButtonLamps()[floorNum - 1] + "\nElevator: ID# " + elevatorID
					+ " - User(s) gets out of car";
			System.out.println(s3);
			
			new Thread() {
				public void run() {
					try {
						elevatorSubsystem.getOutput().write(s3);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}

		/*
		 * formulate data to dispatch to Elevator Dispatcher (i.e. want to start again
		 * if there are more requests). StartElevator expects START_ELEVATOR_HEADER,
		 * elevatorID, 0
		 */
		if (continueStatus.equals(StringData.CONTINUE.getString())) {
			try {
				Thread.sleep(IntData.SIMULATE_USER.getTime(elevatorSubsystem.getSpeed()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			byte[] newData = ("" + elevatorID).getBytes();
			byte[] dummy = ("" + -1).getBytes();
			int len = START_ELEVATOR_HEADER.length + newData.length + ZERO_PARSER.length + dummy.length;
			byte msg[] = new byte[len];
			int index = 0;
			System.arraycopy(START_ELEVATOR_HEADER, 0, msg, index, START_ELEVATOR_HEADER.length);
			index += START_ELEVATOR_HEADER.length;
			System.arraycopy(newData, 0, msg, index, newData.length);
			index += newData.length;
			System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
			index += ZERO_PARSER.length;
			System.arraycopy(dummy, 0, msg, index, dummy.length);

			/*
			 * Message is sent internally instead of through SchedulerListener. dispatch to
			 * get to start elevator
			 */
			Dispatcher d = new Dispatcher(elevatorSubsystem, msg);
			d.start();
		}

	}

	/**
	 * Decodes the data received to retrieve elevator ID. Receives data in format:
	 * "elevatorID, 0, floorNum, 0, continueStatus, 0"
	 */
	private void decodeData() {
		int len = 0;
		int index = TEMP_LENGTH;

		// Find out which elevator to stop
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		elevatorID = Integer.valueOf(new String(data, index, len)); // find elevatorID

		// Find out what floor elevator at
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		floorNum = Integer.valueOf(new String(data, index, len)); // find floor num;

		// Find out destination that request wants
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		continueStatus = new String(data, index, len); // Find status elevator status (Done or continue)
	}
}
