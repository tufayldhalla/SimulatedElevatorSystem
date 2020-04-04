package elevatorSystem.scheduler.helpers;

import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;

/**
 * Holds the data within each request
 *
 * @author L4G3
 * @version 2.0
 */
public class RequestData {

	/**
	 * Number of floor to service
	 */
	private int floorToService;

	/**
	 * Direction that the user at the floor to service wishes to go in
	 */
	private StringData directionTraveling;

	/**
	 * Destination that the user will want to go to (if applicable)
	 */
	private int destination;

	/**
	 * Fault that will occur, for now just a boolean  
	 */
	private int fault; 
	
	/**
	 * Type of request elevator is servicing 
	 */
	private StringData type; 
	
	/**
	 * Key of request
	 */
	private Integer key;
	
	/**
	 * Constructs RequestData that comes from floorSubsystem's InputFileReader
	 * 
	 * @param s 					  is the scheduler subsystem reference to get next key
	 * @param floorToService     is the floor to service
	 * @param directionTraveling is the direction the user wants to go in
	 * @param dest               is the destination they will choose once in the
	 *                           elevator
	 * @param fault              does the elevator have a fault or not 
	 */
	public RequestData(Scheduler s, int floorToService, StringData directionTraveling, int dest, int fault) {
		this.floorToService = floorToService;
		this.directionTraveling = directionTraveling;
		this.destination = dest;
		this.fault = fault; 
		this.type = StringData.PICK_UP;
		this.key = s.nextKey();
	}
	
	/**
	 * Constructs RequestData that comes from floorSubsystem's InputFileReader
	 * 
	 * 
	 * @param floorToService     is the floor to service
	 * @param directionTraveling is the direction the user wants to go in
	 * @param dest               is the destination they will choose once in the
	 *                           elevator
	 * @param fault              does the elevator have a fault or not 
	 * @param key 					  is the key of the original request that has already attempted to pass thru system before fault
	 */
	public RequestData(int floorToService, StringData directionTraveling, int dest, int fault, int key) {
		this.floorToService = floorToService;
		this.directionTraveling = directionTraveling;
		this.destination = dest;
		this.fault = fault; 
		this.type = StringData.PICK_UP;
		this.key = key;
	}

	/**
	 * Constructs RequestData that comes from elevatorSubsystem's
	 * DestinationSelected (set destination to -1 because not applicable)
	 * 
	 * @param floorToService      is the floor the user wants to go to
	 * @param directionTravelling is the direction the elevator has to go to get
	 *                            there
	 */
	public RequestData(int floorToService, StringData directionTravelling, int key) {
		this.floorToService = floorToService;
		this.directionTraveling = directionTravelling;
		this.destination = -1;
		this.fault = -1; 
		this.type = StringData.DROP_OFF;
		this.key = key;
	} 

	/**
	 * Returns the floor number to service
	 * 
	 * @return floorToService
	 */
	public int getFloorToService() {
		return floorToService;
	}

	/**
	 * Returns the direction that the user wants to go to
	 * 
	 * @return directionTraveling
	 */
	public StringData getDirectionTraveling() {
		return directionTraveling;
	}

	/**
	 * Returns the floor that user will press once in car
	 * @return
	 */
	public int getDestinationFloor() {
		return destination;
	}

	/**
	 * Returns the status of the fault 
	 * 
	 * @return fault status of request
	 */
	public int getFault() {
		return fault; 
	}
	
	/**
	 * Returns the type of request being serviced by elevator
	 * 
	 * @returns type of request
	 */
	public StringData getType() {
		return type; 
	}
	
	/**
	 * Returns key of request
	 * 
	 * @return key
	 */
	public Integer getKey() {
		return key;
	}
	
	/**
	 * Checks if two requests are same (i.e. to use when checking if a request in
	 * the floorsToVisit queue already exists. Checks the difference between drop
	 * off and pick up)
	 * 
	 * @return true if object is equal
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		RequestData s = (RequestData) o;
		return s.getDirectionTraveling() == directionTraveling && s.getFloorToService() == floorToService
				&& s.getDestinationFloor() == destination && s.getFault() == fault;
	}
	
	/**
	 * @return string representing the request data
	 */
	public String toString() {
		String str = "";
		if(type == StringData.PICK_UP) {
			str += floorToService +", " + directionTraveling +", " + destination +", " + fault;
		}else {
			str += floorToService +", " + directionTraveling;
		}
		return str;
	}
}
