package elevatorSystem.elevatorSubsystem4.states;

import java.io.IOException;

import elevatorSystem.elevatorSubsystem4.helpers.Elevator;
import elevatorSystem.elevatorSubsystem4.ElevatorSubsystem;
import elevatorSystem.elevatorSubsystem4.communication.SendToScheduler;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.Status;

/**
 * Process to select car button
 *
 * @author L4G3
 * @version 2.0
 */
public class DestinationSelected extends Thread {

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
	 * ID of Elevator to to change button status
	 */
	private int elevatorID;

	/**
	 * Button to change lamp status
	 */
	private int destinationButton;

	/**
	 * Flag for if fault is implemented or not: 1 or 2
	 */
	private int fault;

	/**
	 * Key to pass back to scheduler
	 */
	private int key;

	/**
	 * Reference to ElevatorSubsystem
	 */
	private ElevatorSubsystem elevatorSubsystem;

	/**
	 * Reference to sender from ElevatorSubsystem to Scheduler
	 */
	private SendToScheduler sender;

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * Outgoing header to indicate start for ElevatorArrived process
	 */
	private static final byte[] ADD_NEW_DEST_HEADER = Header.ADD_NEW_DESTINATION.getHeader();

	/**
	 * Creates new Destination Selected process
	 * 
	 * @param s     is the reference to the sender from ElevatorSubsystem to
	 *              Scheduler
	 * @param d     is the data being received by SchedulerListener
	 * @param elevs is the reference to the System's elevators
	 */
	public DestinationSelected(ElevatorSubsystem elevs, byte[] d) {
		super("Destination Selected");
		sender = elevs.getSender();
		data = d;
		fault = 0;
		elevator = elevs.getElevator();
		elevatorSubsystem = elevs;
	}

	/**
	 * Decodes data and changes state of doors, motor, etc.
	 */
	public void run() {

		decodeData(); // decode data

		/*
		 * set car button that user pressed to be on
		 */
		elevator.setButtonLamp(destinationButton, Status.ON);

		String s = "\nElevator: ID# " + elevatorID + " Car Button " + destinationButton + " Car button = "
				+ elevator.getButtonLamps()[destinationButton - 1];
		System.out.println(s);
		new Thread() {
			public void run() {
				/*
				 * Output for simulating on console and to output file
				 */
				try {
					elevatorSubsystem.getOutput().write(s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

		/*
		 * formulate data to dispatch to Scheduler Dispatcher (i.e. want to add
		 * destination to elevator queue) AddNewDestination expects the format of
		 * ADD_NEW_DEST_HEADER, elevatorID, 0, destinationButton, 0, fault, 0
		 */
		byte dataID[] = ("" + elevatorID).getBytes();
		byte data[] = ("" + destinationButton).getBytes();
		byte faultData[] = ("" + fault).getBytes();
		byte keyData[] = ("" + key).getBytes();
		int len = ADD_NEW_DEST_HEADER.length + data.length + dataID.length + 3 * ZERO_PARSER.length + faultData.length
				+ keyData.length;
		byte msg[] = new byte[len];
		int index = 0;
		System.arraycopy(ADD_NEW_DEST_HEADER, 0, msg, index, ADD_NEW_DEST_HEADER.length);
		index += ADD_NEW_DEST_HEADER.length;
		System.arraycopy(dataID, 0, msg, index, dataID.length);
		index += dataID.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(data, 0, msg, index, data.length);
		index += data.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(faultData, 0, msg, index, faultData.length);
		index += faultData.length;
		System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
		index += ZERO_PARSER.length;
		System.arraycopy(keyData, 0, msg, index, keyData.length);

		// send to Scheduler
		sender.send(msg);

	}

	/**
	 * Decodes the data received to retrieve elevator ID. Receives data in format:
	 * "elevatorID, 0, destination, 0, Fault (1 or 2), 0, key, 0"
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
		elevatorID = Integer.valueOf(new String(data, index, len)); // Find elevator num

		// Find out destination that request wants
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		destinationButton = Integer.valueOf(new String(data, index, len)); // Find floor dest. num

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

		// Find out the key.
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		key = Integer.valueOf(new String(data, index, len));
	}
}
