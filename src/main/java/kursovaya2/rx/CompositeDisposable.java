package kursovaya2.rx;

import java.util.ArrayList;
import java.util.List;

// Группа Disposable
public final class CompositeDisposable implements Disposable {
    private final List<Disposable> disposables = new ArrayList<>();
    private volatile boolean disposed;

    public synchronized void add(Disposable disposable) {
        if (disposed) {
            disposable.dispose();
            return;
        }
        disposables.add(disposable);
    }

    @Override
    public synchronized void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
