package kursovaya2.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Scheduler для I/O операций с использованием CachedThreadPool
public class IOThreadScheduler implements Scheduler {
    private final ExecutorService executorService;

    public IOThreadScheduler() {
        this.executorService = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "IOThread-" + System.nanoTime());
            t.setDaemon(false);
            return t;
        });
    }

    @Override
    public void execute(Runnable task) {
        executorService.execute(task);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}
