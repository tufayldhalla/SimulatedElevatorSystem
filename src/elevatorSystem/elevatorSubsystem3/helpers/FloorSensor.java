package elevatorSystem.elevatorSubsystem3.helpers;

import elevatorSystem.elevatorSubsystem3.communication.SendToScheduler;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Speed;
import elevatorSystem.miscellaneous.Status;

/**
 * Sensor for an elevator
 * 
 * @author L4G3
 * @version 2.0
 */
public class FloorSensor extends Thread {

	/**
	 * Condition to periodically sense
	 */
	private boolean sensing;

	/**
	 * Elevator ID
	 */
	private int id;

	/**
	 * Reference to sender to Scheduler from ElevatorSubsystem
	 */
	private SendToScheduler sender;

	/**
	 * Outgoing header to indicate start for Arrived At Floor process
	 */
	private static final byte[] ARRIVED_AT_FLOOR_HEADER = Header.ARRIVED_AT_FLOOR.getHeader();

	/**
	 * Elevator that floor sensor is in
	 */
	private Elevator elevator;
	
	/**
	 * Speed reference
	 */
	private Speed speed;
	
	/**
	 * Creates new Floor Sensor
	 * 
	 * @param s is a reference to sender to Scheduler from ElevatorSubsystem
	 */
	public FloorSensor(Elevator e, SendToScheduler s, int elevatorID, Speed speed) {
		elevator = e;
		this.speed = speed;
		sender = s;
		id = elevatorID;
		sensing = false;
	}

	/**
	 * Senses the floors, alerts scheduler of each floor it visits
	 */
	public void run() {
		while (true) {
			System.out.print("");
			while (sensing) {
				System.out.println("Elevator 3: SENSING...");

				try {
					Thread.sleep(IntData.FLOOR_SENSOR_TIME.getTime(speed));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("Elevator 3: Arrived at floor...");
				byte[] newData = ("" + id).getBytes();
				int index = 0;
				byte[] msg = new byte[newData.length + ARRIVED_AT_FLOOR_HEADER.length];
				System.arraycopy(ARRIVED_AT_FLOOR_HEADER, 0, msg, index, ARRIVED_AT_FLOOR_HEADER.length);
				index += ARRIVED_AT_FLOOR_HEADER.length;
				System.arraycopy(newData, 0, msg, index, newData.length);
				sender.send(msg);
			}
		}
	}

	/**
	 * Stop sensing (i.e. when stop elevator)
	 */
	public synchronized void stopSensing() {
		sensing = false;
	}

	/**
	 * Start sensing (i.e. when start elevator)
	 */
	public synchronized void startSensing() {
		elevator.setMotor(Status.ON);
		sensing = true;
	}

}
