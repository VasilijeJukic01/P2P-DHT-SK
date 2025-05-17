package com.kids.app.servent;

import com.kids.app.AppConfig;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This class implements the logic for starting multiple servent instances.
 * <p>
 * To use it, invoke startServentTest with a directory name as parameter.
 * This directory should include:
 * <ul>
 * <li>A <code>servent_list.properties</code> file (explained in {@link AppConfig} class</li>
 * <li>A directory called <code>output</code> </li>
 * <li>A directory called <code>error</code> </li>
 * <li>A directory called <code>input</code> with text files called
 * <code> servent0_in.txt </code>, <code>servent1_in.txt</code>, ... and so on for each servent.
 * These files should contain the commands for each servent, as they would be entered in console.</li>
 * </ul>
 *
 * @author bmilojkovic
 */
public class MultipleServentStarter {

	/**
	 * We will wait for user stop in a separate thread.
	 * The main thread is waiting for processes to end naturally.
	 */
	@AllArgsConstructor
	private static class ServentCLI implements Runnable {
		
		private List<Process> serventProcesses;
		private Process bsProcess;
		
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			while(true) {
				String line = sc.nextLine();
				if (line.equals("stop")) {
					for (Process process : serventProcesses) {
						process.destroy();
					}
					bsProcess.destroy();
					break;
				}
			}
			sc.close();
		}
	}
	
	/**
	 * The parameter for this function should be the name of a directory that
	 * contains a servent_list.properties file which will describe our distributed system.
	 */
	private static void startServentTest(String testName) {
		List<Process> serventProcesses = new ArrayList<>();
		
		AppConfig.readConfig(testName+"/servent_list.properties", 0);
		
		AppConfig.timestampedStandardPrint("Starting multiple servent runner. If servents do not finish on their own, type \"stop\" to finish them");
		
		Process bsProcess = null;
		ProcessBuilder bsBuilder = new ProcessBuilder(
				"java", "-cp", "build\\classes\\java\\main", "com.kids.app.bootstrap.BootstrapServer",
				String.valueOf(AppConfig.BOOTSTRAP_PORT)
		);
		try {
			bsProcess = bsBuilder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Wait for bootstrap to start
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int serventCount = AppConfig.SERVENT_COUNT;
		
		for(int i = 0; i < serventCount; i++) {
			try {
				ProcessBuilder builder = new ProcessBuilder(
						"java", "-cp", "build\\classes\\java\\main", "com.kids.app.servent.ServentMain", testName+"/servent_list.properties",
						String.valueOf(i)
				);

				builder.redirectOutput(new File(testName+"/output/servent" + i + "_out.txt"));
				builder.redirectError(new File(testName+"/error/servent" + i + "_err.txt"));
				builder.redirectInput(new File(testName+"/input/servent" + i + "_in.txt"));

				Process p = builder.start();
				serventProcesses.add(p);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Thread t = new Thread(new ServentCLI(serventProcesses, bsProcess));

		// CLI thread waiting for user to type "stop".
		t.start();
		
		for (Process process : serventProcesses) {
			try {
				process.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		AppConfig.timestampedStandardPrint("All servent processes finished. Type \"stop\" to halt bootstrap.");
		try {
			bsProcess.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		startServentTest("chord");
	}

}
