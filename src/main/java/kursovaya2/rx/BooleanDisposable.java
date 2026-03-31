package kursovaya2.rx;

import java.util.concurrent.atomic.AtomicBoolean;

// Реализация Disposable на базе флага
public final class BooleanDisposable implements Disposable {
    private final AtomicBoolean disposed = new AtomicBoolean(false);

    @Override
    public void dispose() {
        disposed.set(true);
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
