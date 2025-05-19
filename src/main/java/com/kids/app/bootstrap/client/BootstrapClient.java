package com.kids.app.bootstrap.client;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentIdentity;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BootstrapClient {

    public BootstrapResponse contactBootstrap() {
        ServentIdentity someServentInfo = null;
        List<ServentIdentity> nodes = new ArrayList<>();

        try {
            Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_ADDRESS, AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Hail\n" + AppConfig.myServentInfo.getIpAddress() + "\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
            bsWriter.flush();

            Scanner bsScanner = new Scanner(bsSocket.getInputStream());
            String message = bsScanner.nextLine();

            String[] parts = message.split("_");

            String[] serventData = parts[0].split(":");
            String serventIp = serventData[0];
            int serventPort = Integer.parseInt(serventData[1]);

            if (!"-1".equals(serventIp)) {
                someServentInfo = new ServentIdentity(serventIp, serventPort);
            }

            String nodeListString = parts[1].replace("|", "").trim();

            if (!nodeListString.isEmpty()) {
                String[] nodeStrings = nodeListString.split(",\\s*");

                for (String nodeString : nodeStrings) {
                    String[] nodeParts = nodeString.split(":");
                    String nodeIp = nodeParts[0];
                    int nodePort = Integer.parseInt(nodeParts[1]);

                    nodes.add(new ServentIdentity(nodeIp, nodePort));
                }
            }
            bsSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BootstrapResponse(someServentInfo, nodes);
    }

    public void notifyFirstNode() {
        try {
            Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_ADDRESS, AppConfig.BOOTSTRAP_PORT);
            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("FirstNode\n");
            bsWriter.flush();

            bsSocket.close();
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Bootstrap notify for first node failed.");
        }
    }

    public void notifyNewNode() {
        try {
            Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_ADDRESS, AppConfig.BOOTSTRAP_PORT);
            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + "\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
            bsWriter.flush();

            bsSocket.close();
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint("Bootstrap notify for new node failed.");
        }
    }
}
