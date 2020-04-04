package elevatorSystem.miscellaneous;

/**
 * Configurable data that has an associated String
 */
public enum StringData {

	/*
	 * to signal to elevator
	 */
	DONE("done"), // tell elevator no more requests after this drop off
	CONTINUE("continue"), // tell elevator there are more requests after this drop off

	/*
	 * state of each elevator status floor
	 */
	UP("Up"), // elevator car going up
	DOWN("Down"), // elevator car going down
	IDLE("idle"), // elevator car is not going anywhere (idle)

	/*
	 * Type of requesta
	 */
	PICK_UP("pick up"), // request is a pickup
	DROP_OFF("drop off");// request is a drop off

	/**
	 * string represented by StringData
	 */
	private final String str;

	StringData(String s) {
		str = s;
	}

	/**
	 * 
	 * @return String of StringData
	 */
	public String getString() {
		return str;
	}
}
