package com.kids.app.system;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
import com.kids.file.FileData;
import com.kids.mutex.DistributedMutex;
import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.message.system.ReplicateMessage;
import com.kids.servent.message.system.UploadAckMessage;
import com.kids.servent.message.system.UploadMessage;
import com.kids.servent.message.util.MessageUtil;
import com.kids.app.servent.ServentIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class SystemManager {

    @Getter private final AtomicBoolean isPublic = new AtomicBoolean(true);
    private final Map<Integer, Map<String, FileData>> data;
    private final DistributedMutex<ServentIdentity, SuzukiKasamiToken> suzukiKasamiMutex;

    public void upload(int key, String path, String address,  int port) {
        if (AppConfig.chordState.isKeyMine(key)) {
            FileData fileData = putIntoData(key, path, address, port);
            AppConfig.timestampedStandardPrint("Storing image: " + fileData.path() + " (uploaded by: " + fileData.serventIdentity() + ")");

            // Inform neighbours about the new file and create replication
            createReplicas(fileData);

            // Unlock if we are the one who locked
            if(port == AppConfig.myServentInfo.getListenerPort()) suzukiKasamiMutex.unlock();
            else {
                UploadAckMessage unlockMessage = new UploadAckMessage(
                        AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.myServentInfo.getListenerPort(),
                        address,
                        port
                );
                MessageUtil.sendMessage(unlockMessage);
            }

        }
        else {
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);

            UploadMessage pm = new UploadMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    nextNode.getIpAddress(),
                    nextNode.getListenerPort(),
                    address,
                    port,
                    key,
                    path
            );

            MessageUtil.sendMessage(pm);
        }
    }

    public FileData putIntoData(int key, String path, String address, int port) {
        Map<String, FileData> map = data.computeIfAbsent(key, k -> new HashMap<>());
        ServentIdentity serventIdentity = new ServentIdentity(address, port);
        FileData fileData = new FileData(path, serventIdentity);
        map.putIfAbsent(path, fileData);

        return fileData;
    }

    public List<FileData> getAllData() {
        List<FileData> allFiles = new ArrayList<>();
        for (Map<String, FileData> fileMap : data.values()) {
            allFiles.addAll(fileMap.values());
        }
        return allFiles;
    }

    private void createReplicas(FileData fileData) {
        // Send to predecessor
        ServentInfo predecessorInfo = AppConfig.chordState.getPredecessorInfo();
        if(predecessorInfo != null && !predecessorInfo.equals(AppConfig.myServentInfo)) {
            ReplicateMessage rm = new ReplicateMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    predecessorInfo.getIpAddress(),
                    predecessorInfo.getListenerPort(),
                    fileData
            );
            MessageUtil.sendMessage(rm);
        }

        // Send to successor
        ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();
        if (successorTable[0] != null && !successorTable[0].equals(AppConfig.myServentInfo)) {
            ReplicateMessage rm = new ReplicateMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    successorTable[0].getIpAddress(),
                    successorTable[0].getListenerPort(),
                    fileData
            );
            MessageUtil.sendMessage(rm);
        }
    }

}
