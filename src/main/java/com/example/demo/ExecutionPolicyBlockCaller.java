package com.example.demo;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutionPolicyBlockCaller implements RejectedExecutionHandler {

    private int queueTimeout;

    /**
     * Constructor.
     * @param pPoolQueueTimeout
     */
    public ExecutionPolicyBlockCaller(final int queueTimeout) {
        this.queueTimeout = queueTimeout;
    }

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {

        try {

            // based on the BlockingQueue documentation below should block until able to place on the queue...
            if (!executor.getQueue().offer(r, this.queueTimeout, TimeUnit.SECONDS)) {
                throw new RejectedExecutionException(
                        "Conciliador-Request kafka consumer QUEUE is blocked, timeout waiting for an available place."
                        );
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException(
                    "Unexpected InterruptedException while waiting to add Runnable to ThreadPoolExecutor queue..."
                    , e
                    );
        }
    }
}