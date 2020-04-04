package elevatorSystem.scheduler.helpers;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.StringData;

/**
 * Keeps track of floor and direction of elevator
 *
 * @author L4G3
 * @version 2.0
 */
public class ElevatorStatus extends Observable {

	/**
	 * List of requests of floors to visit by this elevator car
	 */
	private ArrayList<RequestData> floorsToVisit;

	/**
	 * ID of this car's elevator status
	 */
	private int id;

	/**
	 * if elevator is working this returns true
	 */
	private Boolean elevatorWorking;

	/**
	 * represent if the elevator has been told to start yet
	 */
	private boolean elevatorStarted;

	/**
	 * floor number that elevator is at
	 */
	private int elevatorFloor;

	/**
	 * direction that the elevator is going in
	 */
	private StringData elevatorDirection;

	/**
	 * Reference to GUI
	 */
	private GUIApplication gui = null;

	/**
	 * Starting floor of each elevator
	 */
	private static final int[] STARTING_FLOOR = { IntData.STARTING_FLOOR1.getNum(), IntData.STARTING_FLOOR2.getNum(),
			IntData.STARTING_FLOOR3.getNum(), IntData.STARTING_FLOOR4.getNum() };

	/**
	 * creates new elevator status
	 */
	public ElevatorStatus(int id) {
		this.id = id;
		setElevatorFloor(STARTING_FLOOR[id - 1]);
		this.elevatorDirection = StringData.IDLE; // initialize to idle (neither up nor down)
		floorsToVisit = new ArrayList<RequestData>();
		elevatorWorking = true;
		elevatorStarted = new Boolean(false);
	}

	/**
	 * Returns floor elevator is at
	 * 
	 * @return elevatorFloor
	 */
	public int getElevatorFloor() {
		return elevatorFloor;
	}

	/**
	 * Sets the floor that elevator is at
	 * 
	 * @param elevatorFloor is the new floor number
	 */
	public void setElevatorFloor(int elevatorFloor) {
		this.elevatorFloor = elevatorFloor;
		if (gui != null) {
			setChanged();
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_FLOOR));
		}
	}

	/**
	 * returns direction elevator is going in
	 * 
	 * @return elevatorDirection
	 */
	public StringData getElevatorDirection() {
		return elevatorDirection;
	}

	/**
	 * Sets the direction that the elevator is going in
	 * 
	 * @param status is the new direction of the elevator
	 */
	public void setElevatorDirection(StringData status) {

		this.elevatorDirection = status;
		if(gui != null) {
			setChanged(); 
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_DIRECTION)); 
		}
	}

	/**
	 * @return the status of whether or not this elevator has been started yet
	 *         within select elevator
	 */
	public boolean getElevatorStarted() {
		return elevatorStarted;
	}

	/**
	 * Sets whether or not this elevator has been started yet within select elevator
	 */
	public void setElevatorStarted(boolean state) {
		elevatorStarted = state;
		if(gui != null) {
			setChanged(); 
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_WORKING)); 
		}
	}

	/**
	 * @return list of floors to visit by this elevator car
	 */
	public ArrayList<RequestData> getFloorsToVisit() {
		return this.floorsToVisit;
	}

	/**
	 * @return id of elevator
	 */
	public int getID() {
		return id;
	}

	/**
	 * Increment current floor
	 */
	public void incrementFloor() {
		elevatorFloor++;
		if (gui != null) {
			setChanged();
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_FLOOR));
		}
	}

	/**
	 * Decrement current floor
	 */
	public void decrementFloor() {
		elevatorFloor--;
		if (gui != null) {
			setChanged();
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_FLOOR));
		}
	}

	/**
	 * Set flag if elevator is working or not
	 * 
	 * @param elevatorWorking true or false if elevator is working
	 */
	public void setWorking(boolean elevatorWorking) {
		this.elevatorWorking = elevatorWorking;
		if(gui != null) {
			setChanged(); 
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_WORKING)); 
		}
	}

	/**
	 * set gui to gui reference
	 */
	public void addGUI(GUIApplication GUI) {
		gui = GUI;
		this.addObserver(gui);
		setChanged();
		notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_FLOOR));
		setChanged(); 
		notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_WORKING)); 
		setChanged(); 
		notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_DIRECTION)); 
		setChanged(); 
		notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_FLOORSTOVISIT)); 
	}

	/**
	 * Returns if elevator is working or not (fault detector)
	 * 
	 * @return elevatorWorking
	 */
	public boolean isWorking() {
		return elevatorWorking;
	}
	
	/**
	 * reset elevator status
	 */
	public void resetStatus() {
		setElevatorFloor(STARTING_FLOOR[id - 1]);
		setElevatorDirection(StringData.IDLE); 
			floorsToVisit.clear();	// TODO notify change of this somehow
			setWorking(true);
		setElevatorStarted(false);
	}

	/**
	 * Checks if a request at the specified floor in the specified direction exists
	 * and if so, removes it from this list and passes the data
	 * 
	 * @param floor     is the floor of the elevator being checked
	 * @param direction is the direction of the elevator being checked
	 * @return list of pickup requests that match the parameters
	 */
	public ArrayList<RequestData> containsPickUp(int floor, StringData direction) {
		ArrayList<RequestData> temp = new ArrayList<RequestData>();
		synchronized (floorsToVisit) {
			Iterator<RequestData> itr = floorsToVisit.iterator();

			/*
			 * Checks each request if it matches. If so, add to list and remove from floors
			 * to visit of this elevator
			 */
			while (itr.hasNext()) {
				RequestData req = itr.next();
				if (req.getFloorToService() == floor && req.getDirectionTraveling() == direction
						&& req.getType() == StringData.PICK_UP) {
					temp.add(req);
					itr.remove();
				}
			}
		}
		return temp;
	}

	/**
	 * Returns list of floors to visit
	 * 
	 * @return string representing list of floors to visit
	 */
	public String requestsToString() {
		String working = "working :)";
		if (!elevatorWorking) {
			working = "broken :(";
		}
		String str = "Car #" + id + ": currently on floor " + elevatorFloor + "\n\tDirection = " + elevatorDirection
				+ " & " + working;
		synchronized (floorsToVisit) {
			if (!floorsToVisit.isEmpty()) {
				for (RequestData req : floorsToVisit) {
					str += "\n\t|" + req.toString() + "|";
				}
			}
		}

		return str;
	}
}
