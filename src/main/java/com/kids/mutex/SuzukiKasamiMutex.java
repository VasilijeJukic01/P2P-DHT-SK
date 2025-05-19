package com.kids.mutex;

import com.kids.app.AppConfig;
import com.kids.app.servent.ServentIdentity;
import com.kids.app.servent.ServentInfo;
import com.kids.servent.message.Message;
import com.kids.servent.message.token.SuzukiTokenMessage;
import com.kids.servent.message.token.SuzukiTokenRequestMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * Suzuki-Kasami Mutex implementation
 * <p>
 * This class implements the Suzuki-Kasami distributed mutual exclusion algorithm.
 * It uses a token to control access to a critical section.
 * <p>
 * The class is thread-safe and can be used in a distributed system.
 */
public class SuzukiKasamiMutex implements DistributedMutex<ServentIdentity, SuzukiKasamiToken> {

    private final int nodeId;
    private final AtomicBoolean lock;
    private final AtomicBoolean hasToken;
    private final AtomicBoolean usesToken;

    @Getter private final CopyOnWriteArrayList<Integer> RN;

    @Getter private SuzukiKasamiToken token;

    public SuzukiKasamiMutex(int numNodes, int nodeId) {
        this.nodeId = nodeId;
        this.lock = new AtomicBoolean(false);
        this.hasToken = new AtomicBoolean(false);
        this.usesToken = new AtomicBoolean(false);
        this.RN = new CopyOnWriteArrayList<>();

        IntStream.range(0, numNodes).forEach(i -> RN.add(0));
    }

    /**
     * This method is called when a node wants to acquire a lock.
     * It will first check if it has the token, if not it will request it.
     * If it has the token, it will acquire the lock.
     * <p>
     * @param broadcastNodes List of nodes to broadcast the token request to
     * @param isPriority If true, the node will not wait for the token
     */
    @Override
    public void lock(List<ServentIdentity> broadcastNodes, boolean isPriority) {
        AppConfig.timestampedStandardPrint("Requested token");

        // Lock (other threads on the same node cant increment RN)
        while(!lock.compareAndSet(false, true)){
            try {
                if(isPriority) break;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Ask for token
        if(token == null){
            incrementRN();
            broadcastTokenRequest(broadcastNodes);
        }

        // Wait
        while (!hasToken.get()){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        usesToken.set(true);
    }

    /**
     * This method is called when a node wants to release a lock.
     * It will update the token and send it to the next node in the queue.
     */
    @Override
    public void unlock() {
        // Update LN
        token.getLN().set(nodeId, RN.get(nodeId));

        // Add requests to queue if they are not already in it
        for(int i = 0; i < RN.size(); i++){
            ServentIdentity serventIdentity = getServentIdentity(i);
            if(serventIdentity != null && RN.get(i) == token.getLN().get(i) + 1 && !token.getQueue().contains(serventIdentity)){
                token.getQueue().add(serventIdentity);
            }
        }

        // If someone is waiting -> send token
        tryToSendToken();

        usesToken.set(false);
        lock.set(false);

        AppConfig.timestampedStandardPrint("Released token");
    }

    public void tryToSendToken() {
        if(!token.getQueue().isEmpty()){
            ServentIdentity receiver = token.getQueue().poll();

            Message tokenMessage = new SuzukiTokenMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    receiver.ip(),
                    receiver.port(),
                    token
            );

            MessageUtil.sendMessage(tokenMessage);
            hasToken.set(false);
            token = null;
        }
    }

    public void broadcastTokenRequest(List<ServentIdentity> broadcastNodes){
        for(ServentIdentity receiver : broadcastNodes) {
            Message requestMessage = new SuzukiTokenRequestMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    receiver.ip(),
                    receiver.port(),
                    RN.get(nodeId)
            );
            MessageUtil.sendMessage(requestMessage);
        }
    }

    private ServentIdentity getServentIdentity(int nodeId) {
        for (ServentInfo nodeInfo : AppConfig.chordState.getAllNodeInfo()) {
            if (nodeInfo != null && nodeInfo.getChordId() == nodeId) {
                return new ServentIdentity(nodeInfo.getIpAddress(), nodeInfo.getListenerPort());
            }
        }
        return null;
    }

    public void incrementRN(){
        RN.set(nodeId, RN.get(nodeId) + 1);
    }

    @Override
    public void setToken(SuzukiKasamiToken token) {
        this.token = token;
        this.hasToken.set(true);
        AppConfig.timestampedStandardPrint("Token set successfully");
    }

    @Override
    public boolean hasToken() {
        return hasToken.get();
    }

    @Override
    public void setHasToken(boolean hasToken) {
        this.hasToken.set(hasToken);
    }

    @Override
    public boolean usesToken() {
        return usesToken.get();
    }

    @Override
    public void setUsesToken(boolean usesToken) {
        this.usesToken.set(usesToken);
    }
}

