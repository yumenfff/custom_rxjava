# Курсовая работа (Часть 2): Реализация RxJava библиотеки

## Что реализовано

Во второй части курсовой работы реализована собственная версия библиотеки RxJava, которая демонстрирует основные
концепции реактивного программирования. Проект включает базовые компоненты реактивного потока, поддержку асинхронного
выполнения, обработку ошибок и операторы преобразования данных

---

## Структура проекта

```
kursovaya_2/
├── src/
│   ├── main/java/kursovaya2/
│   │   ├── rx/
│   │   │   ├── Observer.java            - интерфейс наблюдателя
│   │   │   ├── Disposable.java          - управление подписками
│   │   │   ├── BooleanDisposable.java   - флаговая реализация Disposable
│   │   │   ├── CompositeDisposable.java - группа Disposable
│   │   │   ├── ObservableSource.java    - контракт источника
│   │   │   ├── Observable.java          - основной класс с операторами
│   │   │   ├── Function.java            - функциональный интерфейс
│   │   │   └── Predicate.java           - интерфейс фильтрации
│   │   ├── scheduler/
│   │   │   ├── Scheduler.java             - интерфейс планировщика
│   │   │   ├── IOThreadScheduler.java     - для I/O операций
│   │   │   ├── ComputationScheduler.java  - для вычислений
│   │   │   └── SingleThreadScheduler.java - один поток
│   │   └── demo/
│   │       └── Demo.java                - демонстрационная программа
│   └── test/java/kursovaya2/
│       └── RxJavaTests.java             - юнит-тесты
└── pom.xml
└── README.md
```

---

## Архитектура проекта

Проект разделен на два основных модуля:

### 1. Модуль RX (`kursovaya2.rx`)

Основные компоненты реактивной системы:

1. #### Observer<T> - Интерфейс наблюдателя

```java
public interface Observer<T> {
  void onNext(T item);  // Получение нового элемента

  void onError(Throwable t);  // Обработка ошибки

  void onComplete();  // Сигнал завершения потока
}
```

**Назначение**: Определяет контракт для получения событий от источника данных.
Любой наблюдатель должен реализовать три метода для обработки различных состояний потока

2. #### ObservableSource<T> - Контракт источника

```java

@FunctionalInterface
public interface ObservableSource<T> {
  Disposable subscribe(Observer<? super T> observer);
}
```

**Назначение**: Функциональный интерфейс для создания источника данных, к которому могут подписаться наблюдатели

3. #### Observable<T> - Основной класс реактивного потока

**Ключевые методы**:

- **create()** - создание Observable из источника
  - Позволяет создать поток данных из любого источника
  - Принимает ObservableSource в качестве аргумента

- **map(Function<T, R> mapper)** - оператор преобразования
  - Преобразует каждый элемент в новый элемент
  - Пример: `observable.map(x -> x * 2)`
  - Возвращает новый Observable с преобразованными данными

- **filter(Predicate<T> predicate)** - оператор фильтрации
  - Пропускает только элементы, удовлетворяющие условию
  - Пример: `observable.filter(x -> x > 5)`
  - Отфильтровывает нежелательные элементы

- **flatMap(Function<T, Observable<R>> mapper)** - оператор плоского отображения
  - Преобразует каждый элемент в Observable и объединяет результаты
  - Используется для работы с вложенными потоками
  - Управляет асинхронностью вложенных потоков

- **subscribeOn(Scheduler scheduler)** - выполнение подписки в потоке
  - Подписка на Observable выполняется в заданном потоке scheduler
  - Влияет на поток, где создаются события
  - Пример: `observable.subscribeOn(new IOThreadScheduler())`

- **observeOn(Scheduler scheduler)** - обработка событий в потоке
  - События передаются observer в заданном потоке
  - Влияет на поток обработки событий
  - Пример: `observable.observeOn(new ComputationScheduler())`

4. #### Disposable - Управление подписками

```java
public interface Disposable {
  void dispose();  // Отменить подписку

  boolean isDisposed();  // Проверить статус
}
```

**Назначение**: Позволяет управлять жизненным циклом подписок и освобождать ресурсы

5. #### Function<T, R> и Predicate<T> - Функциональные интерфейсы

**Назначение**: Используются для передачи логики преобразования и фильтрации в операторах

---

### 2. Модуль Scheduler (`kursovaya2.scheduler`)

Система планирования выполнения задач в отдельных потоках

1. #### Scheduler - Интерфейс планировщика

```java
public interface Scheduler {
  void execute(Runnable task);  // Выполнить задачу
  void shutdown();  // Завершить scheduler и освободить потоки
}
```

**Назначение**: Определяет контракт для планирования асинхронного выполнения

2. #### IOThreadScheduler - Для I/O операций

- **Использует**: CachedThreadPool
- **Назначение**: Предназначен для операций, которые блокируют поток (I/O, сетевые запросы)
- **Характеристики**:
  - Поток переиспользуется, если он свободен в течение 60 секунд
  - Создает новые потоки при необходимости
  - Оптимален для операций с ожиданием

3. #### ComputationScheduler - Для вычислений

- **Использует**: FixedThreadPool с количеством потоков = числу ядер процессора
- **Назначение**: Предназначен для CPU-bound операций
- **Характеристики**:
  - Фиксированное количество потоков
  - Предотвращает чрезмерное создание потоков
  - Оптимален для интенсивных вычислений

4. #### SingleThreadScheduler - Последовательное выполнение

- **Использует**: SingleThreadExecutor
- **Назначение**: Выполнение операций в одном потоке последовательно
- **Характеристики**:
  - Все задачи выполняются в одном потоке
  - Гарантирует порядок выполнения
  - Полезен для сохранения состояния

---

## Принцип работы операторов

- ### Map

```
Источник: 1 -> 2 -> 3
Map(*10): 10 -> 20 -> 30
```

Каждый элемент преобразуется функцией mapper перед отправкой observer

- ### Filter

```
Источник: 1 -> 2 -> 3 -> 4 -> 5
Filter(чётные): 2 -> 4
```

Только элементы, удовлетворяющие условию, передаются observer

- ### FlatMap

```
Источник: 1 -> 2
Mapper: [10,100] [20,200]
Result: 10 -> 100 -> 20 -> 200
```

Каждый элемент преобразуется в Observable, и результаты объединяются.

- ### SubscribeOn vs ObserveOn

**SubscribeOn**:

- Выполняет саму подписку в заданном потоке
- Влияет на поток, где начинает работать Observable
- Обычно вызывается один раз в цепочке

**ObserveOn**:

- Переводит обработку событий в заданный поток
- Влияет на поток, где работает Observer
- Может вызываться несколько раз в цепочке для переключения потоков
- Гарантирует порядок событий: все `onNext` перед терминальными сигналами (`onComplete`/`onError`)
- Реализует сериализованный drain-цикл для корректной многопоточной обработки

---

## Примеры использования

### Пример 1: Базовое преобразование

```java
Observable<Integer> source = Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onNext(3);
    observer.onComplete();
    return new BooleanDisposable();
});

source
    .map(x -> x * 10)
    .filter(x -> x > 15)
    .subscribe(new Observer<Integer>() {
        @Override
        public void onNext(Integer item) {
            System.out.println("Значение: " + item);
        }

        @Override
        public void onError(Throwable t) {
            t.printStackTrace();
        }

        @Override
        public void onComplete() {
            System.out.println("Готово");
        }
    });
```

### Пример 2: Асинхронное выполнение

```java
Observable<String> data = Observable.create(observer -> {
    observer.onNext("Hello");
    observer.onNext("World");
    observer.onComplete();
    return new BooleanDisposable();
});

IOThreadScheduler io = new IOThreadScheduler();
ComputationScheduler computation = new ComputationScheduler();

Observer<String> observer = new Observer<>() {
    @Override
    public void onNext(String item) {
        System.out.println(item);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @Override
    public void onComplete() {
        System.out.println("Готово");
    }
};

try {
    data
        .subscribeOn(io)
        .observeOn(computation)
        .subscribe(observer);
} finally {
    io.shutdown();
    computation.shutdown();
}
```

### Пример 3: FlatMap для вложенных операций

```java
Observable<Integer> users = Observable.create(observer -> {
    observer.onNext(1);
    observer.onNext(2);
    observer.onComplete();
    return new BooleanDisposable();
});

users
    .flatMap(userId ->
        Observable.<Integer>create(innerObserver -> {
            innerObserver.onNext(userId * 10);
            innerObserver.onNext(userId * 100);
            innerObserver.onComplete();
            return new BooleanDisposable();
        })
    )
    .subscribe(new Observer<Integer>() {
        @Override
        public void onNext(Integer item) {
            System.out.println(item);
        }

        @Override
        public void onError(Throwable t) {
            System.err.println(t.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("Готово");
        }
    });
```

---

## Тестирование

Для тестирования асинхронных операций используется `CountDownLatch`

1. **testBasicObservable** - проверка базовой функциональности
2. **testMapOperator** - проверка оператора map
3. **testFilterOperator** - проверка оператора filter
4. **testFlatMapOperator** - проверка оператора flatMap с синхронизацией потоков
5. **testSubscribeOnIOThreadScheduler** - проверка IOThreadScheduler
6. **testSubscribeOnComputationScheduler** - проверка ComputationScheduler
7. **testObserveOn** - проверка observeOn с SingleThreadScheduler
8. **testErrorHandling** - проверка обработки ошибок
9. **testChainedOperators** - проверка цепочки операторов
10. **testMapErrorTerminatesStream** - ошибка в map завершает поток без onComplete
11. **testFilterErrorTerminatesStream** - ошибка в filter завершает поток без onComplete
12. **testDisposableStopsAsyncEmissions** - проверка остановки асинхронной подписки через dispose

---

## Сборка и запуск

Требования для запуска:
Maven (для сборки)
Java 21+

```bash
# Запуск тестов
mvn test

# Сборка проекта
mvn compile

# Запуск Demo
mvn compile exec:java
```
