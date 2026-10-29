-- ============================================================================
-- Курсова робота з дисципліни «Кросплатформене програмування»
-- Варіант №22: Інформаційна система сервісного обслуговування автомобілів
-- Студентка: Ревенко Анна Володимирівна, група 203-ТН
--
-- Скрипт повністю відтворює базу даних з нуля (його можна виконувати повторно):
--   1) видаляє попередню навчальну базу car_service;
--   2) створює базу та чотири таблиці з PK і FK;
--   3) додає невелику кількість демонстраційних записів.
-- Призначений для MySQL 8 / MariaDB 10.4 (XAMPP, phpMyAdmin -> Імпорт).
-- ============================================================================

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS car_service;
CREATE DATABASE car_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE car_service;

-- Таблиця клієнтів (власників автомобілів)
CREATE TABLE client (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(160) NOT NULL,
    phone     VARCHAR(20)  NOT NULL,
    email     VARCHAR(120) NULL
);

-- Таблиця автомобілів. Зв'язок: client.id -> car.client_id (1:N)
CREATE TABLE car (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id       BIGINT      NOT NULL,
    vin             VARCHAR(17) NOT NULL,
    license_plate   VARCHAR(15) NOT NULL,
    brand           VARCHAR(60) NOT NULL,
    model           VARCHAR(60) NOT NULL,
    production_year INT         NOT NULL,
    mileage_km      INT         NOT NULL,
    CONSTRAINT uq_car_vin   UNIQUE (vin),
    CONSTRAINT uq_car_plate UNIQUE (license_plate),
    CONSTRAINT chk_car_year    CHECK (production_year BETWEEN 1950 AND 2100),
    CONSTRAINT chk_car_mileage CHECK (mileage_km >= 0),
    FOREIGN KEY (client_id) REFERENCES client(id)
);

-- Таблиця сервісних замовлень (об'єкт чотиристанного життєвого циклу).
-- Стани: CREATED -> DIAGNOSED -> REPAIR -> COMPLETED
-- (СТВОРЕНО -> ДІАГНОСТОВАНО -> РЕМОНТ -> ВИКОНАНО).
-- f1, f2, f3 та priority_index розраховуються сервером і мають діапазон 0-100:
--   F1 - вплив на безпеку, F2 - вплив на мобільність, F3 - тривалість очікування,
--   priority_index = 0,40*F1 + 0,35*F2 + 0,25*F3.
-- Зв'язок: car.id -> service_order.car_id (1:N)
CREATE TABLE service_order (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    car_id         BIGINT       NOT NULL,
    description    VARCHAR(500) NOT NULL,
    state          VARCHAR(20)  NOT NULL DEFAULT 'CREATED',
    created_date   DATE         NOT NULL,
    f1             DOUBLE       NOT NULL DEFAULT 0,
    f2             DOUBLE       NOT NULL DEFAULT 0,
    f3             DOUBLE       NOT NULL DEFAULT 0,
    priority_index DOUBLE       NOT NULL DEFAULT 0,
    CONSTRAINT chk_order_state CHECK (state IN ('CREATED', 'DIAGNOSED', 'REPAIR', 'COMPLETED')),
    CONSTRAINT chk_order_f1    CHECK (f1 BETWEEN 0 AND 100),
    CONSTRAINT chk_order_f2    CHECK (f2 BETWEEN 0 AND 100),
    CONSTRAINT chk_order_f3    CHECK (f3 BETWEEN 0 AND 100),
    CONSTRAINT chk_order_index CHECK (priority_index BETWEEN 0 AND 100),
    FOREIGN KEY (car_id) REFERENCES car(id)
);

-- Таблиця робіт сервісного замовлення.
-- safety_score  - оцінка впливу роботи на безпеку (0-100), основа для F1;
-- mobility_score - оцінка впливу роботи на мобільність (0-100), основа для F2;
-- labor_hours   - трудомісткість у нормо-годинах, основа для F3.
-- Зв'язок: service_order.id -> work.order_id (1:N)
CREATE TABLE work (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT         NOT NULL,
    name           VARCHAR(160)   NOT NULL,
    safety_score   DOUBLE         NOT NULL,
    mobility_score DOUBLE         NOT NULL,
    labor_hours    DOUBLE         NOT NULL,
    cost           DECIMAL(10, 2) NOT NULL,
    CONSTRAINT chk_work_safety   CHECK (safety_score BETWEEN 0 AND 100),
    CONSTRAINT chk_work_mobility CHECK (mobility_score BETWEEN 0 AND 100),
    CONSTRAINT chk_work_hours    CHECK (labor_hours > 0),
    CONSTRAINT chk_work_cost     CHECK (cost >= 0),
    FOREIGN KEY (order_id) REFERENCES service_order(id)
);

-- ---------------------------------------------------------------------------
-- Початкові демонстраційні дані (вигадані)
-- ---------------------------------------------------------------------------

INSERT INTO client (full_name, phone, email) VALUES
('Іваненко Петро Олексійович', '+380501112233', 'ivanenko@example.com'),
('Коваль Олена Миколаївна',    '+380672223344', 'koval@example.com'),
('Шевченко Андрій Васильович', '+380933334455', NULL),
('Мельник Ірина Сергіївна',    '+380634445566', 'melnyk@example.com');

INSERT INTO car (client_id, vin, license_plate, brand, model, production_year, mileage_km) VALUES
(1, 'TMBJG7NE0J0412345', 'BI1234AA', 'Skoda',      'Octavia', 2018,  87500),
(1, 'UU1HSDDGH55123456', 'BI5678KX', 'Renault',    'Duster',  2015, 142300),
(2, 'JTNB11HK103123456', 'AA2345BH', 'Toyota',     'Camry',   2020,  56800),
(3, 'WVWZZZ1KZCW123456', 'BI9012CT', 'Volkswagen', 'Golf',    2012, 198000);

-- Замовлення в різних станах життєвого циклу. Для замовлення у стані CREATED
-- робіт ще немає, тому F1, F2, F3 та індекс дорівнюють 0.
INSERT INTO service_order (car_id, description, state, created_date, f1, f2, f3, priority_index) VALUES
(1, 'Вібрація керма та скрип під час гальмування',        'COMPLETED', '2026-10-05', 90, 40, 8.75, 52.19),
(2, 'Стукіт у передній підвісці на нерівностях',          'REPAIR',    '2026-10-14', 70, 60, 8.75, 51.19),
(3, 'Планове технічне обслуговування, заміна оливи',      'DIAGNOSED', '2026-10-20', 10, 10, 2.5,   8.13),
(4, 'Двигун не запускається вранці',                      'CREATED',   '2026-10-24',  0,  0, 0,     0);

INSERT INTO work (order_id, name, safety_score, mobility_score, labor_hours, cost) VALUES
(1, 'Заміна передніх гальмівних колодок',             90, 40, 1.5, 1200.00),
(1, 'Заміна передніх гальмівних дисків',              90, 40, 2.0, 3600.00),
(2, 'Заміна опорного підшипника передньої стійки',    70, 60, 2.5, 2800.00),
(2, 'Заміна втулок стабілізатора',                    40, 20, 1.0,  650.00),
(3, 'Заміна моторної оливи',                          10, 10, 0.5,  450.00),
(3, 'Заміна оливного та повітряного фільтрів',         5, 10, 0.5,  700.00);
