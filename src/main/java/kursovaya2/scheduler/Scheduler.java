package kursovaya2.scheduler;

// Интерфейс для планирования выполнения задач в отдельных потоках
public interface Scheduler {

// Выполняет задачу в потоке scheduler
    void execute(Runnable task);

// Завершает работу scheduler
    void shutdown();
}
