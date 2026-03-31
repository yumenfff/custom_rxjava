package kursovaya2.rx;

// Интерфейс Observer для получения событий от Observable
 public interface Observer<T> {

// Вызывается при получении нового элемента
    void onNext(T item);

// Вызывается при возникновении ошибки
    void onError(Throwable t);

// Вызывается при завершении потока
    void onComplete();
}
