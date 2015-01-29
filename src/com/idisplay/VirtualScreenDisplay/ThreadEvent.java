package com.idisplay.VirtualScreenDisplay;

public class ThreadEvent {
    private final Object lock;

    public ThreadEvent() {
        this.lock = new Object();
    }

    public void await() throws InterruptedException {
        synchronized (this.lock) {
            this.lock.wait();
        }
    }

    public void signal() {
        synchronized (this.lock) {
            this.lock.notify();
        }
    }
}
