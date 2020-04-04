package elevatorSystem.floorSubsystem.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import elevatorSystem.floorSubsystem.FloorSubsystem;
import elevatorSystem.miscellaneous.Port;

/**
 * Listens for requests to be sent from Scheduler Subsystem to Floor Subsystem
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
	 * Reference to the  Floor Subsystem
	 */
	private FloorSubsystem floorSubsystem;

	/**
	 * Creates a new receive socket with the appropriate port number
	 * 
	 * @param f is the reference to the FloorSubsystem's floors
	 */
	public SchedulerListener(FloorSubsystem f, int port) {
		PORT = port;
		floorSubsystem = f;
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
		int id = 0;
		if(PORT == Port.F_SCHEDULER_LISTENER1.getPort()) {
			id = 1;
		}else if(PORT == Port.F_SCHEDULER_LISTENER2.getPort()) {
			id = 2;
		}
		else if(PORT == Port.F_SCHEDULER_LISTENER3.getPort()) {
			id = 3;
		}else if(PORT == Port.F_SCHEDULER_LISTENER4.getPort()) {
			id = 4;
		}
		while (true) {
			byte receiveData[] = new byte[100]; // Create byte array to store data received on socket
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			/*
			 * Listens for messages from Scheduler Subsystem
			 */
			try {
				System.out.println("Floor: waiting for message from scheduler...");
				receiveSocket.receive(receivePacket); // receive on socket
			} catch (IOException e) {
				System.out.print("IO Exception: likely:");
				System.out.println("Receive Socket Timed Out.\n" + e);
				e.printStackTrace();
				System.exit(1);
			}

			// Output to ensure data received
			System.out.print("\nFloor: Scheduler data received:\n\t");
			for (int j = 0; j < receivePacket.getLength(); j++) {
				System.out.print(" " + receiveData[j]);
			}
			System.out.println("\n\tString: " + new String(receivePacket.getData(), 0, receivePacket.getLength()));

			/*
			 * Notifies dispatcher that a request has been received
			 */
			Dispatcher d = new Dispatcher(floorSubsystem, receiveData, id);
			d.start();
		}
	}
}
