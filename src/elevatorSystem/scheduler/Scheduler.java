package elevatorSystem.scheduler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;
import java.util.Scanner;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.Output;
import elevatorSystem.miscellaneous.PerformanceCalculator;
import elevatorSystem.miscellaneous.Port;
import elevatorSystem.miscellaneous.Speed;
import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.communication.ElevatorListener;
import elevatorSystem.scheduler.communication.FloorListener;
import elevatorSystem.scheduler.communication.SendToElevator;
import elevatorSystem.scheduler.communication.SendToFloor;
import elevatorSystem.scheduler.helpers.ElevatorStatus;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.PickUp;
import elevatorSystem.scheduler.helpers.RequestData;
import elevatorSystem.scheduler.helpers.VariableChangedCode;
import elevatorSystem.scheduler.states.SelectElevator;

/**
 * Configures the Scheduler Subsystem of the Elevator System
 * 
 * @author L4G3
 * @version 2.0
 */
public class Scheduler extends Observable implements Runnable {

	/**
	 * Represents iteration of running system (for determining when to notify of
	 * completion)
	 */
	private int ITERATION = 0;
	
	/**
	 * Represent current iteration no
	 */
	private int CURRENT_ITERATION = 0;

	/**
	 * Represents which floor each elevator is on
	 */
	private ElevatorStatus[] elevators;

	/**
	 * Sender to Floor Subsystem from Scheduler Subsystem (have 4 senders because at
	 * most, 4 cars can arrive at a floor at a time
	 */
	private SendToFloor[] floorSender;

	/**
	 * Sender to Elevator Subsystem from Scheduler Subsystem
	 */
	private SendToElevator[] elevatorSender;

	/**
	 * Thread to listen for requests to be received by Scheduler Subsystem from
	 * Floor Subsystem
	 */
	private FloorListener floorListener;

	/**
	 * Thread to listen for requests to be received by Scheduler Subsystem from
	 * Elevator Subsystem 1
	 */
	private ElevatorListener elevatorListener1;

	/**
	 * Thread to listen for requests to be received by Scheduler Subsystem from
	 * Elevator Subsystem 2
	 */
	private ElevatorListener elevatorListener2;

	/**
	 * Thread to listen for requests to be received by Scheduler Subsystem from
	 * Elevator Subsystem 3
	 */
	private ElevatorListener elevatorListener3;

	/**
	 * Thread to listen for requests to be received by Scheduler Subsystem from
	 * Elevator Subsystem 4
	 */
	private ElevatorListener elevatorListener4;

	/**
	 * List of elevator IDS
	 */
	private int[] elevatorIDs;

	/**
	 * List of elevator IDS
	 */
	private static final int NUM_ELEVATORS = IntData.NUM_ELEVATORS.getNum();

	/**
	 * Reference to object used to print summary to output file
	 */
	private Output output;

	/**
	 * Indicate if simulation is done
	 */
	private Boolean simDone;

	/**
	 * Queue of all the pending requests
	 */
	private Queue<PickUp> pendingRequests = new LinkedList<PickUp>();

	/**
	 * Performance calculator of the Arrival Sensors Interface
	 */
	private PerformanceCalculator arrivalSensorsInterfaceCalculator;

	/**
	 * Performance calculator of the Elevator Buttons Interface
	 */
	private PerformanceCalculator elevatorButtonsInterfaceCalculator;

	/**
	 * Performance calculator of the Floor Buttons Interface
	 */
	private PerformanceCalculator floorButtonsInterfaceCalculator;

	/**
	 * Reference to GUI of system
	 */
	private GUIApplication GUI = null;
	/**
	 * Performance calculator of the Floor Buttons Interface
	 */
	private PerformanceCalculator dropOffCalculator;

	/**
	 * Key for request data to formulate keys for monitoring time of pickup to drop
	 * off
	 */
	private int key = 0;
	
	/**
	 * Speed reference
	 */
	private Speed speed;

	/**
	 * Construct new scheduler. Consists of performance calculators, reference to
	 * the output generator, and an elevator Status object, Listener for each
	 * elevator subsystem and a listener for the floor subsystem
	 * 
	 * @param out is the reference to the output writer
	 */
	public Scheduler(Output out, Speed s) {
		output = out;
		speed = s;
		simDone = false;
		arrivalSensorsInterfaceCalculator = new PerformanceCalculator("ARRIVAL SENSORS INTERFACE", output,
				"text_files/arrivalSensors.txt");
		elevatorButtonsInterfaceCalculator = new PerformanceCalculator("ELEVATOR BUTTONS INTERFACE", output,
				"text_files/elevatorButtons.txt");
		floorButtonsInterfaceCalculator = new PerformanceCalculator("FLOOR BUTTONS INTERFACE", output,
				"text_files/floorButtons.txt");
		dropOffCalculator = new PerformanceCalculator("DROP OFF", output, "text_files/dropOffs.txt", true);
		elevatorIDs = new int[NUM_ELEVATORS];
		elevators = new ElevatorStatus[NUM_ELEVATORS];
		elevatorSender = new SendToElevator[NUM_ELEVATORS];
		floorSender = new SendToFloor[NUM_ELEVATORS];
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevatorIDs[i] = i + 1;
			elevators[i] = new ElevatorStatus(i + 1);
		}

		/*
		 * Send to floorSubsystems
		 */
		floorSender[0] = new SendToFloor(Port.F_SCHEDULER_LISTENER1.getPort(),speed);
		floorSender[1] = new SendToFloor(Port.F_SCHEDULER_LISTENER2.getPort(),speed);
		floorSender[2] = new SendToFloor(Port.F_SCHEDULER_LISTENER3.getPort(),speed);
		floorSender[3] = new SendToFloor(Port.F_SCHEDULER_LISTENER4.getPort(),speed);

		/*
		 * Send to elevators
		 */
		elevatorSender[0] = new SendToElevator(Port.E_SCHEDULER_LISTENER1.getPort(), speed);
		elevatorSender[1] = new SendToElevator(Port.E_SCHEDULER_LISTENER2.getPort(), speed);
		elevatorSender[2] = new SendToElevator(Port.E_SCHEDULER_LISTENER3.getPort(), speed);
		elevatorSender[3] = new SendToElevator(Port.E_SCHEDULER_LISTENER4.getPort(), speed);

		/*
		 * Everyone start listening
		 */
		floorListener = new FloorListener(this);
		floorListener.start();
		elevatorListener1 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER1.getPort());
		elevatorListener1.start();
		elevatorListener2 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER2.getPort());
		elevatorListener2.start();
		elevatorListener3 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER3.getPort());
		elevatorListener3.start();
		elevatorListener4 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER4.getPort());
		elevatorListener4.start();

		new Thread(this).start();
	}

	/**
	 * Construct new scheduler. Consists of performance calculators, reference to
	 * the output generator, and an elevator Status object, Listener for each
	 * elevator subsystem and a listener for the floor subsystem. This one is for
	 * the GUI
	 * 
	 * @param out is the reference to the output writer
	 * @param gui is the reference to the gui
	 */
	public Scheduler(Output out, GUIApplication gui, Speed s) {
		output = out;
		speed = s;
		simDone = false;
		arrivalSensorsInterfaceCalculator = new PerformanceCalculator("ARRIVAL SENSORS INTERFACE", output,
				"text_files/arrivalSensors.txt");
		elevatorButtonsInterfaceCalculator = new PerformanceCalculator("ELEVATOR BUTTONS INTERFACE", output,
				"text_files/elevatorButtons.txt");
		floorButtonsInterfaceCalculator = new PerformanceCalculator("FLOOR BUTTONS INTERFACE", output,
				"text_files/floorButtons.txt");
		dropOffCalculator = new PerformanceCalculator("DROP OFF", output, "text_files/dropOffs.txt", true);
		elevatorIDs = new int[NUM_ELEVATORS];
		elevators = new ElevatorStatus[NUM_ELEVATORS];
		elevatorSender = new SendToElevator[NUM_ELEVATORS];
		floorSender = new SendToFloor[NUM_ELEVATORS];
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevatorIDs[i] = i + 1;
			elevators[i] = new ElevatorStatus(i + 1);
		}

		GUI = gui;
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevators[i].addGUI(GUI);
		}

		/*
		 * Send to floorSubsystems
		 */
		floorSender[0] = new SendToFloor(Port.F_SCHEDULER_LISTENER1.getPort(),speed);
		floorSender[1] = new SendToFloor(Port.F_SCHEDULER_LISTENER2.getPort(),speed);
		floorSender[2] = new SendToFloor(Port.F_SCHEDULER_LISTENER3.getPort(),speed);
		floorSender[3] = new SendToFloor(Port.F_SCHEDULER_LISTENER4.getPort(),speed);

		/*
		 * Send to elevators
		 */
		elevatorSender[0] = new SendToElevator(Port.E_SCHEDULER_LISTENER1.getPort(),speed);
		elevatorSender[1] = new SendToElevator(Port.E_SCHEDULER_LISTENER2.getPort(),speed);
		elevatorSender[2] = new SendToElevator(Port.E_SCHEDULER_LISTENER3.getPort(),speed);
		elevatorSender[3] = new SendToElevator(Port.E_SCHEDULER_LISTENER4.getPort(),speed);

		/*
		 * Everyone start listenting
		 */
		floorListener = new FloorListener(this);
		floorListener.start();
		elevatorListener1 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER1.getPort());
		elevatorListener1.start();
		elevatorListener2 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER2.getPort());
		elevatorListener2.start();
		elevatorListener3 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER3.getPort());
		elevatorListener3.start();
		elevatorListener4 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER4.getPort());
		elevatorListener4.start();

		this.addObserver(GUI);
		new Thread(this).start();
	}

	/**
	 * Construct new scheduler. Consists of performance calculators, reference to
	 * the output generator, and an elevator Status object, Listener for each
	 * elevator subsystem aand a listener for the floor subsystem
	 * 
	 * @param out        is a reference to the output writer
	 * @param fAddress   is the address of the floor subsystem
	 * @param eAddresses are the addresses of the 4 elevator subsystems
	 */
	public Scheduler(Output out, InetAddress fAddress, InetAddress[] eAddresses, Speed s) {
		output = out;
		speed = s;
		simDone = false;
		arrivalSensorsInterfaceCalculator = new PerformanceCalculator("ARRIVAL SENSORS INTERFACE", output,
				"text_files/arrivalSensors.txt");
		elevatorButtonsInterfaceCalculator = new PerformanceCalculator("ELEVATOR BUTTONS INTERFACE", output,
				"text_files/elevatorButtons.txt");
		floorButtonsInterfaceCalculator = new PerformanceCalculator("FLOOR BUTTONS INTERFACE", output,
				"text_files/floorButtons.txt");
		dropOffCalculator = new PerformanceCalculator("DROP OFF", output, "text_files/dropOffs.txt", true);
		elevatorIDs = new int[NUM_ELEVATORS];
		elevators = new ElevatorStatus[NUM_ELEVATORS];
		elevatorSender = new SendToElevator[NUM_ELEVATORS];
		floorSender = new SendToFloor[NUM_ELEVATORS];
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevatorIDs[i] = i + 1;
			elevators[i] = new ElevatorStatus(i + 1);
		}
		/*
		 * Send to floorSubsystems
		 */
		floorSender[0] = new SendToFloor(Port.F_SCHEDULER_LISTENER1.getPort(), fAddress,speed);
		floorSender[1] = new SendToFloor(Port.F_SCHEDULER_LISTENER2.getPort(), fAddress,speed);
		floorSender[2] = new SendToFloor(Port.F_SCHEDULER_LISTENER3.getPort(), fAddress,speed);
		floorSender[3] = new SendToFloor(Port.F_SCHEDULER_LISTENER4.getPort(), fAddress,speed);

		/*
		 * Send to elevators
		 */
		elevatorSender[0] = new SendToElevator(Port.E_SCHEDULER_LISTENER1.getPort(), eAddresses[0], speed);
		elevatorSender[1] = new SendToElevator(Port.E_SCHEDULER_LISTENER2.getPort(), eAddresses[1], speed);
		elevatorSender[2] = new SendToElevator(Port.E_SCHEDULER_LISTENER3.getPort(), eAddresses[2], speed);
		elevatorSender[3] = new SendToElevator(Port.E_SCHEDULER_LISTENER4.getPort(), eAddresses[3], speed);

		/*
		 * Everyone start listening
		 */
		floorListener = new FloorListener(this);
		floorListener.start();
		elevatorListener1 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER1.getPort());
		elevatorListener1.start();
		elevatorListener2 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER2.getPort());
		elevatorListener2.start();
		elevatorListener3 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER3.getPort());
		elevatorListener3.start();
		elevatorListener4 = new ElevatorListener(this, Port.S_ELEVATOR_LISTENER4.getPort());
		elevatorListener4.start();

		new Thread(this).start();
	}

	/**
	 * gets the reference to the floorSender
	 * 
	 * @return floorSender
	 */
	public SendToFloor getFloorSender(int id) {
		return floorSender[id - 1];
	}

	/**
	 * gets the reference to the elevatorSender
	 * 
	 * @return elevatorSender
	 */
	public SendToElevator[] getElevatorSenders() {
		return elevatorSender;
	}

	/**
	 * gets the elevators in the system's statuses
	 * 
	 * @return all elevators
	 */
	public ElevatorStatus[] getElevators() {
		return elevators;
	}

	/**
	 * Get the elevators in the system that currently work
	 * 
	 * @return elevators that currently work
	 */
	public ArrayList<ElevatorStatus> getWorkingElevators() {
		ArrayList<ElevatorStatus> temp = new ArrayList<ElevatorStatus>();
		for (ElevatorStatus e : elevators) {
			synchronized (e) {
				if (e.isWorking()) {
					temp.add(e);
				}
			}
		}
		return temp;
	}

	/**
	 * Gets the ids of all elevators
	 * 
	 * @return list of elevator ids
	 */
	public int[] getElevatorIDs() {
		return elevatorIDs;
	}

	/**
	 * Gets a reference to the output to file generator
	 * 
	 * @return output object reference
	 */
	public Output getOutput() {
		return output;

	}

	/**
	 * @return reference to GUI
	 */
	public GUIApplication getGUI() {
		return GUI;
	}

	/**
	 * Adds request to the pending queue of requests
	 * 
	 * @param data is to be decoded into a request to add to the queue
	 */
	public void addRequest(byte[] data) {
		int len = 0;
		int TEMP_LENGTH = Header.LENGTH.getLength();
		int index = TEMP_LENGTH;
		StringData direction = StringData.IDLE;
		int fault;
		int floorRequested;
		int destination;

		// Find out the floor number that request occurred at
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		floorRequested = Integer.valueOf(new String(data, index, len)); // Find floor num

		// Find out the direction that request occurred at
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}

		String temp = new String(data, index, len);
		if (temp.equals(StringData.UP.getString())) {
			direction = StringData.UP;
		} else if (temp.equals(StringData.DOWN.getString())) {
			direction = StringData.DOWN;
		}

		// Find out destination that request wants
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}

		destination = Integer.valueOf(new String(data, index, len));

		// Find out the fault.
		index += len + 1;
		len = 0;
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		fault = Integer.valueOf(new String(data, index, len));

		PickUp reqToAdd = new PickUp(floorRequested, direction, destination, fault, ++key);
		// Create the pickup request and enqueue into queue.
		synchronized (pendingRequests) {

			pendingRequests.add(reqToAdd);
			
		}

		print();
		
		System.out.println("Scheduler: Request added");

		Long startTime = System.nanoTime();
		dropOffCalculator.addStartTime(reqToAdd.getKey(), startTime);
	}

	/**
	 * Add redistributed requests from the fault handler
	 * 
	 * @param lostRequests   is the list of requests to be redistributed by the
	 *                       scheduler. Pass in only pickup requests if transient
	 *                       fault, otherwise pass in both
	 * @param transientFault is true when indicating that the lamp in the floor
	 *                       subsystem needs to be turned back on (current car
	 *                       failed to close doors so everyone gets out onto this
	 *                       floor)
	 * @param floorFailed    is the floor that the elevator failed at
	 * @param direction      direction the elevator was going in
	 * @param id             of elevator that failed (to use its sender)
	 */
	public void redistributeRequests(ArrayList<RequestData> lostRequests, boolean transientFault, int floorFailed,
			StringData direction, int ID) {
		/*
		 * Create the pickup requests and enqueue into queue. Note that all requests
		 * from here on out have no faults
		 */
		synchronized (pendingRequests) {
			for (RequestData request : lostRequests) {
				if (request.getType() == StringData.DROP_OFF) {
					enqueuePendingRequests(new PickUp(floorFailed, request.getDirectionTraveling(), request.getFloorToService(),
							1, request.getKey()));
				} else {
					enqueuePendingRequests(new PickUp(request.getFloorToService(), request.getDirectionTraveling(),
							request.getDestinationFloor(), 1, request.getKey()));
				}
			}
		}
		/*
		 * If not a transient fault, aka the doors won't close and everyone can get out
		 * at the failed floor, notify floor subsystem
		 */
		if (transientFault) {
			/**
			 * Outgoing trailer of packet for ServiceRequest processor
			 */
			byte[] ZERO_PARSER = Header.ZERO.getHeader();
			byte[] HEADER = Header.TRANSIENT_FAULT.getHeader();

			/*
			 * Packet that is being sent to the floorSubsystem. NonTransientFaultHandler is
			 * expecting NON_TRANSIENT_FAULT_HEADER, floorNumber, 0, direction, 0
			 */
			byte[] floorData = ("" + floorFailed).getBytes();
			byte[] dirStatus = direction.getString().getBytes();
			int index = 0;
			byte[] msg = new byte[floorData.length + HEADER.length + dirStatus.length + ZERO_PARSER.length];
			System.arraycopy(HEADER, 0, msg, index, HEADER.length);
			index += HEADER.length;
			System.arraycopy(floorData, 0, msg, index, floorData.length);
			index += floorData.length;
			System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);
			index += ZERO_PARSER.length;
			System.arraycopy(dirStatus, 0, msg, index, dirStatus.length);
			floorSender[ID - 1].send(msg);
		}
	}

	/**
	 * Get the pending request queue
	 * 
	 * @return pending request queue
	 */
	public Queue<PickUp> getPendingRequests() {
		return pendingRequests;

	}

	/**
	 * Get reference to speed
	 * 
	 * @return speed
	 */
	public Speed getSpeed() {
		return speed;
	}
	
	/**
	 * Enqueue to pending request queue from select elevator
	 */
	public void enqueuePendingRequests(PickUp r) {
		synchronized (pendingRequests) {
			pendingRequests.add(r);
			print();
		}
	}

	/**
	 * element dequeued notification
	 */
	public void notifyDequeue() {
		print();
	}

	/**
	 * print to GUI screen;
	 */
	private void print() {
		if (GUI != null) {
			String s = "";
			synchronized(pendingRequests){
				for(PickUp r : pendingRequests) {
					s += "\t[" + r.toString() + "]";
				}
			}
			setChanged();
			notifyObservers(new GUIPacket(this, VariableChangedCode.ELEVATOR_SCHEDULERLIST,s));
		}
	}

	/**
	 * @return string representing the print out of info of all elevators
	 */
	public String elevatorsToString() {
		String str = "\n\n\tELEVATORS\n**************************************\n";
		for (ElevatorStatus e : elevators) {
			synchronized (e) {
				str += e.requestsToString() + "\n";
			}
		}
		str += "**************************************\n";
		return str;
	}

	/**
	 * Checks if a request at the specified floor in the specified direction exists
	 * and if so, removes it from this list and passes the data
	 * 
	 * @param floor     is the floor of the elevator being checked
	 * @param direction is the direction of the elevator being checked
	 * @return list of pickup requests that match the parameters
	 */
	public ArrayList<RequestData> containsPickUp(int floor, StringData direction) {
		ArrayList<RequestData> temp = new ArrayList<RequestData>();
		synchronized (pendingRequests) {
			Iterator<PickUp> itr = pendingRequests.iterator();

			/*
			 * Checks each request if it matches. If so, add to list and remove from floors
			 * to visit of this elevator
			 */
			while (itr.hasNext()) {
				RequestData req = itr.next();
				if (req.getFloorToService() == floor && req.getDirectionTraveling() == direction) {
					temp.add(req);
					itr.remove();
					notifyDequeue();
				}
			}
		}
		return temp;
	}

	/**
	 * @return performance calculator for the Arrival Sensors Interface
	 */
	public PerformanceCalculator getArrivalSensorsInterfaceCalc() {
		return arrivalSensorsInterfaceCalculator;
	}

	/**
	 * @return performance calculator for the Arrival Sensors Interface
	 */
	public PerformanceCalculator getElevatorButtonsInterfaceCalc() {
		return elevatorButtonsInterfaceCalculator;
	}

	/**
	 * @return performance calculator for the Arrival Sensors Interface
	 */
	public PerformanceCalculator getFloorButtonsInterfaceCalc() {
		return floorButtonsInterfaceCalculator;
	}

	/**
	 * @return performance calculator for the Arrival Sensors Interface
	 */
	public PerformanceCalculator getDropOffCalc() {
		return dropOffCalculator;
	}

	/**
	 * @param value to set if simulation done
	 */
	public synchronized void setSimDone(Boolean set) {
		simDone = set;
		if (GUI != null && ITERATION == CURRENT_ITERATION) {
			ITERATION++;
			setChanged();
			notifyObservers(simDone);
		}
	}
	
	/**
	 * @return iteration #
	 */
	public int getIteration() {
		return CURRENT_ITERATION;
	}

	/**
	 * @return true if simulation done
	 */
	public boolean getSimDone() {
		return simDone;
	}

	/**
	 * Gets the next key for a request
	 * 
	 * @return next key
	 */
	public synchronized Integer nextKey() {
		return key++;
	}

	/**
	 * Reset scheduler subsystem
	 */
	public void reset() {
		CURRENT_ITERATION++;
		ITERATION = CURRENT_ITERATION;
		pendingRequests.clear();
		for (int i = 0; i < NUM_ELEVATORS; i++) {
			elevators[i].resetStatus();
		}
		pendingRequests.clear();// clear again just in case
		simDone = false;
	}

	/**
	 * Constantly buffering to pending requests queue and then allowing the requests
	 * to be placed in appropriate elevator floors to visit lists
	 */
	public void run() {
		while (true) {

			try {
				Thread.sleep(IntData.BUFFER_TIME.getTime(speed));
			} catch (Exception e) {
				System.out.println(e);
			}

			String str = "\n><><><><><><><><\n";
			str += "Buffer contains:\n";
			for (PickUp r : pendingRequests) {
				str += r.toString() + "\n";
			}
			str += "><><><><><><><><\n";

			synchronized (pendingRequests) {
				if (!pendingRequests.isEmpty()) {
					System.out.println(str);
					SelectElevator pickElevator = new SelectElevator(this);
					pickElevator.run();
				}
			}
		}
	}

	/**
	 * MAIN FUNCTION TO RUN ON COMPUTER IN LAB
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("***SCHEDULER SUBSYSTEM***\n\n");
		String[] elevatorSubsystemInput = new String[4];
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the IP address of Elevator Subsystem 1: ");
		elevatorSubsystemInput[0] = scanner.nextLine();
		System.out.println("Enter the IP address of Elevator Subsystem 2: ");
		elevatorSubsystemInput[1] = scanner.nextLine();
		System.out.println("Enter the IP address of Elevator Subsystem 3: ");
		elevatorSubsystemInput[2] = scanner.nextLine();
		System.out.println("Enter the IP address of Elevator Subsystem 4: ");
		elevatorSubsystemInput[3] = scanner.nextLine();
		System.out.println("Enter the IP address of the Floor Subsystem: ");
		String floorSubsystemInput = scanner.nextLine();
		InetAddress[] elevatorAddress = new InetAddress[4];
		InetAddress floorAddress = null;
		try {
			elevatorAddress[0] = InetAddress.getByName(elevatorSubsystemInput[0]);
			elevatorAddress[1] = InetAddress.getByName(elevatorSubsystemInput[1]);
			elevatorAddress[2] = InetAddress.getByName(elevatorSubsystemInput[2]);
			elevatorAddress[3] = InetAddress.getByName(elevatorSubsystemInput[3]);
			floorAddress = InetAddress.getByName(floorSubsystemInput);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		scanner.close();
		Speed speed = new Speed();
		Scheduler s = new Scheduler(new Output("text_files/output.txt"), floorAddress, elevatorAddress, speed);
		s.toString(); // gets rid of warning... serves no purpose lol
	}
}
