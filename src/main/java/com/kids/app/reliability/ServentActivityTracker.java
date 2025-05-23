package com.kids.app.reliability;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServentActivityTracker {

    private volatile ServentState status;
    private volatile long timestamp;
    // If weak timeout has been reached
    private volatile boolean notify;
    // Flags for waiting for confirmation
    private volatile boolean waitingForHelperConfirmation;
    private volatile boolean confirmedDeadByHelper;

    public ServentActivityTracker() {
        this.status = ServentState.ALIVE;
        this.timestamp = System.currentTimeMillis();
        this.notify = false;
        this.waitingForHelperConfirmation = false;
        this.confirmedDeadByHelper = false;
    }

    public void resetTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public void initiateSuspicionCycle() {
        this.status = ServentState.SUSPICIOUS;
        this.notify = true;
        this.waitingForHelperConfirmation = true;
        this.confirmedDeadByHelper = false;
    }

    public void resolveSuspicionCycle() {
        this.status = ServentState.ALIVE;
        resetTimestamp();
        this.notify = false;
        this.waitingForHelperConfirmation = false;
        this.confirmedDeadByHelper = false;
    }

}
