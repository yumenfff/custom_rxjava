package kursovaya2.rx;

// Функциональный интерфейс для преобразования данных
@FunctionalInterface
public interface Function<T, R> {

// Применяет функцию к входному аргументу
    R apply(T t);
}
