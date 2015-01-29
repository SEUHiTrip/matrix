package com.idisplay.DataChannelManager;

import com.idisplay.GlobalCommunicationStructures.MSMTouchEvent;
import com.idisplay.VirtualScreenDisplay.ThreadEvent;
import com.idisplay.util.Logger;
import java.util.LinkedList;
import java.util.Queue;

class TouchEventTask extends Thread {
    private ThreadEvent TOUCH_WAIT;
    private String className;
    private volatile boolean m_stopProcess;
    private TouchEventSendListener touchEventSendListener;
    private Queue<MSMTouchEvent> touchQueue;
    private volatile boolean waiting_for_data;

    TouchEventTask() {
        this.TOUCH_WAIT = new ThreadEvent();
        this.waiting_for_data = false;
        this.touchQueue = new LinkedList();
        this.touchEventSendListener = null;
        this.className = "TouchEventTask";
        this.m_stopProcess = false;
    }

    public void run() {
        while (!this.m_stopProcess) {
            try {
                if (this.touchQueue.isEmpty()) {
                    this.waiting_for_data = true;
                    this.TOUCH_WAIT.await();
                    if (this.m_stopProcess) {
                        Logger.d(this.className + ":Stopping TouchEventTask");
                    }
                }
                this.touchEventSendListener.sendTouchEventTaskListener((MSMTouchEvent) this.touchQueue.remove());
            } catch (Throwable e) {
                Logger.e(getClass().getName(), e);
            }
        }
    }

    public void setTouchEvent(MSMTouchEvent mSMTouchEvent) {
        this.touchQueue.add(mSMTouchEvent);
        if (this.waiting_for_data) {
            this.waiting_for_data = false;
            this.TOUCH_WAIT.signal();
        }
        Logger.d(this.className + ":after add elements in TouchEventTask queue = " + this.touchQueue.size());
    }

    public void setTouchEventSendListener(TouchEventSendListener touchEventSendListener) {
        this.touchEventSendListener = touchEventSendListener;
    }

    public void stopProcess() {
        this.m_stopProcess = true;
        this.TOUCH_WAIT.signal();
    }
}
