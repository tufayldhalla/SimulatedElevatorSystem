package elevatorSystem.floorSubsystem.states;

import java.io.IOException;

import elevatorSystem.floorSubsystem.FloorSubsystem;
import elevatorSystem.floorSubsystem.helpers.Floor;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.Status;

/**
 * Deals with what to do when an elevator is stuck with its doors open at a
 * floor (turns the lamp back on)
 * 
 * @author L4G3
 * @version 2.0
 */
public class TransientFaultHandler extends Thread {

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
	 * Reference to FloorSubsystem
	 */
	private FloorSubsystem floorSubsystem;

	/**
	 * Desired direction of request
	 */
	private String direction;

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * Creates new ElevatorArrived process
	 * 
	 * @param data        is the data received from the SchedulerListener
	 * @param floorsArray is a reference to FloorSubsystem's floors
	 */
	public TransientFaultHandler(FloorSubsystem f, byte[] msg) {
		super("Doors stuck open on floor!");
		data = msg;
		floors = f.getFloors();
		floorSubsystem = f;
	}

	/**
	 * Starts state machine
	 */
	public void run() {

		decodeData(); // decode data

		Floor floor = floors[floorNum - 1]; // floor that elevator stuck at

		String s = "\nFloor: ID# " + floorNum + " - Elevator is stuck with doors open";
		System.out.println("\n" + s);
		
		
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

		/*
		 * Turn on request lamp
		 */
		if (direction.equals("Up")) {
			floor.setUpRequestLamp(Status.ON); // turn on request lamp
			String str = "\nFloor: ID# " + floorNum + " - Up Request Lamp = " + floor.getUpRequestLamp();
			System.out.println("\n" + str);
			
			new Thread() {
				public void run() {
					
					try {
						floorSubsystem.getOutput().write(str);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else if (direction.equals("Down")) {
			floor.setDownRequestLamp(Status.ON); // turn on request lamp
			String str = "\nFloor: ID# " + floorNum + " - Down Request Lamp = " + floor.getDownRequestLamp();
			System.out.println("\n" + str);
			
			new Thread() {
				public void run() {
					
					try {
						floorSubsystem.getOutput().write(str);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	/**
	 * Decodes the data received to retrieve floor number, direction and floor
	 * destination info. Receives data in format: "floorNum, 0, direction, 0"
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
		direction = new String(data, index, len); // Find direction

	}

}