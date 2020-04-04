package elevatorSystem.elevatorSubsystem3.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import elevatorSystem.elevatorSubsystem3.ElevatorSubsystem;

/**
 * Listens for requests to be sent from Scheduler Subsystem to Elevator
 * Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class SchedulerListener extends Thread {

	/**
	 * Socket to receive packets from Scheduler Subsystem
	 */
	private DatagramSocket receiveSocket;

	/**
	 * Elevator's Scheduler-Listener's port number
	 */
	private int PORT; 

	/**
	 * Reference to ElevatorSubsystem
	 */
	private ElevatorSubsystem elevatorSubsystem;
	
	/**
	 * Creates new Scheduler Listener
	 * @param elevs is a reference to ElevatorSubsystem's elevators 
	 */
	public SchedulerListener(ElevatorSubsystem elev, int port) {
		elevatorSubsystem = elev;
		PORT = port; 
		try {
			receiveSocket = new DatagramSocket(PORT);
		} catch (Exception e) { // Throw exception if failed to create socket at specified port
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Listens for request to be sent from the Scheduler to the Elevator. Notifies
	 * dispatcher that a request has been received
	 */
	public void run() {
		
		while (true) {	// listens for requests forever (until controller is terminated)
			
			byte receiveData[] = new byte[100]; // Create byte array to store data received on socket
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			/*
			 * Listens for messages from Scheduler Subsystem
			 */
			try {
				System.out.println("Elevator: waiting for message from scheduler...");
				receiveSocket.receive(receivePacket); // receive on socket
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			/*
			 * Output to ensure data received
			 */
			System.out.print("\nElevator: Scheduler data received:\n\tbytes: ");
			for (int j = 0; j < receivePacket.getLength(); j++) {
				System.out.print(" " + receiveData[j]);
			}
			System.out.println("\n\tString: " + new String(receivePacket.getData(), 0, receivePacket.getLength()));

			/*
			 * Notifies observer dispatcher that a request has been received
			 */
			Dispatcher d = new Dispatcher(elevatorSubsystem,receiveData);
			d.start();
		}
	}
}
