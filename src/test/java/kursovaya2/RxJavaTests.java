package kursovaya2;

import kursovaya2.rx.BooleanDisposable;
import kursovaya2.rx.Disposable;
import kursovaya2.rx.Function;
import kursovaya2.rx.Observable;
import kursovaya2.rx.Observer;
import kursovaya2.scheduler.ComputationScheduler;
import kursovaya2.scheduler.IOThreadScheduler;
import kursovaya2.scheduler.SingleThreadScheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

// Юнит тесты для RxJava реализации
public class RxJavaTests {

    @Test
    void testBasicObservable() {
        System.out.println("Тест 1: Базовый Observable");
        List<Integer> results = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Ошибка: " + t.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });

        assertEquals(3, results.size());
        assertEquals(1, results.get(0));
        System.out.println(" Пройден\n");
    }

    @Test
    void testMapOperator() {
        System.out.println("Тест 2: Map оператор");
        List<Integer> results = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.map(x -> x * 10)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertEquals(3, results.size());
        assertEquals(10, results.get(0));
        assertEquals(20, results.get(1));
        assertEquals(30, results.get(2));
        System.out.println(" Пройден\n");
    }

    @Test
    void testFilterOperator() {
        System.out.println("Тест 3: Filter оператор");
        List<Integer> results = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 1; i <= 5; i++) observer.onNext(i);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.filter(x -> x % 2 == 0)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertEquals(2, results.size());
        assertEquals(2, results.get(0));
        assertEquals(4, results.get(1));
        System.out.println(" Пройден\n");
    }

    @Test
    void testFlatMapOperator() throws InterruptedException {
        System.out.println("Тест 4: FlatMap оператор");
        List<Integer> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.flatMap((Function<Integer, Observable<Integer>>) x -> Observable.create(inner -> {
            inner.onNext(x * 10);
            inner.onNext(x * 100);
            inner.onComplete();
            return new BooleanDisposable();
        })).subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(item);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);

        assertEquals(4, results.size());
        assertTrue(results.contains(10));
        assertTrue(results.contains(100));
        assertTrue(results.contains(20));
        assertTrue(results.contains(200));
        System.out.println(" Пройден\n");
    }

    @Test
    void testSubscribeOnIOThreadScheduler() throws InterruptedException {
        System.out.println("Тест 5: SubscribeOn с IOThreadScheduler");
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        IOThreadScheduler scheduler = new IOThreadScheduler();

        Observable<String> source = Observable.create(observer -> {
            observer.onNext(Thread.currentThread().getName());
            observer.onComplete();
            return new BooleanDisposable();
        });

        try {
            source.subscribeOn(scheduler)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onNext(String item) {
                            results.add(item);
                        }

                        @Override
                        public void onError(Throwable t) {
                        }

                        @Override
                        public void onComplete() {
                            latch.countDown();
                        }
                    });

            latch.await(2, TimeUnit.SECONDS);

            assertEquals(1, results.size());
            assertTrue(results.get(0).contains("IOThread"));
            System.out.println(" Пройден\n");
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void testSubscribeOnComputationScheduler() throws InterruptedException {
        System.out.println("Тест 6: SubscribeOn с ComputationScheduler");
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        ComputationScheduler scheduler = new ComputationScheduler();

        Observable<String> source = Observable.create(observer -> {
            observer.onNext(Thread.currentThread().getName());
            observer.onComplete();
            return new BooleanDisposable();
        });

        try {
            source.subscribeOn(scheduler)
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onNext(String item) {
                            results.add(item);
                        }

                        @Override
                        public void onError(Throwable t) {
                        }

                        @Override
                        public void onComplete() {
                            latch.countDown();
                        }
                    });

            latch.await(2, TimeUnit.SECONDS);

            assertEquals(1, results.size());
            assertTrue(results.get(0).contains("ComputationThread"));
            System.out.println(" Пройден\n");
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void testObserveOn() throws InterruptedException {
        System.out.println("Тест 7: ObserveOn оператор");
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onComplete();
            return new BooleanDisposable();
        });

        try {
            source.observeOn(scheduler)
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onNext(Integer item) {
                            results.add(Thread.currentThread().getName());
                        }

                        @Override
                        public void onError(Throwable t) {
                        }

                        @Override
                        public void onComplete() {
                            latch.countDown();
                        }
                    });

            latch.await(2, TimeUnit.SECONDS);

            assertEquals(2, results.size());
            assertEquals(results.get(0), results.get(1));
            System.out.println(" Пройден\n");
        } finally {
            scheduler.shutdown();
        }
    }

    @Test
    void testErrorHandling() {
        System.out.println("Тест 8: Обработка ошибок");
        List<String> results = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onError(new RuntimeException("Test error"));
            return new BooleanDisposable();
        });

        source.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                results.add(String.valueOf(item));
            }

            @Override
            public void onError(Throwable t) {
                results.add("Error: " + t.getMessage());
            }

            @Override
            public void onComplete() {
            }
        });

        assertEquals(3, results.size());
        assertEquals("Error: Test error", results.get(2));
        System.out.println(" Пройден\n");
    }

    @Test
    void testChainedOperators() {
        System.out.println("Тест 9: Цепочка операторов (map + filter)");
        List<Integer> results = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            for (int i = 1; i <= 5; i++) observer.onNext(i);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.map(x -> x * 2)
                .filter(x -> x > 4)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertEquals(3, results.size());
        assertEquals(6, results.get(0));
        assertEquals(8, results.get(1));
        assertEquals(10, results.get(2));
        System.out.println(" Пройден\n");
    }

    @Test
    void testMapErrorTerminatesStream() {
        System.out.println("Тест 10: Ошибка в map завершает поток");
        List<Integer> values = new ArrayList<>();
        List<String> terminal = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.map(x -> {
                    if (x == 2) {
                        throw new RuntimeException("map boom");
                    }
                    return x * 10;
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        values.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        terminal.add("E:" + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        terminal.add("C");
                    }
                });

        assertEquals(List.of(10), values);
        assertEquals(List.of("E:map boom"), terminal);
        System.out.println(" Пройден\n");
    }

    @Test
    void testFilterErrorTerminatesStream() {
        System.out.println("Тест 11: Ошибка в filter завершает поток");
        List<Integer> values = new ArrayList<>();
        List<String> terminal = new ArrayList<>();

        Observable<Integer> source = Observable.create(observer -> {
            observer.onNext(1);
            observer.onNext(2);
            observer.onNext(3);
            observer.onComplete();
            return new BooleanDisposable();
        });

        source.filter(x -> {
                    if (x == 2) {
                        throw new RuntimeException("filter boom");
                    }
                    return x % 2 != 0;
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        values.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        terminal.add("E:" + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        terminal.add("C");
                    }
                });

        assertEquals(List.of(1), values);
        assertEquals(List.of("E:filter boom"), terminal);
        System.out.println(" Пройден\n");
    }

    @Test
    void testDisposableStopsAsyncEmissions() throws InterruptedException {
        System.out.println("Тест 12: Disposable останавливает асинхронную подписку");
        List<Integer> values = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch firstValue = new CountDownLatch(1);

        Observable<Integer> source = Observable.create(observer -> {
            BooleanDisposable d = new BooleanDisposable();
            Thread worker = new Thread(() -> {
                for (int i = 1; i <= 100; i++) {
                    if (d.isDisposed()) {
                        return;
                    }
                    observer.onNext(i);
                    if (i == 1) {
                        firstValue.countDown();
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                observer.onComplete();
            }, "dispose-test-worker");
            worker.start();
            return d;
        });

        Disposable disposable = source.subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer item) {
                values.add(item);
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });

        assertTrue(firstValue.await(1, TimeUnit.SECONDS));
        disposable.dispose();
        Thread.sleep(80);

        int sizeAfterDispose = values.size();
        Thread.sleep(80);

        assertTrue(disposable.isDisposed());
        assertEquals(sizeAfterDispose, values.size());
        assertTrue(values.size() < 100);
        System.out.println(" Пройден\n");
    }
}
