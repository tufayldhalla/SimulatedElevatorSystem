package elevatorSystem.miscellaneous;

/**
 * Headers for all types of processes
 *
 * @author L4G3
 * @version 2.0
 */
public enum Header {
	/*
	 * FloorSubsystem incoming request headers
	 */
	ELEVATOR_ARRIVED((byte) 0, (byte) 0, (byte) 1), // From Scheduler
	SERVICE_REQUEST((byte) 0, (byte) 1, (byte) 0), // From Input File Reader (internal)

	/*
	 * SchedulerSubsystem incoming request headers
	 */
	ARRIVED_AT_FLOOR((byte) 0, (byte) 0, (byte) 1), // From ElevatorSubsystem<k>
	ADD_NEW_DESTINATION((byte) 0, (byte) 1, (byte) 0), // From ElevatorSubsystem<k>
	SELECT_ELEVATOR((byte) 0, (byte) 1, (byte) 1), // From FloorSubsystem

	/*
	 * ElevatorSubsystem incoming request headers
	 */
	START_ELEVATOR((byte) 0, (byte) 0, (byte) 1), // From Scheduler or internal
	STOP_ELEVATOR((byte) 0, (byte) 1, (byte) 0), // From Scheduler or internal
	DESTINATION_SELECTED((byte) 0, (byte) 1, (byte) 1), // From Scheduler

	/*
	 * Fault header (from Elevator Subsystem to Scheduler)
	 */
	SYSTEM_FAULT((byte) 2, (byte) 2, (byte) 2),
	
	/*
	 * DOOR header (from Elevator Subsystem to Scheduler)
	 */
	DOOR_FAULT((byte) 4, (byte) 4, (byte) 4),
	
	/*
	 * DOOR fixed header
	 */
	DOOR_FIXED((byte)5, (byte)5, (byte)5),

	/*
	 * transient fault header (from Scheduler to Floor Subsystem)
	 */
	TRANSIENT_FAULT((byte) 3, (byte) 3, (byte) 3),

	/*
	 * Zero to separate info or indicate end (i.e. trailer)
	 */
	ZERO((byte) 0),

	/*
	 * Reference length of all headers
	 */
	LENGTH;

	/**
	 * byte[] header for packet to send or receive
	 */
	private final byte[] header;

	/**
	 * length of header
	 */
	private final int headerLength = 3;

	/**
	 * Create new header of arbitrary length
	 * 
	 * @param arg byte array of arbitrary length
	 */
	Header(byte... arg) {
		header = arg;
	}

	/**
	 * retrieves byte array for header
	 * 
	 * @return header
	 */
	public byte[] getHeader() {
		return header;
	}

	/**
	 * gets length of header
	 * 
	 * @return
	 */
	public int getLength() {
		return headerLength;
	}
}
