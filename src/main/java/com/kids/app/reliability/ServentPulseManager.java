package com.kids.app.reliability;

import com.kids.app.AppConfig;
import com.kids.app.Cancellable;
import com.kids.app.ChordState;
import com.kids.app.servent.ServentIdentity;
import com.kids.app.servent.ServentInfo;
import com.kids.mutex.SuzukiKasamiMutex;
import com.kids.mutex.SuzukiKasamiToken;
import com.kids.servent.message.reliability.DoYouHaveTokenMessage;
import com.kids.servent.message.reliability.HelpRequestMessage;
import com.kids.servent.message.reliability.PingMessage;
import com.kids.servent.message.reliability.RecoveryMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class ServentPulseManager implements Runnable, Cancellable {

    public static volatile boolean working = true;
    private final ServentActivityTracker tracker;

    public static final AtomicInteger tokenQueryResponsesCount = new AtomicInteger(0);
    public static volatile boolean globalTokenFoundElsewhere = false;

    @Override
    public void run() {
        while(working) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!working) break;

            ServentInfo predecessor = AppConfig.chordState.getPredecessorInfo();
            if (predecessor == null) {
                tracker.resetTimestamp();
                tracker.setStatus(ServentState.ALIVE);
                tracker.setNotify(false);
                tracker.setWaitingForHelperConfirmation(false);
                tracker.setConfirmedDeadByHelper(false);
                continue;
            }

            // Ping predecessor
            PingMessage pingMessage = new PingMessage(
                    AppConfig.myServentInfo.getIpAddress(),
                    AppConfig.myServentInfo.getListenerPort(),
                    predecessor.getIpAddress(),
                    predecessor.getListenerPort()
            );
            MessageUtil.sendMessage(pingMessage);

            long timeSinceLastResponse = System.currentTimeMillis() - tracker.getTimestamp();

            // Weak limit check
            if (timeSinceLastResponse > AppConfig.WEAK_TIMEOUT && tracker.getStatus() == ServentState.ALIVE && !tracker.isWaitingForHelperConfirmation()) {
                tracker.initiateSuspicionCycle();
                AppConfig.timestampedStandardPrint("Node is suspicious: " + predecessor + ". Requesting confirmation.");
                sendHelpRequest(predecessor);
            }

            // Strong limit check
            if (timeSinceLastResponse > AppConfig.STRONG_TIMEOUT && tracker.getStatus() == ServentState.SUSPICIOUS) {
                if (!tracker.isWaitingForHelperConfirmation() || tracker.isConfirmedDeadByHelper()) {
                    AppConfig.timestampedStandardPrint("Strong timeout triggered for node: " + predecessor +
                            " (Time since last response: " + timeSinceLastResponse + "ms, Strong timeout: " + AppConfig.STRONG_TIMEOUT + "ms)." +
                            (tracker.isConfirmedDeadByHelper() ? " Confirmed dead by helper." : " No positive confirmation received, or help request failed."));

                    SuzukiKasamiMutex mutex = (SuzukiKasamiMutex) AppConfig.chordState.getMutex();
                    List<ServentIdentity> liveNodesForMutex = AppConfig.chordState.getAllNodeInfo().stream()
                            .filter(si -> si.getChordId() != predecessor.getChordId() && si.getChordId() != AppConfig.myServentInfo.getChordId())
                            .map(n -> new ServentIdentity(n.getIpAddress(), n.getListenerPort()))
                            .toList();

                    AppConfig.timestampedStandardPrint("Acquiring mutex lock for node removal...");
                    mutex.lock(liveNodesForMutex, true);

                    if (!mutex.hasToken()) {
                        AppConfig.timestampedStandardPrint("Token might be lost with " + predecessor + ". Querying other nodes.");
                        boolean tokenFound = queryOtherNodesForToken(liveNodesForMutex);
                        if (!tokenFound && !mutex.hasToken()) {
                            AppConfig.timestampedStandardPrint("Token confirmed lost or unobtainable. Regenerating token.");
                            SuzukiKasamiToken newToken = new SuzukiKasamiToken(ChordState.CHORD_SIZE);
                            for(int i=0; i < ChordState.CHORD_SIZE; i++) {
                                if (i < mutex.getRN().size() && i < newToken.getLN().size()) {
                                    newToken.getLN().set(i, mutex.getRN().get(i));
                                }
                            }
                            mutex.setToken(newToken);
                        }
                        else {
                            AppConfig.timestampedStandardPrint("Token is held by another node or arrived during query. No regeneration needed.");
                        }
                    }

                    AppConfig.timestampedStandardPrint("Node death officially declared for: " + predecessor);
                    AppConfig.chordState.removeNode(predecessor);

                    if (mutex.getToken() != null) {
                        mutex.getToken().removeNodeFromQueue(new ServentIdentity(predecessor.getIpAddress(), predecessor.getListenerPort()));
                    }


                    if (AppConfig.chordState.getAllNodeInfo().isEmpty()) {
                        AppConfig.timestampedStandardPrint("I am the only node in the network after removal.");
                    }
                    else {
                        sendRecoveryMessages(predecessor);
                        triggerReReplication();
                    }

                    tracker.resolveSuspicionCycle();

                    mutex.unlock();
                } else {
                    AppConfig.timestampedStandardPrint("Strong timeout for " + predecessor +
                            ", but still waiting for helper confirmation (waiting: "+tracker.isWaitingForHelperConfirmation()+
                            ", confirmedDead: "+tracker.isConfirmedDeadByHelper()+").");
                }
            }
        }
    }

    /**
     * Sends a help request to a helper node to confirm the status of the suspicious node.
     * If no helper node is found, it assumes the node is dead and proceeds with the removal process.
     *
     * @param suspiciousNode The node that is suspected to be dead.
     */
    private void sendHelpRequest(ServentInfo suspiciousNode) {
        ServentInfo helperNode = findHelperNode(suspiciousNode);

        if (helperNode != null) {
            HelpRequestMessage hrm = new HelpRequestMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    helperNode.getIpAddress(), helperNode.getListenerPort(),
                    suspiciousNode.getIpAddress(), suspiciousNode.getListenerPort(),
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort()
            );
            MessageUtil.sendMessage(hrm);
            AppConfig.timestampedStandardPrint("Sent help request to " + helperNode + " to check " + suspiciousNode);
        }
        else {
            AppConfig.timestampedStandardPrint("Could not find a suitable helper node to confirm status of " + suspiciousNode + ". Proceeding as if confirmed dead.");
            tracker.setWaitingForHelperConfirmation(false);
            tracker.setConfirmedDeadByHelper(true);
        }
    }

    private ServentInfo findHelperNode(ServentInfo suspiciousNode) {
        // Prefer the successor if it's not the suspicious node or the current node
        ServentInfo successor = AppConfig.chordState.getSuccessorTable()[0];
        if (successor != null && !successor.equals(suspiciousNode) && !successor.equals(AppConfig.myServentInfo)) {
            return AppConfig.chordState.getSuccessorTable()[0];
        }
        // Otherwise, check any other node
        for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
            if (!si.equals(suspiciousNode) && !si.equals(AppConfig.myServentInfo)) {
                return si;
            }
        }
        return null;
    }

    private void sendRecoveryMessages(ServentInfo failedNode) {
        String myIp = AppConfig.myServentInfo.getIpAddress();
        int myPort = AppConfig.myServentInfo.getListenerPort();

        AppConfig.chordState.getAllNodeInfo().stream()
                .filter(serventInfo -> !(serventInfo.getIpAddress().equals(myIp) && serventInfo.getListenerPort() == myPort))
                .forEach(serventInfo -> {
                    RecoveryMessage recoveryMessage = new RecoveryMessage(
                            myIp, myPort,
                            serventInfo.getIpAddress(), serventInfo.getListenerPort(),
                            failedNode
                    );
                    MessageUtil.sendMessage(recoveryMessage);
                });
        AppConfig.timestampedStandardPrint("Sent recovery messages regarding failed node " + failedNode);
    }

    private void triggerReReplication() {
        // TODO: Maybe we will not need this? Replicated data ensures that the data is not lost.
        AppConfig.timestampedStandardPrint("Node removal complete. Re-evaluating data replication.");
    }

    private boolean queryOtherNodesForToken(List<ServentIdentity> liveNodesToQuery) {
        if (liveNodesToQuery.isEmpty()) return false;

        globalTokenFoundElsewhere = false;
        tokenQueryResponsesCount.set(0);
        int queryId = new Random().nextInt(100000);

        for (ServentIdentity otherNode : liveNodesToQuery) {
            DoYouHaveTokenMessage queryMsg = new DoYouHaveTokenMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    otherNode.ip(), otherNode.port(),
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    queryId
            );
            MessageUtil.sendMessage(queryMsg);
        }

        long queryStartTime = System.currentTimeMillis();
        while (tokenQueryResponsesCount.get() < liveNodesToQuery.size() && (System.currentTimeMillis() - queryStartTime) < 3000) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                AppConfig.timestampedErrorPrint("Token query sleep interrupted.");
                return globalTokenFoundElsewhere;
            }
            if (globalTokenFoundElsewhere) break;
        }
        if (tokenQueryResponsesCount.get() < liveNodesToQuery.size()) {
            AppConfig.timestampedStandardPrint("Token query timed out or was interrupted. Received " + tokenQueryResponsesCount.get() + "/" + liveNodesToQuery.size() + " responses.");
        }
        return globalTokenFoundElsewhere;
    }

    @Override
    public void stop() {
        working = false;
    }

}
