package elevatorSystem.floorSubsystem.states;

import java.io.IOException;

import elevatorSystem.floorSubsystem.FloorSubsystem;
import elevatorSystem.floorSubsystem.helpers.Floor;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Status;

/**
 * Deals with what to do when an elevator arrives at a floor
 * 
 * @author L4G3
 * @version 2.0
 */
public class ElevatorArrived extends Thread {

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
	 * Id of elevator that arrived
	 */
	private int ID;

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
	public ElevatorArrived(FloorSubsystem f, byte[] msg, int id) {
		super("Elevator Arrived");
		ID = id;
		data = msg;
		floors = f.getFloors();
		floorSubsystem = f;
	}

	/**
	 * Starts state machine
	 */
	public void run() {

		decodeData(); // decode data

		Floor floor = floors[floorNum - 1]; // floor that elevator Arrived at

		new Thread() {
			public void run() {
				/*
				 * Output for simulating on console and to output file
				 */
				String s = "\nFloor: ID# " + floorNum + " - Elevator " + ID + " arrived";
				System.out.println("\n" + s);
				try {
					floorSubsystem.getOutput().write(s);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();

		/*
		 * ENTRY: turn on direction lamps and turn off associated request lamp for
		 * direction if on
		 */
		if (direction.equals("Up")) {
			floor.setUpDirectionLamp(Status.ON, ID); // turn on direction lamp
			String s1 = "Floor: ID# " + floorNum + " - Up Direction lamp = " + floor.getUpDirectionLamps()[ID - 1];
			System.out.println(s1);

			new Thread() {
				public void run() {
					/*
					 * Output for simulating on console and to output file
					 */
					try {
						floorSubsystem.getOutput().write(s1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			if (floor.getUpRequestLamp() == Status.ON) {
				floor.setUpRequestLamp(Status.OFF); // turn off request lamp

				String s2 = "Floor: ID# " + floorNum + " - Up Request lamp = " + floor.getUpRequestLamp() + "\nFloor: ID# "
						+ floorNum + " - Up request has been serviced\nFloor: ID# " + floorNum + " - User gets into car";
				System.out.println(s2);
				/*
				 * Output for simulating on console and to output file
				 */
				try {
					floorSubsystem.getOutput().write(s2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else if (direction.equals("Down")) {
			floor.setDownDirectionLamp(Status.ON, ID);// turn on direction lamp

			new Thread() {
				public void run() {

					String s1 = "Floor: ID# " + floorNum + " - Down Direction lamp = "
							+ floor.getDownDirectionLamps()[ID - 1];
					System.out.println(s1);
					/*
					 * Output for simulating on console and to output file
					 */
					try {
						floorSubsystem.getOutput().write(s1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			if (floor.getDownRequestLamp() == Status.ON) {
				floor.setDownRequestLamp(Status.OFF);// turn off request lamp

				new Thread() {
					public void run() {
						String s2 = "Floor: ID# " + floorNum + " - Down Request lamp = " + floor.getDownRequestLamp()
								+ "\nFloor: ID# " + floorNum + " - Down request has been serviced\nFloor: ID# " + floorNum
								+ " - User gets into car";
						System.out.print(s2);
						/*
						 * Output for simulating on console and to output file
						 */
						try {
							floorSubsystem.getOutput().write(s2);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.start();
			}
		}
		
		try {
			Thread.sleep(IntData.DIRECTION_LAMPS.getTime(floorSubsystem.getSpeed()));
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		/*
		 * EXIT: turn off direction lamps
		 */
		if (direction.equals("Up")) {
			floor.setUpDirectionLamp(Status.OFF, ID);// turn off direction lamp

			new Thread() {
				public void run() {
					String s1 = "Floor: ID# " + floorNum + " - Up Direction lamp = " + floor.getUpDirectionLamps()[ID - 1];
					System.out.println(s1);
					/*
					 * Output for simulating on console and to output file
					 */
					try {
						floorSubsystem.getOutput().write(s1);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else if (direction.equals("Down")) {
			floor.setDownDirectionLamp(Status.OFF, ID); // turn off direction lamp

			new Thread() {
				public void run() {

					String s1 = "Floor: ID# " + floorNum + " - Down Direction lamp = "
							+ floor.getDownDirectionLamps()[ID - 1];
					System.out.println(s1);

					/*
					 * Output for simulating on console and to output file
					 */
					try {
						floorSubsystem.getOutput().write(s1);
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