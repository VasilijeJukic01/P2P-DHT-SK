package com.kids.app.bootstrap.client;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentIdentity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ServentBroadcastManager {

    @Getter private static final List<ServentIdentity> broadcastNodes = new ArrayList<>();

    public static void updateBroadcastNodes(List<ServentIdentity> newNodes) {
        if (newNodes == null || newNodes.isEmpty()) return;

        newNodes.stream()
                .filter(node -> !broadcastNodes.contains(node))
                .filter(node -> node.port() != AppConfig.myServentInfo.getListenerPort() ||
                        !node.ip().equals(AppConfig.myServentInfo.getIpAddress()))
                .forEach(broadcastNodes::add);
    }

}
