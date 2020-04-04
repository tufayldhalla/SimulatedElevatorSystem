package elevatorSystem.floorSubsystem;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.floorSubsystem.communication.SchedulerListener;
import elevatorSystem.floorSubsystem.communication.SendToScheduler;
import elevatorSystem.floorSubsystem.helpers.Floor;
import elevatorSystem.floorSubsystem.helpers.InputFileReader;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Output;
import elevatorSystem.miscellaneous.Port;
import elevatorSystem.miscellaneous.Speed;

/**
 * Configures the Floor Subsystem of the Elevator System
 * 
 * @author L4G3
 * @version 2.0
 */
public class FloorSubsystem {

	/**
	 * Thread to listen for requests to be received by Floor Subsystem from
	 * Scheduler Subsystem (4 because could have 4 elevators arrive all at once)
	 */
	private SchedulerListener[] listener;

	/**
	 * Number of floors in the building that the elevator exists in
	 */
	private static final int NUM_FLOORS = IntData.NUM_FLOORS.getNum();

	/**
	 * Floor stores the status of the lamps on each floor
	 */
	private Floor[] floors = new Floor[NUM_FLOORS];

	/**
	 * Sender to Scheduler Subsystem from Floor Subsystem
	 */
	private SendToScheduler sender;

	/**
	 * Object used to print summary to output file
	 */
	private Output output;

	/**
	 * Reference to GUI of system
	 */
	private GUIApplication GUI;
	
	/**
	 * Reference to input file reader
	 */
	private InputFileReader reader;
	
	/**
	 * Speed reference
	 */
	private Speed speed;
	
	/**
	 * Creates a new Dispatcher, SchedulerListener and InputFileReader
	 * 
	 * @param filename is the name of the user input file to be read
	 * @param out      Object used to print summary to output file
	 */
	public FloorSubsystem(String filename, Output out, Speed s) {
		speed = s;
		output = out;

		// Initialize the each index of the array to Floor object
		for (int i = 0; i < NUM_FLOORS; i++) {
			if (i == 0) {
				floors[i] = new Floor(i+1,true);
			} else if (i == NUM_FLOORS - 1) {
				floors[i] = new Floor(i+1, false);
			} else {
				floors[i] = new Floor(i+1);
			}
		}

		sender = new SendToScheduler(speed);
		// create Scheduler listeners
		listener = new SchedulerListener[IntData.NUM_ELEVATORS.getNum()];
		listener[0] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER1.getPort());
		listener[1] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER2.getPort());
		listener[2] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER3.getPort());
		listener[3] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER4.getPort());

		// start listening
		listener[0].start();
		listener[1].start();
		listener[2].start();
		listener[3].start();

		// create Input File Reader to simulate user input
		reader = new InputFileReader(this, filename);
		reader.run();

	}

	/**
	 * Creates a new Dispatcher, SchedulerListener and InputFileReader for GUI
	 * 
	 * @param filename is the name of the user input file to be read
	 * @param out      Object used to print summary to output file
	 * @param gui      GUI
	 */
	public FloorSubsystem(String filename, Output out, GUIApplication gui, Speed s) {
		output = out;

		speed = s;
		// Initialize the each index of the array to Floor object
		for (int i = 0; i < NUM_FLOORS; i++) {
			if (i == 0) {
				floors[i] = new Floor(i+1,true);
			} else if (i == NUM_FLOORS - 1) {
				floors[i] = new Floor(i+1,false);
			} else {
				floors[i] = new Floor(i+1);
			}
		}

		GUI = gui;

		for (int i = 0; i < NUM_FLOORS; i++) {
				floors[i].addGUI(GUI);
		}
		
		sender = new SendToScheduler(speed);
		// create Scheduler listeners
		listener = new SchedulerListener[IntData.NUM_ELEVATORS.getNum()];
		listener[0] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER1.getPort());
		listener[1] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER2.getPort());
		listener[2] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER3.getPort());
		listener[3] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER4.getPort());

		// start listening
		listener[0].start();
		listener[1].start();
		listener[2].start();
		listener[3].start();

		// create Input File Reader to simulate user input
		reader = new InputFileReader(this, filename, GUI);
		reader.run();
	}

	/**
	 * Creates a new Dispatcher, SchedulerListener and InputFileReader for GUI
	 * 
	 * @param out      Object used to print summary to output file
	 * @param gui      GUI
	 */
	public FloorSubsystem(Output out, GUIApplication gui, Speed s) {
		output = out;
		speed = s;

		// Initialize the each index of the array to Floor object
		for (int i = 0; i < NUM_FLOORS; i++) {
			if (i == 0) {
				floors[i] = new Floor(i+1,true);
			} else if (i == NUM_FLOORS - 1) {
				floors[i] = new Floor(i+1,false);
			} else {
				floors[i] = new Floor(i+1);
			}
		}

		GUI = gui;

		for (int i = 0; i < NUM_FLOORS; i++) {
				floors[i].addGUI(GUI);
		}
		
		sender = new SendToScheduler(speed);
		// create Scheduler listeners
		listener = new SchedulerListener[IntData.NUM_ELEVATORS.getNum()];
		listener[0] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER1.getPort());
		listener[1] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER2.getPort());
		listener[2] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER3.getPort());
		listener[3] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER4.getPort());

		// start listening
		listener[0].start();
		listener[1].start();
		listener[2].start();
		listener[3].start();

		// create Input File Reader to simulate user input
		reader = new InputFileReader(this, GUI);
		reader.run();
	}
	
	/**
	 * Creates a new Dispatcher, SchedulerListener and InputFileReader for multiple
	 * computers implementation
	 * 
	 * @param filename         is the name of the user input file to be read
	 * @param out              Object used to print summary to output file
	 * @param schedulerAddress is the IP address of the scheduler subsystem
	 */
	public FloorSubsystem(String filename, Output out, InetAddress schedulerAddress, Speed s) {
		speed = s;
		output = out;

		// Initialize the each index of the array to Floor object
		for (int i = 0; i < NUM_FLOORS; i++) {
			if (i == 0) {
				floors[i] = new Floor(i+1,true);
			} else if (i == NUM_FLOORS - 1) {
				floors[i] = new Floor(i+1,false);
			} else {
				floors[i] = new Floor(i+1);
			}
		}

		sender = new SendToScheduler(schedulerAddress, speed);
		// create Scheduler listeners
		listener = new SchedulerListener[IntData.NUM_ELEVATORS.getNum()];
		listener[0] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER1.getPort());
		listener[1] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER2.getPort());
		listener[2] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER3.getPort());
		listener[3] = new SchedulerListener(this, Port.F_SCHEDULER_LISTENER4.getPort());

		// start listening
		listener[0].start();
		listener[1].start();
		listener[2].start();
		listener[3].start();

		// create Input File Reader to simulate user input
		reader = new InputFileReader(this, filename);
		reader.run();

	}

	/**
	 * Returns reference to the SchedulerSender
	 * 
	 * @return sender to Scheduler
	 */
	public SendToScheduler getSender() {
		return sender;
	}

	/**
	 * Retrieves reference to floors of the floorSubsystem
	 * 
	 * @return floors
	 */
	public Floor[] getFloors() {
		return floors;
	}

	/**
	 * Retrieves a reference to the output object to write to the output file
	 */
	public Output getOutput() {
		return output;
	}

	/**
	 * @return reference to speed
	 */
	public Speed getSpeed() {
		return speed;
	}
	
	/**
	 * Retrieves a reference to the input reader
	 */
	public InputFileReader getInputReader() {
		return reader;
	}
	
	/**
	 * MAIN FUNCTION TO RUN ON COMPUTER IN LAB
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("***FLOOR SUBSYSTEM***\n\n");
		System.out.println("Note that the Scheduler subsystem and Elevator Subsystem's should already be runnning\n");
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the IP address of the Scheduler: ");
		String input = scanner.nextLine();
		InetAddress address = null;
		try {
			address = InetAddress.getByName(input);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		scanner.close();
		Speed speed = new Speed();
		FloorSubsystem f = new FloorSubsystem("text_files/input.txt", new Output("text_files/output.txt"), address, speed);
		f.toString(); // gets rid of warning... serves no purpose lol
	}

}
