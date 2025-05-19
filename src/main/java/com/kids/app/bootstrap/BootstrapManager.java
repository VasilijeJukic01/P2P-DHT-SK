package com.kids.app.bootstrap;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentIdentity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BootstrapManager {
    
    private final List<ServentIdentity> activeServents;
    private final AtomicBoolean sync = new AtomicBoolean(false);
    private final Random rand;
    
    public BootstrapManager() {
        this.activeServents = new ArrayList<>();
        this.rand = new Random(System.currentTimeMillis());
    }
    
    public void handleMessage(Socket newServentSocket) throws IOException {
        Scanner socketScanner = new Scanner(newServentSocket.getInputStream());
        String message = socketScanner.nextLine();
        
        switch (message) {
            case "Hail" -> {
                String newServentIp = socketScanner.nextLine();
                int newServentPort = socketScanner.nextInt();
                ServentIdentity newServentInfo = new ServentIdentity(newServentIp, newServentPort);

                System.out.println("got " + newServentIp + ":" + newServentPort);
                PrintWriter socketWriter = new PrintWriter(newServentSocket.getOutputStream());

                boolean canAdd = sync.compareAndSet(false, true);
                // Wait for synchronization
                if (!canAdd) {
                    AppConfig.timestampedStandardPrint("Waiting for synchronization: " + newServentPort);
                    socketWriter.write(createMessage(new ServentIdentity("localhost", -3)));
                }
                else {
                    // We are the first one
                    if (activeServents.isEmpty()) {
                        socketWriter.write(createMessage(new ServentIdentity("localhost", -1)));
                        activeServents.add(newServentInfo);
                        AppConfig.timestampedStandardPrint("First node: " + newServentPort);
                    }
                    else {
                        ServentIdentity randServent = activeServents.get(rand.nextInt(activeServents.size()));
                        String msg = createMessage(randServent);
                        AppConfig.timestampedStandardPrint("Sending message to " + newServentPort + ": " + msg);
                        socketWriter.write(msg);
                    }
                }
                socketWriter.flush();
            }
            case "New" -> {
                String newServentIp = socketScanner.nextLine();
                int newServentPort = socketScanner.nextInt();
                ServentIdentity newServentInfo = new ServentIdentity(newServentIp, newServentPort);
                AppConfig.timestampedStandardPrint("Adding: " + newServentIp + ":" + newServentPort);
                activeServents.add(newServentInfo);
                sync.set(false);
                AppConfig.timestampedStandardPrint("Synchronization released: " + newServentPort);
            }
            case "Sorry" -> {
                int newServentPort = socketScanner.nextInt();
                sync.set(false);
                AppConfig.timestampedStandardPrint("Synchronization released: " + newServentPort);
            }
            case "FirstNode" -> {
                sync.set(false);
                AppConfig.timestampedStandardPrint("Synchronization released for first node");
            }
        }
    }

    private String createMessage(ServentIdentity servent) {
        String activeServentsList = this.activeServents.stream()
                .map(info -> info.ip() + ":" + info.port())
                .collect(Collectors.joining(","));

        String serventInfo = servent == null ? "-1:-1" : servent.ip() + ":" + servent.port();
        return serventInfo + "_|" + activeServentsList + "|\n";
    }
}
