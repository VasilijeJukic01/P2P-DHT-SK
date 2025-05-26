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

    @Getter private final List<String> followers;
    @Getter private final List<String> pendingFollows;

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

    public void addPendingFollow(String address, int port) {
        String follower = address + ":" + port;
        if (!pendingFollows.contains(follower) && !followers.contains(follower)) {
            pendingFollows.add(follower);
            AppConfig.timestampedStandardPrint("Received follow request from " + follower);
        }
    }

    public void acceptFollow(String address, int port) {
        String follower = address + ":" + port;
        if (pendingFollows.contains(follower)) {
            pendingFollows.remove(follower);
            followers.add(follower);

            FollowAcceptMessage fam = new FollowAcceptMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    address,
                    port
            );
            MessageUtil.sendMessage(fam);

            AppConfig.timestampedStandardPrint("Accepted follow request from " + follower);
        }
        else {
            AppConfig.timestampedErrorPrint("No pending follow request from " + follower);
        }
    }

    public boolean isFollower(String address, int port){
        return followers.contains(address + ":" + port);
    }

    // Private
    private void createReplicas(FileData fileData) {
        List<ServentInfo> replicationTargets = AppConfig.chordState.getReplicationNodes();

        if (replicationTargets.isEmpty()) {
            AppConfig.timestampedStandardPrint("No nodes available for replication of " + fileData.path());
            return;
        }

        AppConfig.timestampedStandardPrint("Replicating " + fileData.path() + " to: " + replicationTargets);
        for (ServentInfo targetNode : replicationTargets) {
            if (targetNode.getChordId() == AppConfig.myServentInfo.getChordId()) {
                continue;
            }

            ReplicateMessage rm = new ReplicateMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    targetNode.getIpAddress(),
                    targetNode.getListenerPort(),
                    fileData
            );
            MessageUtil.sendMessage(rm);
        }
    }

    private void removeReplicas(int key, String value) {
        List<ServentInfo> replicationTargets = AppConfig.chordState.getReplicationNodes();

        if (replicationTargets.isEmpty()) {
            AppConfig.timestampedStandardPrint("No nodes available to send remove_replica for " + value);
            return;
        }

        AppConfig.timestampedStandardPrint("Sending remove_replica for " + value + " to: " + replicationTargets);
        for (ServentInfo targetNode : replicationTargets) {
            if (targetNode.getChordId() == AppConfig.myServentInfo.getChordId()) {
                continue;
            }

            RemoveReplicaMessage rrm = new RemoveReplicaMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    targetNode.getIpAddress(),
                    targetNode.getListenerPort(),
                    key,
                    value
            );
            MessageUtil.sendMessage(rrm);
        }
    }

    public void ensureDataReplication() {
        AppConfig.timestampedStandardPrint("Ensuring data replication for primary owned files.");

        /*  Snapshot of keys for which this node is primary to avoid concurrent modification
             issues if the data map itself gets modified during replication.
         */
        List<Integer> primaryKeys = new ArrayList<>();
        for (Integer key : data.keySet()) {
            if (AppConfig.chordState.isKeyMine(key)) primaryKeys.add(key);
        }

        for (Integer fileKey : primaryKeys) {
            Map<String, FileData> filesForKey = data.get(fileKey);
            if (filesForKey != null) {
                // Create a copy of file entries for this key
                Map<String, FileData> filesToReplicateSnapshot = new HashMap<>(filesForKey);
                for (Map.Entry<String, FileData> fileEntry : filesToReplicateSnapshot.entrySet()) {
                    FileData fileData = fileEntry.getValue();
                    /*
                    This node is primary for fileKey. It's responsible for this fileData's replication.
                    The original uploader check in the thought process was too restrictive.
                    If this node is now primary for a key, it's responsible for all data under that key.
                     */
                    AppConfig.timestampedStandardPrint("Re-evaluating replicas for data with key " + fileKey + ", path: " + fileData.path() + " (Original uploader: " + fileData.serventIdentity() + ")");
                    createReplicas(fileData);
                }
            }
        }
    }

    private boolean canDelete(ServentIdentity serventIdentity, String address, int port) {
        return serventIdentity.ip().equals(address) && serventIdentity.port() == port;
    }

}
