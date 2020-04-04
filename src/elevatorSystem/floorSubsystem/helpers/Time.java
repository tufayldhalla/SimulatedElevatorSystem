package elevatorSystem.floorSubsystem.helpers;

/**
 * Time of request
 * 
 * @author L4G3
 * @version 2.0
 */
public class Time {

	/**
	 * Hour at which the request was made
	 */
	private int hour;

	/**
	 * Minute at which the request was made
	 */
	private int min;

	/**
	 * second at which the request was made
	 */
	private int sec;

	/**
	 * millisecond at which the request was made
	 */
	private int msec;

	/**
	 * to convert from hour to milliseconds
	 */
	private static final int HOUR_TO_MS = 3600000;

	/**
	 * to convert from minutes to milliseconds
	 */
	private static final int MIN_TO_MS = 60000;

	/**
	 * to convert from seconds to milliseconds
	 */
	private static final int SEC_TO_MS = 1000;

	/**
	 * Creates new Time
	 * 
	 * @param time is the time represented as a String
	 */
	public Time(String time) {
		String temp[] = time.split(":");
		String temp2[] = temp[2].split("\\.");

		hour = Integer.valueOf(temp[0]);
		min = Integer.valueOf(temp[1]);
		sec = Integer.valueOf(temp2[0]);
		msec = Integer.valueOf(temp2[1]);

	}

	/**
	 * Returns hour at which request was made
	 * 
	 * @return hour
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Returns min at which request was made
	 * 
	 * @return min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Returns sec at which request was made
	 * 
	 * @return sec
	 */
	public int getSec() {
		return sec;
	}

	/**
	 * Returns msec at which request was made
	 * 
	 * @return msec
	 */
	public int getMsec() {
		return msec;
	}

	/**
	 * Compares two times to figure out which happened first
	 * 
	 * @param t is the time to be compared to
	 * @return true if time is before t
	 */
	public boolean before(Time t) {
		if (hour < t.getHour()) {
			return true;
		} else if (hour == t.getHour()) {
			if (min < t.getMin()) {
				return true;
			} else if (min == t.getMin()) {
				if (sec < t.getSec()) {
					return true;
				} else if (sec == t.getSec()) {
					if (msec < t.getMsec()) {
						return true;
					}
				}
			}
		}
		return false;

	}

	/**
	 * Computes the duration of time that should pass before simulating the next
	 * request
	 * 
	 * @param t time prior to this request
	 * @return total number of milliseconds to wait before simulating next request
	 */
	public int waitTime(Time t) {
		int hours;
		hours = hour - t.getHour();
		int mins;
		mins = min - t.getMin();
		int secs;
		secs = sec - t.getSec();
		int msecs;
		msecs = msec - t.getMsec();

		int total = hours * HOUR_TO_MS + mins * MIN_TO_MS + secs * SEC_TO_MS + msecs;
		return total;
	}

}
