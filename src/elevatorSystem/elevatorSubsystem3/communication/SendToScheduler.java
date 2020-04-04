package elevatorSystem.elevatorSubsystem3.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Speed;

/**
 * Sends requests from Elevator Subsystem to Scheduler Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class SendToScheduler {

	/**
	 * Packet to send packets to Scheduler Subsystem
	 */
	private DatagramPacket sendPacket;

	/**
	 * Socket to send packets to Scheduler Subsystem
	 */
	private DatagramSocket sendSocket;

	/**
	 * Scheduler's Elevator-Listener's port number
	 */
	private int PORT;
	
	/**
	 * Speed reference
	 */
	private Speed speed;
	

	/**
	 * Scheduler's InetAddress
	 */
	private InetAddress schedulerAddress;
	
	/**
	 * Creates a new send socket with the appropriate port number and IP address
	 */
	public SendToScheduler(int port, InetAddress sAddress, Speed s) {
		PORT = port;
		speed = s;
		schedulerAddress = sAddress;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) { // Unable to create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates a new send socket with the appropriate port number using local host
	 * IP address
	 */
	public SendToScheduler(int port, Speed s) {
		PORT = port;
		speed = s;
		try {
			schedulerAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) { // Unable to create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Construct packet from byte message and send to Scheduler. Synchronized so
	 * that only one process can send at a time, allowing the receive socket to
	 * handle all requests
	 * 
	 * @param msg is the message to be sent to the Scheduler
	 */
	public synchronized void send(byte[] msg) {

		sendPacket = new DatagramPacket(msg, msg.length, schedulerAddress, PORT);

		/*
		 * Process the datagram to be sent (print out the information about sending
		 * packet to scheduler)
		 */
		System.out.print("Elevator: sends request to Scheduler = \n\tbytes:");
		for (int j = 0; j < sendPacket.getLength(); j++) {
			System.out.print(" " + sendPacket.getData()[j]);
		}
		System.out.println("\n\tString: " + new String(sendPacket.getData(), 0, sendPacket.getLength()));

		/*
		 * Sends datagram
		 */
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		/*
		 * slow down before letting other thread try to send to scheduler
		 */
		try {
			Thread.sleep(IntData.SEND_TIME.getTime(speed));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
