package com.kids.app.servent;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.kids.app.AppConfig;
import com.kids.servent.message.core.NewNodeMessage;
import com.kids.servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private int getSomeServentPort() {
		int bsPort = AppConfig.BOOTSTRAP_PORT;
		int retVal = -2;
		
		try {
			Socket bsSocket = new Socket("localhost", bsPort);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();
			
			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			retVal = bsScanner.nextInt();
			
			bsSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	@Override
	public void run() {
		int someServentPort = getSomeServentPort();
		
		if (someServentPort == -2) {
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}
		// Bootstrap gave us -1 -> we are first
		if (someServentPort == -1) {
			AppConfig.timestampedStandardPrint("First node in Chord system.");
		}
		// Bootstrap gave us something else - let that node tell our successor that we are here
		else {
			NewNodeMessage nnm = new NewNodeMessage(AppConfig.myServentInfo.getListenerPort(), someServentPort);
			MessageUtil.sendMessage(nnm);
		}
	}

}
