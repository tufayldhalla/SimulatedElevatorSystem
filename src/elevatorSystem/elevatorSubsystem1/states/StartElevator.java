package elevatorSystem.elevatorSubsystem1.states;

import java.io.IOException;

import elevatorSystem.elevatorSubsystem1.helpers.Elevator;
import elevatorSystem.elevatorSubsystem1.ElevatorSubsystem;
import elevatorSystem.elevatorSubsystem1.communication.SendToScheduler;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Status;

/**
 * Process to start elevator
 *
 * @author L4G3
 * @version 2.0
 */
public class StartElevator extends Thread {

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
	 * ID of Elevator to start
	 */
	private int elevatorID;

	/**
	 * Reference to ElevatorSubsystem
	 */

	private int fault;

	/**
	 * Reference to ElevatorSubsystem
	 */
	private ElevatorSubsystem elevatorSubsystem;

	private SendToScheduler sender;

	private static final byte[] DOOR_FAULT_HEADER = Header.DOOR_FAULT.getHeader();

	private static final byte[] DOOR_FIXED_HEADER = Header.DOOR_FIXED.getHeader();

	private static final byte[] SYSTEM_FAULT_HEADER = Header.SYSTEM_FAULT.getHeader();

	/**
	 * Integer flag for fault that tells if fault is implemented or not (0
	 */

	/**
	 * Create new Start Elevator process
	 * 
	 * @param msg   is the message received by SchedulerListener
	 * @param elevs is the reference to the System's elevators
	 */
	public StartElevator(ElevatorSubsystem elevs, byte[] msg) {
		super("Start Elevator");
		data = msg;
		elevator = elevs.getElevator();
		elevatorSubsystem = elevs;
		sender = elevs.getSender();
	}

	/**
	 * Decodes data and changes state of doors, motor, etc.
	 */
	public void run() {
		decodeData(); // decodes the data

		/*
		 * DOOR STUCK OPEN AND WONT CLOSE
		 */
		if (fault == 3) {
			// set elevator working status to not working
			elevator.setWorking(false);

			// Send data to scheduler with format, (SYSTEM_FAULT, Elevator ID, 0)
			byte[] newData = ("" + elevatorID).getBytes();

			int index = 0;

			byte[] msg = new byte[DOOR_FAULT_HEADER.length + newData.length];

			System.arraycopy(DOOR_FAULT_HEADER, 0, msg, index, DOOR_FAULT_HEADER.length);
			index += DOOR_FAULT_HEADER.length;
			System.arraycopy(newData, 0, msg, index, newData.length);
			index += newData.length;
			System.out.println(
					"\n\n***********\nElevator " + elevatorID + ": FAULT OCCURRED--> DOORS WONT CLOSE\n***********\n");
			if (elevator.getMotor() == Status.ON) {
				elevator.getFloorSensor().stopSensing();
				System.out.println("ELEVATOR " + elevatorID + " DOORS STUCK OPEN");
			}
			sender.send(msg);

			// Simulate doors open for 1 minute (simulate transient fault)
			try {
				Thread.sleep(IntData.DOORS_STUCK_OPEN.getTime(elevatorSubsystem.getSpeed()));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// set elevator working status to not working
			elevator.setWorking(true);

			// Send data to scheduler with format, (SYSTEM_FAULT, Elevator ID, 0)
			newData = ("" + elevatorID).getBytes();

			index = 0;

			msg = new byte[DOOR_FIXED_HEADER.length + newData.length];

			System.arraycopy(DOOR_FIXED_HEADER, 0, msg, index, DOOR_FIXED_HEADER.length);
			index += DOOR_FIXED_HEADER.length;
			System.arraycopy(newData, 0, msg, index, newData.length);
			index += newData.length;
			System.out.println("\n\n***********\nElevator " + elevatorID + ": FAULT FIXED, DOORS CLOSED\n***********\n");
			sender.send(msg);

			return;
		}
		/*
		 * ELEVATOR STALLED
		 */
		else if (fault == 2) {
			elevator.setDoors(Status.CLOSED); // close doors
		}
		/*
		 * NO FAULT :)
		 */
		else {
			// starts elevator if its not already in motion
			if (elevator.getMotor() == Status.OFF) {
				elevator.setDoors(Status.CLOSED); // close doors
				// elevator.setMotor(Status.ON); // motor on

				elevator.getFloorSensor().startSensing(); // start sensing for floors

				String s1 = "Elevator: ID# " + elevatorID + " starts\nElevator: ID# " + elevatorID + " - Doors = "
						+ elevator.getDoors() + "\nElevator: ID# " + elevatorID + " - Motor = " + elevator.getMotor();
				System.out.println(s1);
				
				new Thread() {
					public void run() {
						/*
						 * Output for simulating on console and to output file
						 */
						try {
							elevatorSubsystem.getOutput().write("\n" + s1);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
			return;
		}

		//if (elevator.getMotor() == Status.OFF && elevator.isWorking()) {
			elevator.setWorking(false);
			// Send data to scheduler with format, (SYSTEM_FAULT, Elevator ID, 0)
			byte[] newData = ("" + elevatorID).getBytes();

			int index = 0;

			byte[] msg = new byte[SYSTEM_FAULT_HEADER.length + newData.length];

			System.arraycopy(SYSTEM_FAULT_HEADER, 0, msg, index, SYSTEM_FAULT_HEADER.length);
			index += SYSTEM_FAULT_HEADER.length;
			System.arraycopy(newData, 0, msg, index, newData.length);
			index += newData.length;
			System.out
					.println("\n\n***********\nElevator " + elevatorID + ": FAULT OCCURRED --> CAR STUCK\n***********\n");
			//if (elevator.getMotor() == Status.ON) {
				elevator.getFloorSensor().stopSensing();
				System.out.println("ELEVATOR " + elevatorID + " STUCK BETWEEN FLOORS");
			//}
			sender.send(msg);

		//}
	}

	/**
	 * Decodes the data received to retrieve elevator ID. Receives data in format:
	 * "elevatorID, 0, fault(1 or 2), 0"
	 */
	private void decodeData() {
		int len = 0;
		int index = TEMP_LENGTH;

		// Find out which elevator to start
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		elevatorID = Integer.valueOf(new String(data, index, len)); // Find elevator ID

		// Find out the fault.
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {

			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}

		fault = Integer.valueOf(new String(data, index, len));

	}
}
