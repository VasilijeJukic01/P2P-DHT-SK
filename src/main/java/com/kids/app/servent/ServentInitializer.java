package com.kids.app.servent;

import com.kids.app.AppConfig;
import com.kids.app.ChordState;
import com.kids.app.bootstrap.client.BootstrapClient;
import com.kids.app.bootstrap.client.BootstrapResponse;
import com.kids.app.bootstrap.client.ServentBroadcastManager;
import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.message.core.NewNodeMessage;
import com.kids.servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private final BootstrapClient bootstrapClient;
	private ServentIdentity someServentInfo;

	public ServentInitializer() {
		this.bootstrapClient = new BootstrapClient();
	}

	private void contactBootstrap() {
		BootstrapResponse response = bootstrapClient.contactBootstrap();
		someServentInfo = response.someServentInfo();
		ServentBroadcastManager.updateBroadcastNodes(response.nodes());
	}

	@Override
	public void run() {
		contactBootstrap();

		while(someServentInfo != null && someServentInfo.port() == -3) {
			try {
				Thread.sleep(500);
				contactBootstrap();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (someServentInfo == null || someServentInfo.port() == -2) {
			AppConfig.timestampedErrorPrint("Error in contacting bootstrap. Exiting...");
			System.exit(0);
		}

		if (someServentInfo.port() == -1) initializeFirstNode();
		else initializeRegularNode();
	}

	private void initializeFirstNode() {
		AppConfig.timestampedStandardPrint("First node in Chord system.");
		SuzukiKasamiToken token = new SuzukiKasamiToken(ChordState.CHORD_SIZE);
		AppConfig.chordState.getMutex().setToken(token);
		AppConfig.chordState.getMutex().setHasToken(true);

		bootstrapClient.notifyFirstNode();
	}

	private void initializeRegularNode() {
		AppConfig.timestampedStandardPrint("Acquiring token...");
		AppConfig.chordState.getMutex().lock(ServentBroadcastManager.getBroadcastNodes(), false);
		AppConfig.timestampedStandardPrint("Got token");

		NewNodeMessage nnm = new NewNodeMessage(
				AppConfig.myServentInfo.getIpAddress(),
				AppConfig.myServentInfo.getListenerPort(),
				someServentInfo.ip(),
				someServentInfo.port()
		);

		MessageUtil.sendMessage(nnm);

		bootstrapClient.notifyNewNode();
	}
}