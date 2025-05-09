package com.kids.mutex;

public interface DistributedMutex {
    void lock();
    void unlock();
}
