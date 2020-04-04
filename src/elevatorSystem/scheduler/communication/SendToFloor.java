package elevatorSystem.scheduler.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Speed;

/**
 * Sends requests from Scheduler Subsystem to Floor Subsystem
 * 
 * @author L4G3
 * @version 2.0
 */
public class SendToFloor {

	/**
	 * Packet to send packets to Elevator Subsystem
	 */
	private DatagramPacket sendPacket;

	/**
	 * Socket to send packets to Elevator Subsystem
	 */
	private DatagramSocket sendSocket;

	/**
	 * Floor's Scheduler-Listener's port number
	 */
	private int PORT;

	/**
	 * Elevator Subsystem's InetAddress
	 */
	private InetAddress floorAddress;

	/**
	 * Reference to speed
	 */
	private Speed speed;

	/**
	 * Creates a new send socket with the appropriate port number
	 * 
	 * @param port is the port to send to
	 */
	public SendToFloor(int port, Speed s) {
		speed = s;
		PORT = port;
		try {
			floorAddress = InetAddress.getLocalHost();
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
	 * Creates a new send socket with the appropriate port number
	 * 
	 * @param port     is the port to send to
	 * @param eAddress is the IP address of the floor subsystem
	 */
	public SendToFloor(int port, InetAddress fAddress, Speed s) {
		speed = s;
		PORT = port;
		floorAddress = fAddress;
		try {
			sendSocket = new DatagramSocket();
		} catch (SocketException se) { // Unable to create the socket.
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Construct packet from byte message and send to Floor Subsystem. Synchronized
	 * so that only one process can send at a time, allowing the receive socket to
	 * handle all requests
	 * 
	 * @param msg is the message to be sent to the Floor Subsystem
	 */
	public synchronized void send(byte[] msg) {

		sendPacket = new DatagramPacket(msg, msg.length, floorAddress, PORT);

		/*
		 * Process the datagram to be sent (print out the information about sending
		 * packet to Floor Subsystem)
		 */
		System.out.print("Scheduler: sends request to Floor Subsystem = \n\tbytes:");
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
