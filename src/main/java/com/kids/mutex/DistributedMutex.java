package com.kids.mutex;

import java.util.List;

/**
 * DistributedMutex interface
 * <p>
 * This interface defines the methods for a distributed mutex implementation.
 * It is used to control access to a critical section in a distributed system.
 * <p>
 * Type parameters:
 * <N> - Type of node identifier
 * <T> - Type of token used in the mutex implementation
 */
public interface DistributedMutex<N, T> {

    /**
     * This method is called when a node wants to acquire a lock.
     * It will first check if it has the token, if not it will request it.
     * If it has the token, it will acquire the lock.
     *
     * @param broadcastNodes List of nodes to broadcast the token request to
     * @param isPriority If true, the node will not wait for the token
     */
    void lock(List<N> broadcastNodes, boolean isPriority);

    /**
     * This method is called when a node wants to release a lock.
     * It will release the lock and send the token to the next node in the queue.
     */
    void unlock();

    /**
     * Sets the token for this mutex implementation
     *
     * @param token The token to set
     */
    void setToken(T token);

    /**
     * Checks if this node currently has the token
     *
     * @return true if the node has the token, false otherwise
     */
    boolean hasToken();

    /**
     * Sets the token for this mutex implementation
     *
     * @param hasToken flag to set.
     */
    void setHasToken(boolean hasToken);

    /**
     * Checks if this node is currently using the token
     *
     * @return true if the node is using the token, false otherwise
     */
    boolean usesToken();

    /**
     * Sets the token for this mutex implementation
     *
     * @param usesToken flag to set.
     */
    void setUsesToken(boolean usesToken);

}