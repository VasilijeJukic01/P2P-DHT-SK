package com.kids.app.system;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentInfo;
import com.kids.file.FileData;
import com.kids.file.FileOperations;
import com.kids.mutex.DistributedMutex;
import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.message.system.*;
import com.kids.servent.message.util.MessageUtil;
import com.kids.app.servent.ServentIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@AllArgsConstructor
public class SystemManager {

    @Getter private final AtomicBoolean isPublic = new AtomicBoolean(true);
    private final Map<Integer, Map<String, FileData>> data;
    private final DistributedMutex<ServentIdentity, SuzukiKasamiToken> suzukiKasamiMutex;
    private List<String> removeList;

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

    public void remove(int key, String value, String address, int port){
        if (AppConfig.chordState.isKeyMine(key)) {
            Status deleted = tryToRemove(key, value, address, port);

            if (deleted == Status.SUCCESS) removeReplicas(key, value);

            // Unlock if we are the one who locked
            if(Objects.equals(address, AppConfig.myServentInfo.getIpAddress()) && port == AppConfig.myServentInfo.getListenerPort()) {
                suzukiKasamiMutex.unlock();
            }
            else {
                RemoveAckMessage ram = new RemoveAckMessage(
                        AppConfig.myServentInfo.getIpAddress(),
                        AppConfig.myServentInfo.getListenerPort(),
                        address,
                        port,
                        value,
                        deleted
                );
                MessageUtil.sendMessage(ram);
            }
        }
        else {
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);

            RemoveMessage rm = new RemoveMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    nextNode.getIpAddress(),
                    nextNode.getListenerPort(),
                    key,
                    value,
                    address,
                    port
            );
            MessageUtil.sendMessage(rm);
        }
    }

    public Status tryToRemove(int key, String value, String address, int port) {
        Map<String, FileData> map = data.get(key);
        if (map == null) {
            AppConfig.timestampedStandardPrint("Map for key " + key + " is null");
            return Status.FAILURE;
        }

        FileData fileData = map.get(value);
        if (fileData == null) {
            AppConfig.timestampedStandardPrint("Image remove: " + value + " not found");
            return Status.NOT_FOUND;
        }

        if (!canDelete(fileData.serventIdentity(), address, port)) {
            AppConfig.timestampedStandardPrint("Image remove: " + value + " is not owned by " + address + ":" + port);
            return Status.FAILURE;
        }

        map.remove(value);
        AppConfig.timestampedStandardPrint("Image remove: " + fileData.path() + " owned by: " + fileData.serventIdentity().ip() + ":" + fileData.serventIdentity().port());
        return Status.SUCCESS;
    }

    public void removeReplicasHandle(String path) {
        if(!removeList.contains(path)) removeList.add(path);

        // Remove replicas for all paths in removeList
        List<String> copyOnWrite = new CopyOnWriteArrayList<>();
        for (String p : removeList) {
            int key = FileOperations.hashFilePath(p);

            Map<String, FileData> map = data.get(key);
            boolean shouldRemove = true;
            if (map != null) {
                FileData deleted = map.remove(p);
                if (deleted != null) {
                    AppConfig.timestampedStandardPrint("Replica image remove: " + deleted.path() + " owned by " + deleted.serventIdentity().ip()+ ":" + deleted.serventIdentity().port());
                    removeReplicas(key, p);
                    shouldRemove = false;
                }
            }
            if (!shouldRemove) copyOnWrite.add(p);
        }
        removeList = copyOnWrite;
    }

    // Private
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

    private void removeReplicas(int key, String value) {
        // Send to predecessor
        ServentInfo predecessorInfo = AppConfig.chordState.getPredecessorInfo();
        if(predecessorInfo != null) {
            RemoveReplicaMessage rrm = new RemoveReplicaMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    predecessorInfo.getIpAddress(),
                    predecessorInfo.getListenerPort(),
                    key,
                    value
            );
            MessageUtil.sendMessage(rrm);
        }

        // Send to successor
        ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();
        if (successorTable[0] != null) {
            RemoveReplicaMessage rrm = new RemoveReplicaMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    successorTable[0].getIpAddress(),
                    successorTable[0].getListenerPort(),
                    key,
                    value
            );
            MessageUtil.sendMessage(rrm);
        }
    }

    private boolean canDelete(ServentIdentity serventIdentity, String address, int port) {
        return serventIdentity.ip().equals(address) && serventIdentity.port() == port;
    }

}
