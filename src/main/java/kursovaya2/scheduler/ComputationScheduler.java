package kursovaya2.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Scheduler для вычислительных операций с использованием FixedThreadPool (количество потоков равно количеству ядер процессора)
public class ComputationScheduler implements Scheduler {
    private final ExecutorService executorService;
    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

    public ComputationScheduler() {
        this.executorService = Executors.newFixedThreadPool(NUM_THREADS, r -> {
            Thread t = new Thread(r, "ComputationThread-" + System.nanoTime());
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
