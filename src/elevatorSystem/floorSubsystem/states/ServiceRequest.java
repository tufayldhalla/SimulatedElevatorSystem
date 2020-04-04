package elevatorSystem.floorSubsystem.states;

import java.io.IOException;

import elevatorSystem.floorSubsystem.FloorSubsystem;
import elevatorSystem.floorSubsystem.communication.SendToScheduler;
import elevatorSystem.floorSubsystem.helpers.Floor;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.Status;

/**
 * Deals with what to do when a user requests service at a floor
 * 
 * @author L4G3
 * @version 2.0
 */
public class ServiceRequest extends Thread {

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * Outgoing header to indicate start for AddNewDestination process
	 */
	private static final byte[] SELECT_ELEV_HEADER = Header.SELECT_ELEVATOR.getHeader();

	/**
	 * Reference to the sender to Scheduler Subsystem from Floor Subsystem
	 */
	private SendToScheduler sender;

	/**
	 * Stores data received from SchedulerListener or InputFileReader
	 */
	private byte[] data;

	/**
	 * Reference to FloorSubsystem's floors
	 */
	private Floor[] floors;

	/**
	 * Floor number of request
	 */
	private int floorNum;

	/**
	 * fault
	 */
	private int fault;

	/**
	 * Desired direction of request
	 */
	private String direction;

	/**
	 * Reference to floorSubsystem to use for output
	 */
	private FloorSubsystem floorSubsystem;

	/**
	 * Creates new ServiceRequest process
	 * 
	 * @param s              is the sender to Scheduler Subsystem from Floor
	 *                       Subsystem
	 * @param serviceRequest is the data received from SchedulerListener or
	 *                       InputFileReader
	 * @param floorsArray    is a reference to FloorSubsystem's floors
	 */
	public ServiceRequest(FloorSubsystem f, byte[] serviceRequest) {
		super("Service Request");
		sender = f.getSender();
		data = serviceRequest;
		floors = f.getFloors();
		floorSubsystem = f;
	}

	/**
	 * Executes states
	 */
	public void run() {

		decodeData(); // decode data received

		Floor floor = floors[floorNum - 1]; // floor that request came from

		// turn ON request lamps
		if (direction.equals("Up")) {
			floor.setUpRequestLamp(Status.ON);
			
			/*
			 * Output for simulating on console and to output file
			 */
			String s = "Floor: #" + floorNum + " - Up Request lamp = " + floor.getUpRequestLamp() + " (fault = " + fault
					+ ")";
			System.out.println(s);
			new Thread() {
				public void run() {

					try {
						floorSubsystem.getOutput().write(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else if (direction.equals("Down")) {
			floor.setDownRequestLamp(Status.ON);

			String s = "Floor: #" + floorNum + " - Down Request lamp = " + floor.getDownRequestLamp() + " (fault = "
					+ fault + ")";
			System.out.println(s);
			new Thread() {
				public void run() {
					/*
					 * Output for simulating on console and to output file
					 */

					try {
						floorSubsystem.getOutput().write(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}

		/*
		 * Format the packet to send to the Scheduler (SelectElevator expects
		 * SELECT_ELEV_HEADER, data without the header, 0). Packet sent echoes what this
		 * class received, however, takes out the header and replaces with appropriate
		 * one
		 */
		int len = -1;// don't include last byte (end of packet)
		for (int i = TEMP_LENGTH; i < data.length; i++) {
			len++;
		}
		int index = 0;
		byte[] msg = new byte[len + SELECT_ELEV_HEADER.length + ZERO_PARSER.length];
		System.arraycopy(SELECT_ELEV_HEADER, 0, msg, index, SELECT_ELEV_HEADER.length);
		index += SELECT_ELEV_HEADER.length;
		System.arraycopy(data, TEMP_LENGTH, msg, index, len);

		System.out.println("Floor: " + this.getName() + " trying to send to Scheduler");
		sender.send(msg);
	}

	/**
	 * Decodes the data received to retrieve floor number, direction and floor
	 * destination info. Receives data in format: "floorNum, 0, direction, 0,
	 * destination, 0, fault, 0"
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
		floorNum = Integer.valueOf(new String(data, index, len)); // Find floor num

		// Find out the direction that request occurred at
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		direction = new String(data, index, len);

		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}

		/*
		 * NOTE: receives destination as well, but does not currently need that
		 * information within this class..
		 */

		// Find out the fault
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
