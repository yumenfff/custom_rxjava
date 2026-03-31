package kursovaya2.rx;

// Контракт для создания Observable
@FunctionalInterface
public interface ObservableSource<T> {

// Подписывает observer на поток данных
    Disposable subscribe(Observer<? super T> observer);
}
