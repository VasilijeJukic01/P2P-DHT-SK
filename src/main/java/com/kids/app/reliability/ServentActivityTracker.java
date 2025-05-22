package com.kids.app.reliability;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServentActivityTracker {

    private volatile ServentState status;
    private volatile long timestamp;
    private volatile boolean notify;

    public ServentActivityTracker() {
        this.status = ServentState.ALIVE;
        this.timestamp = System.currentTimeMillis();
        this.notify = false;
    }

    public void resetTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

}
