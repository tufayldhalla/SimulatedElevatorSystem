package elevatorSystem.elevatorSubsystem2;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.elevatorSubsystem2.communication.SchedulerListener;
import elevatorSystem.elevatorSubsystem2.communication.SendToScheduler;
import elevatorSystem.elevatorSubsystem2.helpers.Elevator;
import elevatorSystem.miscellaneous.Output;
import elevatorSystem.miscellaneous.Port;
import elevatorSystem.miscellaneous.Speed;

/**
 * Configures the Elevator Subsystem of the Elevator System
 * 
 * @author L4G3
 * @version 2.0
 */
public class ElevatorSubsystem {

	/**
	 * Floor stores the status of the lamps on each floor
	 */
	private Elevator elevator;

	/**
	 * Thread to listen for requests to be received by Elevator Subsystem
	 */
	private SchedulerListener listener;

	/**
	 * Sender to Scheduler Subsystem from Elevator Subsystem
	 */
	private SendToScheduler sender;

	/**
	 * Reference to output object to write to output file
	 */
	private Output output;
	
	/**
	 * Reference to GUI of system
	 */
	private GUIApplication GUI;
	
	/**
	 * Port for this elevator subsystem to receive from scheduler
	 */
	private static final int ListenPort = Port.E_SCHEDULER_LISTENER2.getPort(); 
	
	/**
	 * Port for this elevator subsystem to send to scheduler
	 */
	private static final int SendPort = Port.S_ELEVATOR_LISTENER2.getPort(); 
	
	
	/**
	 * ID of this elevator subsystem 
	 */
	private static final int ID = 2; 	
	
	/**
	 * Speed reference
	 */
	private Speed speed;

	/**
	 * Creates a new Dispatcher and listener... use this for multiple computers
	 * 
	 * @param out              is the output object to write to output file
	 * @param schedulerAddress is the address of the scheduler
	 */
	public ElevatorSubsystem(Output out, InetAddress schedulerAddress, Speed s) {
		output = out;
		speed = s;
		sender = new SendToScheduler(SendPort, schedulerAddress, speed);

		// create elevators
		elevator = new Elevator(sender, ID,s);

		// start listening for requests
		listener = new SchedulerListener(this, ListenPort);
		listener.start();
	}

	/**
	 * Creates a new Dispatcher and listener
	 * 
	 * @param out is the output object to write to output file
	 */
	public ElevatorSubsystem(Output out, Speed s) {
		output = out;
		speed = s;
		sender = new SendToScheduler(SendPort, speed);

		// create elevators
		elevator = new Elevator(sender, ID,s);

		// start listening for requests
		listener = new SchedulerListener(this, ListenPort);
		listener.start();
	}
	
	/**
	 * Creates a new Dispatcher and listener for GUI
	 * 
	 * @param out is the output object to write to output file
	 * @param gui is the GUI reference
	 */
	public ElevatorSubsystem(Output out, GUIApplication gui, Speed s) {
		output = out;
		speed = s;
		sender = new SendToScheduler(SendPort, speed);
		GUI = gui;

		// create elevators
		elevator = new Elevator(sender, ID,s);
		elevator.addGUI(GUI);

		// start listening for requests
		listener = new SchedulerListener(this, ListenPort);
		listener.start();
	}

	/**
	 * Returns reference to the object that sends to the Scheduler's
	 * ElevatorListener socket
	 * 
	 * @return SchedulerSender
	 */
	public SendToScheduler getSender() {
		return sender;
	}

	/**
	 * Returns reference to all elevators in the elevator Subsystem
	 * 
	 * @return elevators
	 */
	public Elevator getElevator() {
		return elevator;
	}

	/**
	 * @return speed
	 */
	public Speed getSpeed() {
		return speed;
		
	}
	/**
	 * Returns reference to the output object that writes to the output file
	 * 
	 * @return output
	 */
	public Output getOutput() {
		return output;
	}
	
	
	/**
	 * Reset elevator subsystem for new iteration of system test
	 */
	public void reset() {
		elevator.reset();
	}
	
	/**
	 * MAIN FUNCTION TO RUN ON COMPUTER IN LAB
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("***ELEVATOR 2 SUBSYSTEM***\n\n");
		Scanner scanner = new Scanner( System.in ); 
		System.out.println("Enter the IP address of the Scheduler: ");
      String input = scanner.nextLine();
      InetAddress address = null;
      try {
			address = InetAddress.getByName(input);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		scanner.close();
		ElevatorSubsystem e = new ElevatorSubsystem(new Output("text_files/output.txt"),address, new Speed());
		e.toString(); // gets rid of warning... serves no purpose lol
	}
}
