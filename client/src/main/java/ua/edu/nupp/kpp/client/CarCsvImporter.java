package ua.edu.nupp.kpp.client;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Початок CSV-імпорту (етап 3): читання файла з автомобілями, розбір рядків і передавання
 * коректних записів серверу через HttpCarServiceClient.
 * <p>
 * Формат файла sample-data/cars.csv (UTF-8, роздільник «;», перший рядок — заголовок):
 * <pre>
 * client_id;vin;license_plate;brand;model;production_year;mileage_km
 * 2;WF0DXXGCBDGK12345;BI3456EE;Ford;Focus;2016;121400
 * </pre>
 * Клієнт перевіряє тільки форму запису: кількість полів, обов'язковість і числові типи.
 * Предметні правила (формат VIN, межі року, існування клієнта, унікальність VIN і номера)
 * перевіряє Service на сервері; рядки, які він відхилив, враховуються у підсумку.
 * <p>
 * Фоновий запуск через JavaFX Task, ProgressBar і остаточна обробка помилок — етап 4.
 */
public class CarCsvImporter {

    private static final Logger LOG = Logger.getLogger(CarCsvImporter.class.getName());

    /** Очікуваний заголовок: назви та порядок стовпців. */
    public static final List<String> HEADER = List.of(
            "client_id", "vin", "license_plate", "brand", "model", "production_year", "mileage_km");

    /** Підсумок імпорту. */
    public static class Result {
        private int imported;
        private int skipped;
        private int rejected;
        private final List<String> messages = new ArrayList<>();

        /** Кількість записів, які сервер прийняв. */
        public int getImported() {
            return imported;
        }

        /** Кількість рядків, пропущених на клієнті через неправильну форму запису. */
        public int getSkipped() {
            return skipped;
        }

        /** Кількість рядків, відхилених сервером (помилка валідації, дублікат тощо). */
        public int getRejected() {
            return rejected;
        }

        /** Пояснення для кожного пропущеного або відхиленого рядка. */
        public List<String> getMessages() {
            return messages;
        }

        public String summary() {
            return "Імпортовано: " + imported + "; пропущено (неправильний формат): " + skipped
                    + "; відхилено сервером: " + rejected;
        }
    }

    private final HttpCarServiceClient api;

    public CarCsvImporter(HttpCarServiceClient api) {
        this.api = api;
    }

    /**
     * Читає CSV-файл і передає коректні рядки серверу. Помилка в окремому рядку не зупиняє
     * імпорт; недоступність сервера (ApiException зі статусом 0) зупиняє його.
     *
     * @throws IllegalArgumentException файл не знайдено, не в UTF-8 або заголовок неправильний
     */
    public Result importFile(Path file) {
        List<String> lines = readLines(file);
        checkHeader(lines.get(0));

        Result result = new Result();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            int lineNo = i + 1;
            Car car;
            try {
                car = parseLine(line);
            } catch (IllegalArgumentException e) {
                result.skipped++;
                note(result, "Рядок " + lineNo + " пропущено: " + e.getMessage());
                continue;
            }
            try {
                api.addCar(car);
                result.imported++;
            } catch (ApiException e) {
                if (e.getStatus() == 0 || e.getStatus() >= 500) {
                    throw e;                       // сервер недоступний або зламаний — імпорт зупиняється
                }
                result.rejected++;
                note(result, "Рядок " + lineNo + " відхилено сервером: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * Розбирає один рядок даних на об'єкт Car.
     *
     * @throws IllegalArgumentException якщо кількість полів не дорівнює 7, обов'язкове поле
     *                                  порожнє або число має неправильний формат
     */
    public static Car parseLine(String line) {
        String[] p = line.split(";", -1);
        if (p.length != HEADER.size()) {
            throw new IllegalArgumentException(
                    "очікується " + HEADER.size() + " полів, а знайдено " + p.length);
        }
        for (int i = 0; i < p.length; i++) {
            p[i] = p[i].trim();
            if (p[i].isEmpty()) {
                throw new IllegalArgumentException("поле '" + HEADER.get(i) + "' порожнє");
            }
        }
        return new Car(null,
                longNumber(p[0], HEADER.get(0)),
                p[1], p[2], p[3], p[4],
                intNumber(p[5], HEADER.get(5)),
                intNumber(p[6], HEADER.get(6)));
    }

    private static long longNumber(String value, String column) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле '" + column + "' має бути цілим числом");
        }
    }

    private static int intNumber(String value, String column) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("поле '" + column + "' має бути цілим числом");
        }
    }

    private static List<String> readLines(Path file) {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("Файл " + file + " не знайдено. "
                    + "Запускайте клієнт із каталогу client, де є каталог sample-data");
        }
        List<String> lines;
        try {
            lines = new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (MalformedInputException e) {
            throw new IllegalArgumentException("Файл " + file + " має бути збережений у кодуванні UTF-8");
        } catch (IOException e) {
            throw new IllegalArgumentException("Не вдалося прочитати файл " + file + ": " + e.getMessage());
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Файл " + file + " порожній");
        }
        // Excel додає на початок UTF-8-файла невидимий символ BOM — прибираємо його
        if (lines.get(0).startsWith("﻿")) {
            lines.set(0, lines.get(0).substring(1));
        }
        return lines;
    }

    private static void checkHeader(String header) {
        List<String> found = List.of(header.trim().toLowerCase(Locale.ROOT).split(";", -1));
        if (!found.equals(HEADER)) {
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: "
                    + String.join(";", HEADER));
        }
    }

    private static void note(Result result, String message) {
        LOG.warning(message);
        result.messages.add(message);
    }
}
