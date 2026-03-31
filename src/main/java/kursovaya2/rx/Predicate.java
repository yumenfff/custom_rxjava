package kursovaya2.rx;

// Функциональный интерфейс для фильтрации
@FunctionalInterface
public interface Predicate<T> {

// Проверяет условие для элемента
    boolean test(T t);
}
