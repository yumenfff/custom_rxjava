package kursovaya2.rx;

import kursovaya2.scheduler.Scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// Основной класс для создания потоков данных и применения операторов
public abstract class Observable<T> implements ObservableSource<T> {

// Подписывает observer на поток
    @Override
    public abstract Disposable subscribe(Observer<? super T> observer);

// Создает новый Observable из источника данных
    public static <T> Observable<T> create(ObservableSource<T> source) {
        return new Observable<T>() {
            @Override
            public Disposable subscribe(Observer<? super T> observer) {
                return source.subscribe(observer);
            }
        };
    }

// Оператор map преобразует элементы потока
    public <R> Observable<R> map(Function<T, R> mapper) {
        return new Observable<R>() {
            @Override
            public Disposable subscribe(Observer<? super R> observer) {
                BooleanDisposable disposed = new BooleanDisposable();
                AtomicBoolean terminated = new AtomicBoolean(false);
                Disposable upstream = Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        if (disposed.isDisposed() || terminated.get()) {
                            return;
                        }
                        try {
                            R result = mapper.apply(item);
                            observer.onNext(result);
                        } catch (Throwable e) {
                            if (terminated.compareAndSet(false, true)) {
                                disposed.dispose();
                                observer.onError(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (!disposed.isDisposed() && terminated.compareAndSet(false, true)) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (!disposed.isDisposed() && terminated.compareAndSet(false, true)) {
                            observer.onComplete();
                        }
                    }
                });
                return wrap(upstream, disposed);
            }
        };
    }

// Оператор filter фильтрует элементы потока
    public Observable<T> filter(Predicate<T> predicate) {
        return new Observable<T>() {
            @Override
            public Disposable subscribe(Observer<? super T> observer) {
                BooleanDisposable disposed = new BooleanDisposable();
                AtomicBoolean terminated = new AtomicBoolean(false);
                Disposable upstream = Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        if (disposed.isDisposed() || terminated.get()) {
                            return;
                        }
                        try {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        } catch (Throwable e) {
                            if (terminated.compareAndSet(false, true)) {
                                disposed.dispose();
                                observer.onError(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (!disposed.isDisposed() && terminated.compareAndSet(false, true)) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (!disposed.isDisposed() && terminated.compareAndSet(false, true)) {
                            observer.onComplete();
                        }
                    }
                });
                return wrap(upstream, disposed);
            }
        };
    }

// Оператор flatMap преобразует элементы в новые Observable и объединяет их
    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return new Observable<R>() {
            @Override
            public Disposable subscribe(Observer<? super R> observer) {
                CompositeDisposable all = new CompositeDisposable();
                AtomicInteger activeCount = new AtomicInteger(1);
                AtomicBoolean terminated = new AtomicBoolean(false);

                Disposable upstream = Observable.this.subscribe(new Observer<T>() {

                    @Override
                    public void onNext(T item) {
                        if (all.isDisposed() || terminated.get()) {
                            return;
                        }
                        try {
                            Observable<R> inner = mapper.apply(item);
                            activeCount.incrementAndGet();

                            Disposable innerDisposable = inner.subscribe(new Observer<R>() {
                                @Override
                                public void onNext(R value) {
                                    if (!all.isDisposed() && !terminated.get()) {
                                        observer.onNext(value);
                                    }
                                }

                                @Override
                                public void onError(Throwable t) {
                                    if (terminated.compareAndSet(false, true) && !all.isDisposed()) {
                                        observer.onError(t);
                                        all.dispose();
                                    }
                                }

                                @Override
                                public void onComplete() {
                                    if (activeCount.decrementAndGet() == 0 && terminated.compareAndSet(false, true) && !all.isDisposed()) {
                                        observer.onComplete();
                                    }
                                }
                            });
                            all.add(innerDisposable);
                        } catch (Throwable e) {
                            if (terminated.compareAndSet(false, true) && !all.isDisposed()) {
                                observer.onError(e);
                                all.dispose();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (terminated.compareAndSet(false, true) && !all.isDisposed()) {
                            observer.onError(t);
                            all.dispose();
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (activeCount.decrementAndGet() == 0 && terminated.compareAndSet(false, true) && !all.isDisposed()) {
                            observer.onComplete();
                        }
                    }
                });
                all.add(upstream);
                return all;
            }
        };
    }

// Оператор subscribeOn подписывает Observable в заданном потоке
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            public Disposable subscribe(Observer<? super T> observer) {
                BooleanDisposable disposed = new BooleanDisposable();
                final Object lock = new Object();
                final Disposable[] upstreamHolder = new Disposable[1];

                scheduler.execute(() -> {
                    if (disposed.isDisposed()) {
                        return;
                    }
                    Disposable upstream = Observable.this.subscribe(observer);
                    synchronized (lock) {
                        upstreamHolder[0] = upstream;
                    }
                    if (disposed.isDisposed()) {
                        upstream.dispose();
                    }
                });

                return new Disposable() {
                    @Override
                    public void dispose() {
                        disposed.dispose();
                        synchronized (lock) {
                            if (upstreamHolder[0] != null) {
                                upstreamHolder[0].dispose();
                            }
                        }
                    }

                    @Override
                    public boolean isDisposed() {
                        return disposed.isDisposed();
                    }
                };
            }
        };
    }

// Оператор observeOn обрабатывает события в заданном потоке
    public Observable<T> observeOn(Scheduler scheduler) {
        return new Observable<T>() {
            @Override
            public Disposable subscribe(Observer<? super T> observer) {
                BooleanDisposable disposed = new BooleanDisposable();
                ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
                AtomicBoolean done = new AtomicBoolean(false);
                AtomicReference<Throwable> error = new AtomicReference<>(null);
                AtomicInteger wip = new AtomicInteger(0);

                Runnable drain = () -> {
                    if (wip.getAndIncrement() != 0) {
                        return;
                    }

                    int missed = 1;
                    for (;;) {
                        if (disposed.isDisposed()) {
                            queue.clear();
                            return;
                        }

                        boolean isDone = done.get();
                        T item = queue.poll();
                        boolean empty = item == null;

                        if (isDone && empty) {
                            Throwable throwable = error.get();
                            if (throwable != null) {
                                observer.onError(throwable);
                            } else {
                                observer.onComplete();
                            }
                            disposed.dispose();
                            return;
                        }

                        if (!empty) {
                            observer.onNext(item);
                            continue;
                        }

                        missed = wip.addAndGet(-missed);
                        if (missed == 0) {
                            break;
                        }
                    }
                };

                Disposable upstream = Observable.this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        if (disposed.isDisposed() || done.get()) {
                            return;
                        }
                        queue.offer(item);
                        scheduler.execute(drain);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (disposed.isDisposed() || !done.compareAndSet(false, true)) {
                            return;
                        }
                        error.set(t);
                        scheduler.execute(drain);
                    }

                    @Override
                    public void onComplete() {
                        if (disposed.isDisposed() || !done.compareAndSet(false, true)) {
                            return;
                        }
                        scheduler.execute(drain);
                    }
                });
                return wrap(upstream, disposed);
            }
        };
    }

    private static Disposable wrap(Disposable upstream, BooleanDisposable disposed) {
        return new Disposable() {
            @Override
            public void dispose() {
                disposed.dispose();
                upstream.dispose();
            }

            @Override
            public boolean isDisposed() {
                return disposed.isDisposed() || upstream.isDisposed();
            }
        };
    }
}
