package com.rulebased848.gamsibot.core;

import java.util.concurrent.SynchronousQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ThreadPool {
    private final Worker[] threads;

    private final SynchronousQueue<Runnable> queue = new SynchronousQueue<>();

    public ThreadPool(@Value("${pool.thread-count}") int threadCount) {
        threads = new Worker[threadCount];
        for (int i = 0; i < threadCount; ++i) {
            threads[i] = new Worker();
            threads[i].start();
        }
    }

    public void execute(Runnable task) {
        try {
            queue.put(task);
        } catch (InterruptedException ie) {
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task = queue.take();
                    task.run();
                } catch (InterruptedException ie) {
                } catch (RuntimeException re) {
                }
            }
        }
    }
}