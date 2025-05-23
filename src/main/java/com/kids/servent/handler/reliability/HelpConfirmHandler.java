package com.kids.servent.handler.reliability;

import com.kids.app.AppConfig;
import com.kids.app.reliability.ServentActivityTracker;
import com.kids.app.servent.ServentInfo;
import com.kids.servent.handler.MessageHandler;
import com.kids.servent.message.Message;
import com.kids.servent.message.MessageType;
import com.kids.servent.message.reliability.HelpConfirmMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HelpConfirmHandler implements MessageHandler {

    private final Message clientMessage;

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.HELP_CONFIRM) {
            HelpConfirmMessage crm = (HelpConfirmMessage) clientMessage;
            AppConfig.timestampedStandardPrint("Received confirmation about " + crm.getSuspiciousNodeIp() + ":" + crm.getSuspiciousNodePort() + ". Helper says: " + (crm.isAlive() ? "ALIVE" : "DEAD"));

            ServentInfo currentPredecessor = AppConfig.chordState.getPredecessorInfo();
            if (currentPredecessor != null && currentPredecessor.getIpAddress().equals(crm.getSuspiciousNodeIp()) && currentPredecessor.getListenerPort() == crm.getSuspiciousNodePort()) {
                ServentActivityTracker tracker = AppConfig.chordState.getTracker();

                // Signal
                tracker.setWaitingForHelperConfirmation(false);

                if (crm.isAlive()) {
                    tracker.resolveSuspicionCycle();
                    AppConfig.timestampedStandardPrint("Predecessor " + currentPredecessor + " confirmed ALIVE by helper. Tracker reset.");
                }
                else {
                    tracker.setConfirmedDeadByHelper(true);
                    AppConfig.timestampedStandardPrint("Predecessor " + currentPredecessor + " confirmed DEAD by helper. Awaiting strong timeout.");
                }
            }
            else {
                AppConfig.timestampedStandardPrint("Confirmation received for node " + crm.getSuspiciousNodeIp() + ":" + crm.getSuspiciousNodePort() +
                        ", which is not my current predecessor or predecessor is null. Ignoring.");
            }
        } else {
            AppConfig.timestampedErrorPrint("HelpConfirmHandler received a message that is not HELP_CONFIRM: " + clientMessage.getMessageType());
        }
    }
}
