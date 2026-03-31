package kursovaya2.demo;

import kursovaya2.rx.BooleanDisposable;
import kursovaya2.rx.Function;
import kursovaya2.rx.Observable;
import kursovaya2.rx.Observer;
import kursovaya2.scheduler.ComputationScheduler;
import kursovaya2.scheduler.IOThreadScheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// Демонстрационная программа для RxJava реализации
public class Demo {

    public static void main(String[] args) {
        System.out.println("=== RxJava Demo ===\n");

        IOThreadScheduler ioScheduler = new IOThreadScheduler();
        ComputationScheduler computationScheduler = new ComputationScheduler();

        try {

        // Пример 1: базовый Observable с map
        System.out.println("1. Map оператор:");
        demonstrateMap();

        // Пример 2: Filter оператор
        System.out.println("\n2. Filter оператор:");
        demonstrateFilter();

        // Пример 3: FlatMap оператор
        System.out.println("\n3. FlatMap оператор:");
        demonstrateFlatMap();

        // Пример 4: SubscribeOn с IOThreadScheduler
        System.out.println("\n4. SubscribeOn с IOThreadScheduler:");
        demonstrateSubscribeOn(ioScheduler);

        // Пример 5: ObserveOn с ComputationScheduler
        System.out.println("\n5. ObserveOn с ComputationScheduler:");
        demonstrateObserveOn(computationScheduler);


        System.out.println("\n=== Demo завершено ===");
        } finally {
            ioScheduler.shutdown();
            computationScheduler.shutdown();
        }
    }

    private static void demonstrateMap() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.map(x -> x * 10)
              .subscribe(createPrintingObserver());
    }

    private static void demonstrateFilter() {
        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 1; i <= 5; i++) {
                observer.onNext(i);
            }
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.filter(x -> x % 2 == 0)
              .subscribe(createPrintingObserver());
    }

    private static void demonstrateFlatMap() {
        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.flatMap((Function<Integer, Observable<Integer>>) x -> Observable.create(innerObserver -> {
                   innerObserver.onNext(x * 10);
                   innerObserver.onNext(x * 100);
                   innerObserver.onComplete();
                   return new BooleanDisposable();
               }))
              .subscribe(createPrintingObserver());
    }

    private static void demonstrateSubscribeOn(IOThreadScheduler scheduler) {
        CountDownLatch done = new CountDownLatch(1);
        Observable<String> source = Observable.create(observer -> {
            System.out.println("Выполняется в потоке: " + Thread.currentThread().getName());
            observer.onNext("Hello");
            observer.onNext("World");
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.subscribeOn(scheduler)
              .subscribe(new Observer<String>() {
                  @Override
                  public void onNext(String item) {
                      System.out.println("  -> " + item);
                  }

                  @Override
                  public void onError(Throwable t) {
                      System.err.println("Ошибка: " + t.getMessage());
                      done.countDown();
                  }

                  @Override
                  public void onComplete() {
                      System.out.println("Завершено");
                      done.countDown();
                  }
              });

        awaitCompletion(done);
    }

    private static void demonstrateObserveOn(ComputationScheduler scheduler) {
        CountDownLatch done = new CountDownLatch(1);
        Observable<Integer> source = Observable.create(observer -> {
            System.out.println("Source в потоке: " + Thread.currentThread().getName());
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.observeOn(scheduler)
              .subscribe(new Observer<Integer>() {
                  @Override
                  public void onNext(Integer item) {
                      System.out.println("Observer в потоке: " + Thread.currentThread().getName() + 
                                       ", значение: " + item);
                  }

                  @Override
                  public void onError(Throwable t) {
                      System.err.println("Ошибка: " + t.getMessage());
                      done.countDown();
                  }

                  @Override
                  public void onComplete() {
                      System.out.println("Завершено");
                      done.countDown();
                  }
              });

        awaitCompletion(done);
    }

    private static void awaitCompletion(CountDownLatch done) {
        try {
            if (!done.await(3, TimeUnit.SECONDS)) {
                System.err.println("Ожидание завершения заняло слишком много времени");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ожидание было прервано");
        }
    }

    private static <T> Observer<T> createPrintingObserver() {
        return new Observer<T>() {
            @Override
            public void onNext(T item) {
                System.out.println("  -> " + item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
                System.out.println("Завершено");
            }
        };
    }
}
