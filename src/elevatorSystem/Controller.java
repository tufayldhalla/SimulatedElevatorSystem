package elevatorSystem;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.floorSubsystem.*;
import elevatorSystem.miscellaneous.Output;
import elevatorSystem.miscellaneous.Speed;
import elevatorSystem.scheduler.*;

/**
 * Configures the three subsystems. In future, will need to run each system on
 * each computer, individually
 * 
 * @author L4G3
 * @version 2.0
 */
public class Controller {

	/**
	 * User specified input file for simulating elevator requests
	 */
	private static final String IFILENAME = "text_files/input.txt";

	/**
	 * User specified input file for simulating elevator requests
	 */
	private static final String OFILENAME = "text_files/output.txt";

	
	/**
	 * floor
	 */
	private FloorSubsystem floor;
	
	/**
	 * Scheduler
	 */
	private Scheduler scheduler;
	
	/**
	 * Speed control
	 */
	private Speed speed;
	
	/**
	 * Elevators
	 */
	private elevatorSystem.elevatorSubsystem1.ElevatorSubsystem elevator1;
	private elevatorSystem.elevatorSubsystem2.ElevatorSubsystem elevator2;
	private elevatorSystem.elevatorSubsystem3.ElevatorSubsystem elevator3;
	private elevatorSystem.elevatorSubsystem4.ElevatorSubsystem elevator4;
	
	/**
	 * Start system via GUI
	 */
	public Controller(GUIApplication gui) {
		// Configure output
		Output out = new Output(OFILENAME);
		
		speed = new Speed();

		/*
		 * Configure subsystems
		 */
		elevator1 = new elevatorSystem.elevatorSubsystem1.ElevatorSubsystem(
				out, gui, speed);
		elevator2 = new elevatorSystem.elevatorSubsystem2.ElevatorSubsystem(
				out, gui, speed);
		elevator3 = new elevatorSystem.elevatorSubsystem3.ElevatorSubsystem(
				out, gui, speed);
		elevator4 = new elevatorSystem.elevatorSubsystem4.ElevatorSubsystem(
				out, gui, speed);

		scheduler = new Scheduler(out, gui, speed);

		floor = new FloorSubsystem(IFILENAME, out, gui, speed);

		/*
		 * Useless operations to get rid of warnings
		 */
		elevator1.toString();
		elevator2.toString();
		elevator3.toString();
		elevator4.toString();
		scheduler.toString();
		floor.toString();

	}
	
	public Controller(GUIApplication gui, boolean reset) {
		// Configure output
		Output out = new Output(OFILENAME);


		speed = new Speed();

		/*
		 * Configure subsystems
		 */
		elevator1 = new elevatorSystem.elevatorSubsystem1.ElevatorSubsystem(
				out, gui, speed);
		elevator2 = new elevatorSystem.elevatorSubsystem2.ElevatorSubsystem(
				out, gui, speed);
		elevator3 = new elevatorSystem.elevatorSubsystem3.ElevatorSubsystem(
				out, gui, speed);
		elevator4 = new elevatorSystem.elevatorSubsystem4.ElevatorSubsystem(
				out, gui, speed);

		scheduler = new Scheduler(out, gui, speed);
		floor = new FloorSubsystem(out, gui, speed);

		/*
		 * Useless operations to get rid of warnings
		 */
		elevator1.toString();
		elevator2.toString();
		elevator3.toString();
		elevator4.toString();
		scheduler.toString();
		floor.toString();

	}
	
	/**
	 * Create new temp input file for testing
	 */
	public void newInputFile(String fileName, String data) {
		floor.getInputReader().createNewFile(fileName, data);
	}
	
	/**
	 * Reset system for new input
	 * @param newInputFile
	 */
	public void resetInput(String newInputFile, double s) {
		speed.setSpeed(s);
		elevator1.reset();
		elevator2.reset();
		elevator3.reset();
		elevator4.reset();
		scheduler.reset();
		floor.getInputReader().resetInput(newInputFile);
	}

	/**
	 * Starts the Elevator System
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Configure output
		Output out = new Output(OFILENAME);
		Speed speed = new Speed();
		
		/*
		 * Configure subsystems
		 */
		elevatorSystem.elevatorSubsystem1.ElevatorSubsystem elevator1 = new elevatorSystem.elevatorSubsystem1.ElevatorSubsystem(
				out, speed);
		elevatorSystem.elevatorSubsystem2.ElevatorSubsystem elevator2 = new elevatorSystem.elevatorSubsystem2.ElevatorSubsystem(
				out, speed);
		elevatorSystem.elevatorSubsystem3.ElevatorSubsystem elevator3 = new elevatorSystem.elevatorSubsystem3.ElevatorSubsystem(
				out, speed);
		elevatorSystem.elevatorSubsystem4.ElevatorSubsystem elevator4 = new elevatorSystem.elevatorSubsystem4.ElevatorSubsystem(
				out, speed);

		Scheduler scheduler = new Scheduler(out, speed);

		FloorSubsystem floor = new FloorSubsystem(IFILENAME, out, speed);

		/*
		 * Useless operations to get rid of warnings
		 */
		elevator1.toString();
		elevator2.toString();
		elevator3.toString();
		elevator4.toString();
		scheduler.toString();
		floor.toString();

	}
}
