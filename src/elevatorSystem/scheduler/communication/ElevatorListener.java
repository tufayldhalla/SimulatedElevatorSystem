package elevatorSystem.scheduler.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import elevatorSystem.miscellaneous.*;
import elevatorSystem.scheduler.Scheduler;

/**
 * Listens for requests to be sent from Elevator Subsystem to Scheduler
 * Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class ElevatorListener extends Thread{

	/**
	 * Socket to receive packets from Elevator Subsystem
	 */
	private DatagramSocket receiveSocket;

	/**
	 * Reference to Scheduler subsystem
	 */
	private Scheduler schedulerSubsystem;
	
	/**
	 * Schedulers's Elevator-Listener's port number
	 */
	private int PORT; 

	/**
	 * Creates a new receive socket with the appropriate port number
	 */
	public ElevatorListener(Scheduler s, int port) {
		schedulerSubsystem = s;
		PORT = port; 
		try {
			receiveSocket = new DatagramSocket(PORT);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Listens for request to be sent from the Elevator Subsystem to the Scheduler
	 * Subsystem. Notifies dispatcher that a request has been received
	 */
	public void run() {
		int id = 0;
		if(PORT == Port.S_ELEVATOR_LISTENER1.getPort()) {
			id = 1;
		}else if(PORT == Port.S_ELEVATOR_LISTENER2.getPort()) {
			id = 2;
		}
		else if(PORT == Port.S_ELEVATOR_LISTENER3.getPort()) {
			id = 3;
		}else if(PORT == Port.S_ELEVATOR_LISTENER4.getPort()) {
			id = 4;
		}
		
		while (true) {
			byte receiveData[] = new byte[100]; // Create byte array to store data received on socket
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			/*
			 * Listens for messages from Scheduler Subsystem
			 */
			try {
				System.out.println("Scheduler: Waiting on message from elevator " + id);
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
			System.out.print("\nScheduler "+ id +": elevator data received\n\tbytes:");
			for (int j = 0; j < receivePacket.getLength(); j++) {
				System.out.print(" " + receiveData[j]);
			}
			System.out.println("\n\tString: " + new String(receivePacket.getData(), 0, receivePacket.getLength()));

			/*
			 * Notifies dispatcher that a request has been received
			 */
			Dispatcher d = new Dispatcher(schedulerSubsystem, receiveData);
			d.start();
		}
	}
}
