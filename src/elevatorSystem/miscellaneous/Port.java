package elevatorSystem.miscellaneous;

/**
 * Port numbers for all the receiving sockets
 * 
 * @author L4G3
 * @version 2.0
 */
public enum Port {

	
	/*
	 * Port for Scheduler to send to FloorSubsystem with its notification of
	 * elevator arrival
	 */
	F_SCHEDULER_LISTENER1(2000), // for elevator car 1 arrivals
	F_SCHEDULER_LISTENER2(2001), // for elevator car 2 arrivals
	F_SCHEDULER_LISTENER3(2002), // for elevator car 3 arrivals
	F_SCHEDULER_LISTENER4(2003), // for elevator car 4 arrivals

	/*
	 * Port for Input File Reader to send to with its new data
	 */
	F_INPUT_LISTENER(6789),

	/*
	 * Port for FloorSubsystem to send to Scheduler with its new request to service
	 */
	S_FLOOR_LISTENER(6900),

	/*
	 * Ports for ElevatorSubsystem to send to Scheduler with its arrived at floor
	 * notifications and its new destination requests
	 */
	S_ELEVATOR_LISTENER1(5020), // for elevator subsystem 1
	S_ELEVATOR_LISTENER2(5021), // for elevator subsystem 2
	S_ELEVATOR_LISTENER3(5022), // for elevator subsystem 3
	S_ELEVATOR_LISTENER4(5023), // for elevator subsystem 4

	/*
	 * Ports for Scheduler to send to ElevatorSubsystem<k> with its notification to
	 * start or stop or simulate destination
	 */
	E_SCHEDULER_LISTENER1(2300), // for elevator subsystem 1
	E_SCHEDULER_LISTENER2(2301), // for elevator subsystem 2
	E_SCHEDULER_LISTENER3(2302), // for elevator subsystem 3
	E_SCHEDULER_LISTENER4(2303); // for elevator subsystem 4

	/**
	 * Port number for the named port
	 */
	private final int portNum;

	/**
	 * Port
	 * 
	 * @param num is the port number
	 */
	Port(int num) {
		this.portNum = num;
	}

	/**
	 * Retrieves the port number for the named port
	 * 
	 * @return portNum
	 */
	public int getPort() {
		return portNum;
	}
}
