package com.aengdulab.distributedmail;

import java.util.concurrent.CountDownLatch;

public class GlobalLatch {

    private final CountDownLatch latch;

    public GlobalLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void countDown() {
        latch.countDown();
    }
}
