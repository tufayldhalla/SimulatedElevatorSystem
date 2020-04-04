package elevatorSystem.floorSubsystem.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import elevatorSystem.GUIApplication.GUIApplication;
import elevatorSystem.floorSubsystem.FloorSubsystem;
import elevatorSystem.floorSubsystem.communication.Dispatcher;
import elevatorSystem.miscellaneous.Header;
import elevatorSystem.miscellaneous.IntData;
import elevatorSystem.miscellaneous.StringData;

/**
 * Reads from input file and notifies floor subsystem of these requests at
 * appropriate times
 * 
 * @author L4G3
 * @version 2.0
 */
public class InputFileReader extends Observable implements Runnable {

	/**
	 * Delimiter for reading file
	 */
	private static final String WHITESPACE_DELIMITER = " ";

	/**
	 * Reference to the floor subsystem
	 */
	private FloorSubsystem floorSubsystem;

	/**
	 * Outgoing header to indicate start for ServiceRequest process
	 */
	private static final byte[] SERVICE_REQUEST_HEADER = Header.SERVICE_REQUEST.getHeader();

	/**
	 * Outgoing trailer of packet for ServiceRequest processor
	 */
	private static final byte[] ZERO_PARSER = Header.ZERO.getHeader();

	/**
	 * Text of file
	 */
	private String fileText = "";

	/**
	 * Input Filename to be given by controller
	 */
	private String filename;

	/**
	 * GUI Reference
	 */
	private GUIApplication GUI;

	/**
	 * List of all requests
	 */
	private ArrayList<Request> requests;

	/**
	 * Creates new list of requests and reads the file to populate the list
	 * 
	 * @param s    is the reference to the sender to Scheduler Subsystem from Floor
	 *             Subsystem
	 * @param f    is the reference to the FloorSubsystem's floors
	 * @param file is the filename of the input file
	 */
	public InputFileReader(FloorSubsystem s, String file) {
		floorSubsystem = s;
		filename = file;
		GUI = null;
		requests = new ArrayList<Request>();
		try {
			readFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates new list of requests and reads the file to populate the list
	 * 
	 * @param s    is the reference to the sender to Scheduler Subsystem from Floor
	 *             Subsystem
	 * @param f    is the reference to the FloorSubsystem's floors
	 * @param file is the filename of the input file
	 * @param GUI  is the gui reference
	 */
	public InputFileReader(FloorSubsystem s, String file, GUIApplication GUI) {
		floorSubsystem = s;
		filename = file;
		requests = new ArrayList<Request>();
		try {
			readFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.GUI = GUI;
		this.addObserver(this.GUI);
	}

	/**
	 * Creates new list of requests and reads the file to populate the list
	 * 
	 * @param s    is the reference to the sender to Scheduler Subsystem from Floor
	 *             Subsystem
	 * @param f    is the reference to the FloorSubsystem's floors
	 * @param file is the filename of the input file
	 * @param GUI  is the gui reference
	 */
	public InputFileReader(FloorSubsystem s, GUIApplication GUI) {
		floorSubsystem = s;
		filename = null;
		requests = new ArrayList<Request>();
		this.GUI = GUI;
		this.addObserver(this.GUI);
	}

	/**
	 * Adds request in order of time
	 * 
	 * @param r is the request to add to the list of requests
	 * @return true once done
	 */
	protected boolean addRequest(Request r) {
		for (int i = 0; i < requests.size(); i++) {
			if (r.getTime().before(requests.get(i).getTime())) {
				requests.add(i, r);
				return true;
			}
		}
		requests.add(r); // add to end if not before any other request
		return true;
	}

	/**
	 * Reads the file and adds each request to the list
	 */
	private void readFile() throws Exception {
		BufferedReader br = null;
		try {
			// Reading the file
			FileReader f = new FileReader(filename);
			br = new BufferedReader(f);

			String line = "";
			// Read to skip over the header line
			line = br.readLine();
			saveLine(line);
			String[] columnHeadings = line.split(WHITESPACE_DELIMITER);
			if (columnHeadings.length <= 0) { // Just in case
				throw new IOException("No data found in first line");
			}

			// Reading from the second line (and beyond)
			while ((line = br.readLine()) != null) {
				saveLine(line);
				String[] cols = line.split(WHITESPACE_DELIMITER);
				if (cols.length == 5) {
					if (checkValid(cols[0], cols[1], cols[2], cols[3], cols[4])) {
						addRequest(new Request(cols[0], cols[1], cols[2], cols[3], cols[4]));
					}
				}
			}
		} catch (Exception ee) {
			throw ee;
		} finally {
			br.close(); // Make sure the file is closed (even if exception while reading)
		}
		if (GUI != null) {
			setChanged();
			notifyObservers(fileText);
		}
	}

	/**
	 * Check if line is valid input
	 */
	private boolean checkValid(String time, String floor, String dir, String dest, String fault) {
		try {
			int floorNum = Integer.valueOf(floor);
			int destNum = Integer.valueOf(dest);
			int faultNum = Integer.valueOf(fault);
			
			String temp[] = time.split(":");
			String temp2[] = temp[2].split("\\.");
			if(temp.length < 2 || temp2.length < 2) {
				return false;
			}
				
			int hour = Integer.valueOf(temp[0]);
			int min = Integer.valueOf(temp[1]);
			int sec = Integer.valueOf(temp2[0]);
			int msec = Integer.valueOf(temp2[1]);
			
			if (floorNum <= 0 || floorNum > IntData.NUM_FLOORS.getNum() || destNum <= 0
					|| destNum > IntData.NUM_FLOORS.getNum()) {
				return false;
			} else if (!(dir.equals(StringData.UP.getString()) || dir.equals(StringData.DOWN.getString()))){
				return false;
			} else if (!(faultNum == 1 || faultNum == 2 || faultNum == 3)) {
				return false;
			} else if(destNum > floorNum && dir.equals(StringData.DOWN.getString())) {
				return false;
			} else if(destNum < floorNum && dir.equals(StringData.UP.getString())) {
				return false;
			} else if(hour < 0 || hour >24 || min < 0 || min > 60 || sec < 0 || sec > 60 || msec <0 || msec>1000) {
				return false;
			}
		} catch (Exception ee) {
			return false;
		}
		return true;
	}

	/**
	 * Saves file in text format for GUI
	 */
	private void saveLine(String line) {
		fileText += line + "\n";
	}

	/**
	 * Reset file to read
	 */
	public void resetInput(String file) {
		requests.clear();
		filename = file;
		fileText = ""; // clear text
		boolean runInputs = true;
		try {
			readFile();
		} catch (Exception e) {
			e.printStackTrace();
			runInputs = false;
			setChanged();
			notifyObservers(true);
		}

		if (requests.isEmpty()) {
			runInputs = false;
			setChanged();
			notifyObservers(true);
		}

		if (runInputs) {
			this.run();
		}
	}

	/**
	 * Create new file input
	 */
	public void createNewFile(String fileName, String data) {
		File newFile = new File(fileName);
		if (newFile.exists()) {
			newFile.delete();
			newFile = new File(fileName);
		}
		FileWriter fw = null;

		try {
			fw = new FileWriter(newFile, true);
			fw.write("Time Floor FloorButton CarButton Fault\n" + data + "\n");
		} catch (IOException e) {
			System.out.println("IO Error");
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				System.out.println("IO Error");
			}
		}
	}

	/**
	 * Notifies FloorSubsystem of requests at appropriate times
	 */
	public void run() {

		Time before = null;
		Iterator<Request> itr = requests.iterator();
		// Loop for all requests in file
		while (itr.hasNext()) {
			Request r = itr.next();
			if (before != null) {
				// Sleep to simulate time in-between requests
				try {
					Thread.sleep((int) ((double) (r.getTime().waitTime(before)) / floorSubsystem.getSpeed().getSpeed()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// formulate data to dispatch to Floor Dispatcher
			byte data[] = r.getRequest();
			int len = data.length + SERVICE_REQUEST_HEADER.length + ZERO_PARSER.length;
			byte msg[] = new byte[len];
			int index = 0;
			System.arraycopy(SERVICE_REQUEST_HEADER, 0, msg, index, SERVICE_REQUEST_HEADER.length);
			index += SERVICE_REQUEST_HEADER.length;
			System.arraycopy(data, 0, msg, index, data.length);
			index += data.length;
			System.arraycopy(ZERO_PARSER, 0, msg, index, ZERO_PARSER.length);

			String s = r.toString();
			new Thread() {
				public void run() {
					/*
					 * Output for simulating on console and to output file
					 */
					try {
						floorSubsystem.getOutput().write(s);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();

			System.out.print("\nFloor: Received user input = \n\tbytes: ");
			for (int j = 0; j < msg.length; j++) {
				System.out.print(" " + msg[j]);
			}
			System.out.println("\n\tString: " + new String(msg, 0, msg.length));

			// notify Dispatcher that there is a new request
			Dispatcher d = new Dispatcher(floorSubsystem, msg);
			d.start();

			// update before so that new thread sleep value can be found
			before = r.getTime();
		}
	}
}
