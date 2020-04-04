package elevatorSystem.floorSubsystem.helpers;

/**
 * Request to store fields from table
 * 
 * @author L4G3
 * @version 2.0
 */
public class Request {

	/**
	 * Time of request
	 */
	private Time time;
	
	/**
	 * String rep of time
	 */
	private String timeStr;

	/**
	 * Floor request came from
	 */
	private String floor;

	/**
	 * Direction user would like to go
	 */
	private String floorButton;

	/**
	 * Floor they would like to visit once on car
	 */
	private String carButton;
	
	/**
	 * Adding a fault in at a particular floor
	 */
	private String fault;

	/**
	 * Creates new request
	 * 
	 * @param t  is the time of request
	 * @param f  is the floor the request came from
	 * @param fB is the direction floor button that was pressed
	 * @param cB is the car button that the use will press
	 * @param ft is the fault that can occur in the system
	 */
	public Request(String t, String f, String fB, String cB, String ft) {
		timeStr = t;
		time = new Time(t);
		floor = f;
		floorButton = fB;
		carButton = cB;
		fault = ft;
	}

	/**
	 * Returns the time
	 * 
	 * @return time
	 */
	public Time getTime() {
		return time;
	}

	/**
	 * Gets the request (in form of: floor, 0, Direction, 0, CarButton, 0, Fault, 0)
	 * 
	 * @return payload to be given to dispatcher from InputFileReader
	 */
	public byte[] getRequest() {
		byte msg1[] = floor.getBytes();
		byte msg2[] = floorButton.getBytes();
		byte msg3[] = carButton.getBytes();
		byte msg4[] = fault.getBytes();
		byte zero[] = { (byte) 0 };
		int len = msg1.length + msg2.length + msg3.length + msg4.length + 4 * zero.length;

		byte msg[] = new byte[len];
		int index = 0;
		System.arraycopy(msg1, 0, msg, 0, msg1.length);
		index += msg1.length;
		System.arraycopy(zero, 0, msg, index, zero.length);
		index += zero.length;
		System.arraycopy(msg2, 0, msg, index, msg2.length);
		index += msg2.length;
		System.arraycopy(zero, 0, msg, index, zero.length);
		index += zero.length;
		System.arraycopy(msg3, 0, msg, index, msg3.length);
		index += msg3.length;
		System.arraycopy(zero, 0, msg, index, zero.length);
		index += zero.length;
		System.arraycopy(msg4, 0, msg, index, msg4.length);
		index += msg4.length;
		System.arraycopy(zero, 0, msg, index, zero.length);
		index += zero.length;
		return msg;
	}
	
	/**
	 * Returns string of request for output
	 * 
	 * @return string representation of Request
	 */
	public String toString() {
		return "Input Request: " +  timeStr + " " + floor + " " +  floorButton + " " + carButton + " " + fault;
	}

}
