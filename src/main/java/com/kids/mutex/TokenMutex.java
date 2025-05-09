package com.kids.mutex;

public class TokenMutex implements DistributedMutex {

    private volatile boolean haveToken = false;
    private volatile boolean wantLock = false;

    @Override
    public void lock() {
        wantLock = true;
        while (!haveToken) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void unlock() {
        haveToken = false;
        wantLock = false;
        sendTokenForward();
    }

    public void receiveToken() {
        if (wantLock) haveToken = true;
        else sendTokenForward();
    }

    public void sendTokenForward() {
        // TODO:
    }

}
