package com.kids.app.bootstrap;

import com.kids.app.AppConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class BootstrapServer {

	private volatile boolean working = true;
	private final BootstrapManager bootstrapManager;
	
	private class CLIWorker implements Runnable {
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			String line;
			while(true) {
				line = sc.nextLine();
				if (line.equals("stop")) {
					working = false;
					break;
				}
			}
			sc.close();
		}
	}
	
	public BootstrapServer() {
		this.bootstrapManager = new BootstrapManager();
	}
	
	public void doBootstrap(int bsPort) {
		Thread cliThread = new Thread(new CLIWorker());
		cliThread.start();
		
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(bsPort);
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			AppConfig.timestampedErrorPrint("Problem while opening listener socket.");
			System.exit(0);
		}
		
		while (working) {
			try {
				Socket newServentSocket = listenerSocket.accept();
				
				 /* 
				 * Handling these messages is intentionally sequential, to avoid problems with concurrent initial starts.
				 * 
				 * In practice, we would have an always-active backbone of servents to avoid this problem.
				 */
				bootstrapManager.handleMessage(newServentSocket);
				newServentSocket.close();
			} catch (Exception ignored) { }
		}
	}
	
	/**
	 * Expects one command line argument - the port to listen on.
	 */
	public static void main(String[] args) {
		if (args.length != 1) AppConfig.timestampedErrorPrint("Bootstrap started without port argument.");
		
		int bsPort = 0;
		try {
			bsPort = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Bootstrap port not valid: " + args[0]);
			System.exit(0);
		}
		
		AppConfig.timestampedStandardPrint("Bootstrap server started on port: " + bsPort);
		
		BootstrapServer bs = new BootstrapServer();
		bs.doBootstrap(bsPort);
	}
}
