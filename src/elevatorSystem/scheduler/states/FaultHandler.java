package elevatorSystem.scheduler.states;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.StringData;
import elevatorSystem.scheduler.Scheduler;
import elevatorSystem.scheduler.helpers.ElevatorStatus;
import elevatorSystem.scheduler.helpers.GUIPacket;
import elevatorSystem.scheduler.helpers.RequestData;
import elevatorSystem.scheduler.helpers.VariableChangedCode;

/**
 * CLASS TO DEAL WITH FAULTS (ITERATION 3)
 * 
 * @author L4G3
 *
 */
public class FaultHandler extends Observable implements Runnable{

	/**
	 * Reference to the elevator status of the elevator car that experienced a fault
	 */
	private ElevatorStatus elevatorWithFault;

	/**
	 * ID of the elevator status of the elevator car that experienced a fault
	 */
	private int elevatorWithFaultID;

	/**
	 * Reference to the scheduler subsystem
	 */
	private Scheduler scheduler;

	/**
	 * data received from dispatcher to be decoded
	 */
	private byte[] data;

	/**
	 * data received from dispatcher to be decoded
	 */
	private boolean transientFault;
	
	/**
	 * Reference to GUI
	 */
	private GUIApplication gui = null;

	/**
	 * Length of headers
	 */
	private static final int TEMP_LENGTH = Header.LENGTH.getLength();

	/**
	 * Creates a process to handle faults
	 * 
	 * @param s              is a reference to the scheduler
	 * @param msg            is data to decode
	 * @param faultTransient specifies type of fault
	 */
	public FaultHandler(Scheduler s, byte[] msg, boolean faultTransient) {
		//super("Fault Handler");
		scheduler = s;
		gui = s.getGUI();
		transientFault = faultTransient;
		data = msg;
		if(gui!=null) {
			this.addObserver(gui);
		}
	}

	/**
	 * Creates a process to handle faults
	 * 
	 * @param s              is a reference to the scheduler
	 * @param msg            is data to decode
	 * @param faultTransient specifies type of fault
	 */
	public FaultHandler(Scheduler s, byte[] msg) {
		//super("Fault Handler");
		scheduler = s;
		gui = s.getGUI();
		if(gui!=null) {
			this.addObserver(gui);
		}
		data = msg;
		fix();
	}

	/**
	 * Fix transient fault (add elevator back into working elevators list)
	 */
	private void fix() {
		new Thread() {
			public void run() {
				decodeData();
				String s = "Scheduler: Doors of elevator " + elevatorWithFaultID + " finally closed!";
				System.out.println(s);
				new Thread() {
					public void run() {
						try {
							scheduler.getOutput().write("\n" + s);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}.start();
				synchronized (scheduler.getElevators()[elevatorWithFaultID - 1]) {
					elevatorWithFault = scheduler.getElevators()[elevatorWithFaultID - 1];
					elevatorWithFault.setElevatorDirection(StringData.IDLE);
					elevatorWithFault.setWorking(true);
				}
			}
		}.start();
	}

	/**
	 * Run logic of fault handler
	 */
	public void run() {
		// redistribute requests to other working elevators.
		String s = "Scheduler: Fault occurred";
		System.out.println(s);
		try {
			scheduler.getOutput().write("\n" + s);
		} catch (IOException e) {
			e.printStackTrace();
		}

		decodeData();
		elevatorWithFault = scheduler.getElevators()[elevatorWithFaultID - 1];
		int floorFailed = 0;
		StringData direction;
		ArrayList<RequestData> lostRequests = new ArrayList<RequestData>();

		synchronized (elevatorWithFault) {
			/*
			 * Set elevator as not working and get info about elevator
			 */
			elevatorWithFault.setWorking(false);
			floorFailed = elevatorWithFault.getElevatorFloor();
			direction = elevatorWithFault.getElevatorDirection();

			/*
			 * If NOT a transient fault (stuck between floors), redistribute all pick up
			 * requests (4 arguments)... the 2 argument requests (drop offs) are people
			 * stuck in the car
			 */
			if (!transientFault) {
				Iterator<RequestData> itr = elevatorWithFault.getFloorsToVisit().iterator();
				while (itr.hasNext()) {
					RequestData req = itr.next();
					if (req.getType() == StringData.PICK_UP) {
						lostRequests.add(req);
						itr.remove();
					}
				}
			}
			/*
			 * Transient Fault: Doors won't close! redistribute all requests because all
			 * users can get out
			 */
			else {
				Iterator<RequestData> itr = elevatorWithFault.getFloorsToVisit().iterator();
				while (itr.hasNext()) {
					RequestData req = itr.next();
					lostRequests.add(req);
					itr.remove();
				}
			}
			if (gui != null) {
				setChanged();
				notifyObservers(new GUIPacket(elevatorWithFault, VariableChangedCode.ELEVATOR_FLOORSTOVISIT));
			}
		}

		/*
		 * Add new requests to pending queue for redistribution
		 */
		scheduler.redistributeRequests(lostRequests, transientFault, floorFailed, direction, elevatorWithFaultID);
	}

	/**
	 * Decodes data...Data received (or expected) is "elevatorID, 0"
	 */
	public void decodeData() {

		int len = 0;
		int index = TEMP_LENGTH;

		// Find out the floor number that request occurred at
		for (int i = index; i < data.length; i++) {
			if (data[i] == (byte) 0) { // Find next zero
				break;
			}
			len++;
		}
		elevatorWithFaultID = Integer.valueOf(new String(data, index, len)); // Find elevator ID

	}

}
