package com.kids.app.reliability;

import com.kids.app.AppConfig;
import com.kids.app.Cancellable;
import com.kids.app.servent.ServentIdentity;
import com.kids.app.servent.ServentInfo;
import com.kids.mutex.SuzukiKasamiMutex;
import com.kids.servent.message.reliability.PingMessage;
import com.kids.servent.message.util.MessageUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ServentPulseManager implements Runnable, Cancellable {

    // TODO: Implement CRASH

    private volatile boolean working = true;
    private final ServentActivityTracker tracker;

    @Override
    public void run() {
        while(working) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!working) break;

            ServentInfo predecessor = AppConfig.chordState.getPredecessorInfo();
            if (predecessor == null) {
                tracker.resetTimestamp();
                tracker.setStatus(ServentState.DEAD);
                tracker.setNotify(false);
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

            // Weak limit check
            if (System.currentTimeMillis() - tracker.getTimestamp() > AppConfig.WEAK_TIMEOUT && !tracker.isNotify()) {
                tracker.setStatus(ServentState.SUSPICIOUS);
                tracker.setNotify(true);
                checkProblematicNode(predecessor.getIpAddress(), predecessor.getListenerPort());
            }

            // Strong limit check
            if (System.currentTimeMillis() - tracker.getTimestamp() > AppConfig.STRONG_TIMEOUT && tracker.getStatus() == ServentState.SUSPICIOUS) {
                SuzukiKasamiMutex mutex = (SuzukiKasamiMutex) AppConfig.chordState.getMutex();

                List<ServentIdentity> serventIdentity = AppConfig.chordState.getAllNodeInfo().stream()
                        .map(n -> new ServentIdentity(n.getIpAddress(), n.getListenerPort()))
                        .toList();

                mutex.lock(serventIdentity, true);

                tracker.setStatus(ServentState.DEAD);
                AppConfig.timestampedStandardPrint("Node death: " + predecessor.getIpAddress() + ":" + predecessor.getListenerPort());
                // TODO: Implement
                // AppConfig.chordState.removeNode(predecessor);

                // Remove from token queue
                mutex.getToken().removeNodeFromQueue(new ServentIdentity(predecessor.getIpAddress(), predecessor.getListenerPort()));

                if(AppConfig.chordState.getSuccessorTable().length == 0){
                    AppConfig.timestampedStandardPrint("I am only node in the network");
                    continue;
                }

                // Recovery
                recovery(predecessor);

                tracker.setStatus(ServentState.ALIVE);
                tracker.setNotify(false);

                mutex.unlock();
            }
        }
    }

    private void recovery(ServentInfo predecessor) {
       // TODO: Recovery mechanism
    }

    private void checkProblematicNode(String nodeIp, int nodePort) {
        // TODO: Other node should try to contact problematic node
    }

    @Override
    public void stop() {
        working = false;
    }

}
