package elevatorSystem.miscellaneous;

/**
 * Configurable data that has integer as associated value
 * 
 * @author L4G3
 * @version 2.0
 */
public enum IntData {
	/*
	 * configure system
	 */
	NUM_FLOORS(22), // 22 floors (use 10 for testing)
	NUM_ELEVATORS(4), // 4 elevators
	STARTING_FLOOR1(2), // starting floor of car 1
	STARTING_FLOOR2(4), // starting floor of car 2
	STARTING_FLOOR3(6), // starting floor of car 3
	STARTING_FLOOR4(8), // starting floor of car 4

	/*
	 * Configure Thread Sleep Times
	 */
	FLOOR_SENSOR_TIME(1000*3), 	// floor sensor senses car arrival every 3 sec
	SEND_TIME(100*2), 				// allow 1 sec for message to send before next
	SIMULATE_USER(100*2),			// allow for 1 sec for use to enter/leave car
	BUFFER_TIME(5000*2), 			// deal with pending queue every 5 secs ("clock cycle")
	CHECK_DONE(20000*2),			// wait 35 secs after each drop off success to see if done
	DOORS_STUCK_OPEN(15000*2),	// doors are stuck open 
	CHECK_FOR_STALL(1000*2),		// wait 10 sec for arrival sensor to have worked
	DIRECTION_LAMPS(1000*2);		

	/**
	 * integer represented by IntData
	 */
	private final int num;

	IntData(int x) {
		num = x;
	}

	/**
	 * 
	 * @return num associated with data
	 */
	public int getNum() {
		return num;
	}
	
	/**
	 * 
	 * @return num associated with data
	 */
	public int getTime(Speed s) {
		return (int) ((double)num/s.getSpeed());
	}
}
