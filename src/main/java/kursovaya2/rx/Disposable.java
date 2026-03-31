package kursovaya2.rx;

// Интерфейс для управления подписками и ресурсами
public interface Disposable {

// Отменяет подписку и освобождает ресурсы
    void dispose();

// Проверяет, была ли подписка отменена
    boolean isDisposed();
}
