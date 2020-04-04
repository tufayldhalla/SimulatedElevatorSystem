package elevatorSystem.elevatorSubsystem2.helpers;

import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.elevatorSubsystem2.communication.SendToScheduler;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Speed;
import elevatorSystem.miscellaneous.Status;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.VariableChangedCode;

/**
 * Represents buttons, motor and doors of an elevator
 *
 * @author L4G3
 * @version 2.0
 */
public class Elevator extends Observable {

	/**
	 * Number of floor buttons in the elevator
	 */
	private static final int NUM_BUTTONS = IntData.NUM_FLOORS.getNum();

	/**
	 * number of button lamps in the elevator
	 */
	private Status[] buttonLamps;

	/**
	 * The motor for the elevator
	 */
	private Status motor;
	
	/**
	 * flag for fault if elevator is not working
	 */
	private boolean working;

	/**
	 * The doors for the elevator
	 */
	private Status doors;

	/**
	 * Sensor for the elevator
	 */
	private FloorSensor sensor;
	
	/**
	 * Reference to GUI of system
	 */
	private GUIApplication GUI;
	
	private int floorNumber;

	/**
	 * Creates new elevator with motor off and doors open to begin.. all lights are
	 * off. Creates new floor sensor.
	 */
	public Elevator(SendToScheduler s, int id, Speed speed) {
		sensor = new FloorSensor(this,s, id, speed);
		sensor.start();
		motor = Status.OFF;
		doors = Status.OPEN;
		working = true;

		floorNumber = IntData.STARTING_FLOOR2.getNum();
		buttonLamps = new Status[NUM_BUTTONS];
		for (int i = 0; i < NUM_BUTTONS; i++) {
			buttonLamps[i] = Status.OFF;
		}
	}

	/**
	 * Returns status of all button lamps
	 * 
	 * @return buttonLamps
	 */
	public Status[] getButtonLamps() {
		return buttonLamps;
	}

	/**
	 * Sets a button lamp for a floor
	 * 
	 * @param floor  is floor button to change
	 * @param status is change to set
	 */
	public synchronized void setButtonLamp(int floor, Status status) {
		this.buttonLamps[floor - 1] = status;
		setChanged();
		notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_CAR_BUTTON, floor));
	
	}

	/**
	 * Return status of motor
	 * 
	 * @return motor
	 */
	public Status getMotor() {
		return motor;
	}

	/**
	 * Sets motor on or off
	 * 
	 * @param motor is change to set
	 */
	public synchronized void setMotor(Status motor) {
		this.motor = motor;
	}

	/**
	 * Returns status of doors (Open or closed)
	 * 
	 * @return doors
	 */
	public Status getDoors() {
		return doors;
	}

	/**
	 * Opens or closes doors
	 * 
	 * @param doors is change to set
	 */
	public synchronized void setDoors(Status doors) {
		if (this.doors != doors) {
			this.doors = doors;
			if (doors == Status.OPEN) {
				setChanged();
				notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_DOORS, floorNumber, true));
			}
		}
	}

	/**
	 * Returns floor sensor
	 * 
	 * @return sensor
	 */
	public FloorSensor getFloorSensor() {
		return sensor;
	}
	
	/**
	 * set gui to gui reference 
	 */
	public void addGUI(GUIApplication gui) {
		GUI = gui;
		this.addObserver(GUI);
	}
	

	/**
	 * Set flag if elevator is working or not
	 * 
	 * @param working true or false if elevator is working
	 */
	public synchronized void setWorking(boolean working) {
		this.working = working;
		if(working == false) {
			sensor.stopSensing();
		}else {
			resetLamps();
		}
	}
	
	/**
	 * Reset lamps
	 */
	private void resetLamps() {
		for (int i = 0; i < NUM_BUTTONS; i++) {
			setButtonLamp(i+1,Status.OFF);
		}
	}
	

	public void setFloor(int floorID){
		floorNumber = floorID;
	}
	
	public int getFloor(){
		return floorNumber;
	}
	
	/**
	 * Reset the elevator
	 */
	public void reset() {
		sensor.stopSensing();
		setMotor(Status.OFF);
		setFloor(IntData.STARTING_FLOOR2.getNum());
		setWorking(true);
		setDoors(Status.OPEN);
	}
	
	/**
	 * Returns if elevator is working or not (fault detector)
	 * 
	 * @return working
	 */
	public boolean isWorking() {
		return working;
	}
}