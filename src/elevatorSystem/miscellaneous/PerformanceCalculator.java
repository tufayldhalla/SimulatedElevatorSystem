package elevatorSystem.miscellaneous;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Measures the Scheduler's performance to aid in predicting the performance.
 * 
 * @author L4G3
 */
public class PerformanceCalculator {
	/**
	 * Contains the measurements for the interface
	 */
	private ArrayList<Double> measurements;

	/**
	 * Contains the measurements for the interface
	 */
	private HashMap<Integer, Double> startTimes;

	/**
	 * Interface Name
	 */
	private String name;

	/**
	 * File name of the calculation output file
	 */
	private String file;

	/**
	 * Reference to output generator
	 */
	private Output output;

	/**
	 * Constructs new performance calculator
	 */
	public PerformanceCalculator(String interfaceName, Output out, String fileName) {
		measurements = new ArrayList<Double>();
		name = interfaceName;
		output = out;
		file = fileName;
		output.createCalculationFile(fileName);
		addFileTimes();
	}

	/**
	 * Constructs new performance calculator
	 */
	public PerformanceCalculator(String interfaceName, Output out, String fileName, boolean requestMeasurement) {
		this(interfaceName, out, fileName);
		startTimes = new HashMap<Integer, Double>();
	}

	/**
	 * Add times from previous runs of the program to get more data
	 */
	private void addFileTimes() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = "";
			/*
			 * Read the file to get the past iteration's times
			 */
			while ((line = br.readLine()) != null) {
				double newMeasurement = Double.parseDouble(line);
				/*
				 * Add the past iteration's times to the current calculator for calculations
				 * later on
				 */
				synchronized (measurements) {
					measurements.add(newMeasurement);
				}
			}
		} catch (IOException e) {
			//System.out.println("IO Error"); file empty
		} finally {
			/*
			 * Close buffered reader
			 */
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e2) {
				System.out.println("IO Error");
			}
		}
	}

	/**
	 * Gets the list of measurements for this interface
	 * 
	 * @return list of measurements for this interface
	 */
	public ArrayList<Double> getMeasurements() {
		synchronized (measurements) {
			return measurements;
		}
	}

	/**
	 * Add to the list of measurements for this interface
	 */
	public void addMeasurement(double newMeasurement) {
		synchronized (measurements) {
			measurements.add((double) newMeasurement);
			new Thread() {
				public void run() {
					try {
						output.writeCalculation(("" + (double) newMeasurement), file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	/**
	 * Add to the list of start times for this interface
	 */
	public void addStartTime(Integer key, float newStartTime) {
		synchronized (startTimes) {
			startTimes.put(key, (double) newStartTime);
		}
	}

	/**
	 * Determines the end time of the drop off
	 */
	public void calculateTime(Integer key, float endTime) {
		synchronized (startTimes) {
			if (startTimes.containsKey(key)) {
				synchronized (measurements) {
					Double startTime = startTimes.remove(key);
					measurements.add((double) (endTime - startTime) / 1000000F);
					new Thread() {
						public void run() {
							try {
								output.writeCalculation(("" + (double) (endTime - startTime) / 1000000F), file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}.start();
				}
			}
		}
	}

	/**
	 * Calculates the mean of the measurements. Mean = sum of all measurements / N
	 * number of measurements
	 * 
	 * @return mean of the measurements for this interface
	 */
	public double calculateMean() {
		double mean = 0;
		double sum = 0;
		double N = measurements.size();
		for (Double X : measurements) {
			sum += X;
		}
		mean = sum / N;
		if (N == 0)
			return 0;
		return mean;
	}

	/**
	 * Calculates the variance of the measurements. Variance = Sum of (measurement
	 * subtracted by the mean) to the power of 2 all over N number of measurements
	 * 
	 * @return variance of the measurements for this interface
	 */
	public double calculateVariance() {
		double variance = 0F;
		double mean = calculateMean();
		double sum = 0F;
		int N = measurements.size();
		for (double X : measurements) {
			sum += Math.pow((X - mean), 2);
		}
		variance = sum / N;
		if (N == 0)
			return 0;
		return variance;
	}

	/**
	 * Calculates mean & variance of the measurements and returns string
	 * representation of it
	 */
	public String calculationsToString() {
		String str = "++++++++++++++++++++++++++++++++++\n";
		str += name;
		str += ":\n\tMean = " + String.format("%.2f", calculateMean());
		str += " ms\n\tVariance = " + String.format("%.2f", calculateVariance());
		str += " ms\n++++++++++++++++++++++++++++++++++\n";
		return str;
	}
}
