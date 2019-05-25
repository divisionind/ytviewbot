package com.anonymous.ytvb;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ViewBotFactory implements ThreadFactory {

    private AtomicInteger number;

    public ViewBotFactory() {
        this.number = new AtomicInteger(0);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "ViewBot-" + number.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
