package com.idisplay.DataChannelManager;

import com.idisplay.GlobalCommunicationStructures.KeyboardEventStructure;
import com.idisplay.VirtualScreenDisplay.ThreadEvent;
import com.idisplay.util.Logger;
import java.util.LinkedList;
import java.util.Queue;

public class KeyEventTask extends Thread {
    private ThreadEvent KEY_WAIT;
    private String className;
    private KeyEventSendListener keyEventSendListener;
    private Queue<KeyboardEventStructure> keyQueue;
    private boolean m_stopProcess;
    private boolean waiting_for_data;

    public KeyEventTask() {
        super("KeyEventTask Thread");
        this.KEY_WAIT = new ThreadEvent();
        this.waiting_for_data = false;
        this.keyQueue = new LinkedList();
        this.className = "KeyEventTask";
        this.m_stopProcess = false;
        this.keyEventSendListener = null;
    }

    public void run() {
        while (!this.m_stopProcess) {
            try {
                if (this.keyQueue.isEmpty()) {
                    this.waiting_for_data = true;
                    this.KEY_WAIT.await();
                    if (this.m_stopProcess) {
                        Logger.d(this.className + ":Stopping TouchEventTask");
                    }
                }
                KeyboardEventStructure keyboardEventStructure = (KeyboardEventStructure) this.keyQueue.remove();
                Logger.d(this.className + ":After remove elements in KeyEventTask queue = " + this.keyQueue.size());
                this.keyEventSendListener.sendKeyEventListener(keyboardEventStructure);
            } catch (Throwable e) {
                Logger.e(this.className + ":Exception in KeyEventTask " + e.toString(), e);
            }
        }
    }

    public void setKeyEvent(KeyboardEventStructure keyboardEventStructure) {
        this.keyQueue.add(keyboardEventStructure);
        if (this.waiting_for_data) {
            this.waiting_for_data = false;
            this.KEY_WAIT.signal();
        }
        Logger.d(this.className + ":after add elements in KeyEventTask queue = " + this.keyQueue.size());
    }

    public void setKeyEventSendListener(KeyEventSendListener keyEventSendListener) {
        this.keyEventSendListener = keyEventSendListener;
    }

    public void stopProcess() {
        this.m_stopProcess = true;
        this.KEY_WAIT.signal();
    }
}
