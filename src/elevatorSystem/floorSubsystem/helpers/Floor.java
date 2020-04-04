package elevatorSystem.floorSubsystem.helpers;

import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.Status;

/**
 * Floor object within Floor Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class Floor extends Observable{

	/**
	 * Lamp indicating if up floor button has been pressed
	 */
	private Status upRequestLamp;

	/**
	 * Lamp indicating if down floor button has been pressed
	 */
	private Status downRequestLamp;

	/**
	 * Lamp indicating if elevator arriving and going up
	 */
	private Status[] upDirectionLamp;

	/**
	 * Lamp indicating if elevator arriving and going down
	 */
	private Status[] downDirectionLamp;
	
	/**
	 * Reference to GUI of system
	 */
	private GUIApplication GUI;
	
	/**
	 * Floor
	 */
	private int floorNum;

	/**
	 * Creates new floor with lamps all initially off
	 */
	public Floor(int floor) {
		floorNum = floor;
		upRequestLamp = Status.OFF;
		downRequestLamp = Status.OFF;
		upDirectionLamp = new Status[4];
		upDirectionLamp[0] = Status.OFF;
		upDirectionLamp[1] = Status.OFF;
		upDirectionLamp[2] = Status.OFF;
		upDirectionLamp[3] = Status.OFF;
		downDirectionLamp = new Status[4];
		downDirectionLamp[0] = Status.OFF;
		downDirectionLamp[1] = Status.OFF;
		downDirectionLamp[2] = Status.OFF;
		downDirectionLamp[3] = Status.OFF;
	}

	/**
	 * Creates new top or bottom floor with lamps all initially off. Note that the
	 * top floor only has a down request lamp and the bottom floor only has an up
	 * request lamp
	 */
	public Floor(int floor, boolean bottom) {
		floorNum = floor;
		/*
		 * Bottom floor: has no up button 
		 */
		if (bottom) {
			upRequestLamp = Status.DNE;
			downRequestLamp = Status.OFF;
		} 
		/*
		 * Top floor: has no down button
		 */
		else {
			upRequestLamp = Status.OFF;
			downRequestLamp = Status.DNE;
		}
		upDirectionLamp = new Status[4];
		upDirectionLamp[0] = Status.OFF;
		upDirectionLamp[1] = Status.OFF;
		upDirectionLamp[2] = Status.OFF;
		upDirectionLamp[3] = Status.OFF;
		downDirectionLamp = new Status[4];
		downDirectionLamp[0] = Status.OFF;
		downDirectionLamp[1] = Status.OFF;
		downDirectionLamp[2] = Status.OFF;
		downDirectionLamp[3] = Status.OFF;

	}

	/**
	 * Gets the status of the up request lamp of the floor
	 * 
	 * @return upRequestLamp
	 */
	public Status getUpRequestLamp() {
		return upRequestLamp;
	}

	/**
	 * @return floor num
	 */
	public int getFloorNum() {
		return floorNum;
	}
	
	/**
	 * Gets the status of the down request lamp of the floor
	 * 
	 * @return downRequestLamp
	 */
	public Status getDownRequestLamp() {
		return downRequestLamp;
	}

	/**
	 * Gets the status of the up direction lamp of the floor
	 * 
	 * @return upDirectionLamp
	 */
	public Status[] getUpDirectionLamps() {// needed for next iteration
		return upDirectionLamp;
	}

	/**
	 * Gets the status of the down lamp of the floor
	 * 
	 * @return downDirectionLamp
	 */
	public Status[] getDownDirectionLamps() {// needed for next iteration
		return downDirectionLamp;
	}

	/**
	 * Sets the status of the up request lamp of the floor
	 * 
	 * @param status to set lamp to
	 */
	public synchronized void setUpRequestLamp(Status status) {
		upRequestLamp = status;
		setChanged();
		notifyObservers(new GUIPacket_Floor(this,LampChangedCode.REQ_UP));
	}

	/**
	 * Sets the status of the down request lamp of the floor
	 * 
	 * @param status to set lamp to
	 */
	public synchronized void setDownRequestLamp(Status status) {
		downRequestLamp = status;
		setChanged();
		notifyObservers(new GUIPacket_Floor(this,LampChangedCode.REQ_DOWN));
	}

	/**
	 * Add GUI reference
	 * 
	 * @param GUI
	 */
	public void addGUI(GUIApplication gui) {
		GUI = gui;
		this.addObserver(GUI);
	}
	
	/**
	 * Sets the status of the up direction lamp of the floor
	 * 
	 * @param status to set lamp to
	 * @param which car is this associated with
	 */
	public synchronized void setUpDirectionLamp(Status status, int id) {// needed for next iteration
		upDirectionLamp[id-1] = status;
		setChanged();
		notifyObservers(new GUIPacket_Floor(this,LampChangedCode.UP_ARROW, id, status));
	}

	/**
	 * Sets the status of the down direction lamp of the floor
	 * 
	 * @param status to set lamp to
	 *  @param which car is this associated with
	 */
	public synchronized void setDownDirectionLamp(Status status, int id) {// needed for next iteration
		downDirectionLamp[id-1] = status;
		setChanged();
		notifyObservers(new GUIPacket_Floor(this,LampChangedCode.DOWN_ARROW, id, status));
	}

}
