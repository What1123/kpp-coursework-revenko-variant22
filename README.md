# Курсова робота з дисципліни «Кросплатформене програмування»

**Варіант №22:** Інформаційна система сервісного обслуговування автомобілів  
**Виконала:** Ревенко Анна Володимирівна, група 203-ТН  
**Навчальний рік:** 2026–2027

> Стан репозиторію: **етап 1** (аналіз завдання, проєктування БД, початкова структура Maven-проєктів).

## Предметна модель

| Сутність | Таблиця БД | Клас Java |
|---|---|---|
| Клієнт | `client` | `Client` |
| Автомобіль | `car` | `Car` |
| Сервісне замовлення | `service_order` | `ServiceOrder` (+ `OrderState`) |
| Робота | `work` | `Work` |

Зв'язки (1:N): `client → car → service_order → work`.

Життєвий цикл сервісного замовлення: **СТВОРЕНО (CREATED) → ДІАГНОСТОВАНО (DIAGNOSED) → РЕМОНТ (REPAIR) → ВИКОНАНО (COMPLETED)**.

Інтегральний показник: `I = 0,40·F1 + 0,35·F2 + 0,25·F3` (F1 — вплив на безпеку, F2 — вплив на мобільність, F3 — тривалість очікування; усі фактори нормуються до діапазону 0–100).

## Вимоги до середовища

- JDK 21, Apache Maven, IntelliJ IDEA
- MySQL / MariaDB (XAMPP) і phpMyAdmin
- Apache Tomcat 9.x (з етапу реалізації сервера)

## Структура проєкту

```
kpp-coursework-revenko-variant22/
├─ database.sql              # створення БД car_service з нуля + демонстраційні дані
├─ server/                   # Maven-проєкт серверної частини
│  ├─ pom.xml
│  └─ src/main/java/ua/edu/nupp/kpp/server/
│     ├─ DBUtil.java         # підключення до MySQL (JDBC)
│     └─ model/              # Client, Car, ServiceOrder, Work, OrderState
└─ client/                   # Maven-проєкт клієнтської частини (JavaFX додається пізніше)
   ├─ pom.xml
   └─ sample-data/cars.csv   # початковий CSV: реєстр автомобілів
```

## Створення бази даних

1. Запустіть **Apache** і **MySQL** у XAMPP Control Panel.
2. Відкрийте `http://localhost/phpmyadmin` → вкладка **Імпорт** → оберіть `database.sql` → **Імпорт**.
3. Має з'явитися база `car_service` із таблицями `client`, `car`, `service_order`, `work`.

Скрипт можна виконувати повторно: він видаляє стару базу `car_service` і створює її заново.

## Перевірка підключення до MySQL

```
cd server
mvn clean compile
```

Потім у IntelliJ IDEA запустіть метод `main()` класу `ua.edu.nupp.kpp.server.DBUtil`.
Очікуваний результат: `Підключення до MySQL успішне.`
Параметри підключення (URL, користувач, пароль) задані в `DBUtil`; для XAMPP за замовчуванням — `root` без пароля.

## Формат CSV (реєстр автомобілів)

Файл `client/sample-data/cars.csv`, роздільник `;`, перший рядок — заголовок:

`client_id;vin;license_plate;brand;model;production_year;mileage_km`

Фоновий імпорт через JavaFX Task буде реалізовано на наступних етапах.

> Усі дані в `database.sql` і `cars.csv` вигадані й використовуються лише для навчання.
