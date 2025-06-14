package com.kids.app;

import com.kids.app.servent.ServentInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
	
	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}

	public static String BOOTSTRAP_ADDRESS;
	public static int BOOTSTRAP_PORT;
	public static int SERVENT_COUNT;
	public static String ROOT_DIR;

	public static int REPLICATION_FACTOR = 3;
	public static int WEAK_TIMEOUT;
	public static int STRONG_TIMEOUT;
	
	public static ChordState chordState;
	
	/**
	 * Reads a config file. Should be called once at start of app.
	 * The config file should be of the following format:
	 * <br/>
	 * <code><br/>
	 * servent_count=3 			- number of servents in the system <br/>
	 * chord_size=64			- maximum value for Chord keys <br/>
	 * bs.port=2000				- bootstrap server listener port <br/>
	 * servent0.port=1100 		- listener ports for each servent <br/>
	 * servent1.port=1200 <br/>
	 * servent2.port=1300 <br/>
	 * 
	 * </code>
	 * <br/>
	 * So in this case, we would have three servents, listening on ports:
	 * 1100, 1200, and 1300. A bootstrap server listening on port 2000, and Chord system with
	 * max 64 keys and 64 nodes.<br/>
	 * 
	 * @param configName name of configuration file
	 * @param serventId id of the servent, as used in the configuration file
	 */
	public static void readConfig(String configName, int serventId){
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(configName));
			
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}

		try {
			SERVENT_COUNT = Integer.parseInt(properties.getProperty("servent_count"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading servent_count. Exiting...");
			System.exit(0);
		}

		try {
			BOOTSTRAP_ADDRESS = properties.getProperty("bs_address");
		} catch (NullPointerException e) {
			timestampedErrorPrint("Problem reading bs_address. Exiting...");
			System.exit(0);
		}
		
		try {
			BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs_port"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}

		try {
			ROOT_DIR = properties.getProperty("work_dir" + serventId);
		} catch (NullPointerException e) {
			timestampedErrorPrint("Problem reading work_directory property. Exiting...");
			System.exit(0);
		}

		try {
			WEAK_TIMEOUT = Integer.parseInt(properties.getProperty("weak_timeout"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading weak_timeout. Exiting...");
			System.exit(0);
		}

		try {
			STRONG_TIMEOUT = Integer.parseInt(properties.getProperty("strong_timeout"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading strong_timeout. Exiting...");
			System.exit(0);
		}

		String portProperty = "servent"+serventId+".port";
		int serventPort = -1;
		
		try {
			serventPort = Integer.parseInt(properties.getProperty(portProperty));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading " + portProperty + ". Exiting...");
			System.exit(0);
		}

		String serventIp = properties.getProperty("servent.ip_address");
		if (serventIp == null) {
			timestampedErrorPrint("Problem reading ip_address property. Exiting...");
			System.exit(0);
		}

		AppConfig.timestampedStandardPrint(serventIp + ":" + serventPort);

		myServentInfo = new ServentInfo(serventIp, serventPort);

		try {
			ChordState.CHORD_SIZE = Integer.parseInt(properties.getProperty("chord_size"));
			chordState = new ChordState();

		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
			System.exit(0);
		}
	}
	
}
