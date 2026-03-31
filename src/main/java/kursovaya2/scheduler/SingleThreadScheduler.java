package kursovaya2.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Scheduler с одним потоком для выполнения операций последовательно
public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executorService;

    public SingleThreadScheduler() {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SingleThread");
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
