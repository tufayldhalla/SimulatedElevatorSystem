package elevatorSystem.miscellaneous;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Output object to write to output file
 * 
 * @author L4G3
 * @version 2.0
 */
public class Output {

	/**
	 * File to be written to for testing purposes
	 */
	private File file;

	/**
	 * File to be written to for simulation purposes of elevator
	 */
	private File simulation;

	/**
	 * File to be written to for measurements purposes of scheduler
	 */
	private ArrayList<File> measurements;

	/**
	 * Creates new file
	 * 
	 * @param filename of file to write to
	 */
	public Output(String filename) {
		file = new File(filename);
		if (file.exists()) {
			file.delete();
			file = new File(filename);
		}
		simulation = new File("text_files/simulation.txt");
		if (simulation.exists()) {
			simulation.delete();
			simulation = new File("text_files/simulation.txt");
		}
		measurements = new ArrayList<File>();
	}

	/**
	 * Writes to output
	 * 
	 * @param outString is string to write to output.txt
	 * @throws IOException
	 */
	public synchronized void write(String outString) throws IOException {
		FileWriter fw = null;

		try {
			fw = new FileWriter(file, true);
			fw.write(outString + "\n");
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
	 * Writes to simulation file
	 * 
	 * @param outString is string to write to simulation.txt
	 * @throws IOException
	 */
	public synchronized void simulate(String outString) throws IOException {
		
		FileWriter fw = null;
		
		try {
			fw = new FileWriter(simulation, true);
			fw.write(outString + "\n");
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
	 * Creates a calculation file
	 */
	public void createCalculationFile(String fileName) {
		File calculationFile = new File(fileName);
		if (calculationFile.exists()) {
			//calculationFile.delete();
			//calculationFile = new File(fileName);
		}
		synchronized (measurements) {
			measurements.add(calculationFile);
		}
	}

	/**
	 * Writes to a calculation file
	 * 
	 * @param outString is string to write to a calculation file
	 * @throws IOException
	 */
	public synchronized void writeCalculation(String outString, String fileName) throws IOException {
		FileWriter fw = null;
		for (File calculationFile : measurements) {
			if (calculationFile.getName() == fileName) {
				try {
					fw = new FileWriter(calculationFile, true);
					fw.write(outString + "\n");
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
				break;
			}
		}
	}
}
