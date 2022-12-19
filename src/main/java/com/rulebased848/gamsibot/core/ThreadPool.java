package com.rulebased848.gamsibot.core;

import java.util.concurrent.SynchronousQueue;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ThreadPool {
    private static final Logger logger = getLogger(ThreadPool.class);

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
            logger.error("Interrupted while putting a task into the queue.", ie);
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
                    logger.error("Interrupted while taking a task from the queue.", ie);
                } catch (RuntimeException re) {
                    logger.error("Unexpected runtime exception while running a task.", re);
                }
            }
        }
    }
}