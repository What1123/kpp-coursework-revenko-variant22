package ua.edu.nupp.kpp.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Спільна основа для Servlet-ів серверної частини.
 * <p>
 * Клас лише обслуговує HTTP: читає параметри запиту, перетворює результат Service у JSON
 * і формує відповідь про помилку. SQL-запитів і предметної логіки тут немає — вони
 * виконуються на рівнях Service та DAO.
 * <p>
 * Правила відповіді:
 * <ul>
 *   <li>успіх — HTTP 200 і JSON (зазвичай актуальний список сутностей);</li>
 *   <li>помилка даних або запиту (валідація, недопустимий перехід стану, неправильний
 *       параметр) — HTTP 400 і JSON {"error": "повідомлення"};</li>
 *   <li>помилка бази даних або непередбачена помилка — HTTP 500 і JSON {"error": "..."}.</li>
 * </ul>
 * У будь-якому випадку виняток перехоплюється, тому помилковий запит не зупиняє Tomcat.
 */
public abstract class BaseJsonServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(BaseJsonServlet.class.getName());

    /** ObjectMapper потокобезпечний після налаштування, тому спільний для всіх Servlet-ів. */
    protected static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)   // LocalDate -> "2026-10-26"
            .build();

    /** Операція, що повертає дані для JSON-відповіді. */
    @FunctionalInterface
    private interface Action {
        Object execute() throws Exception;
    }

    /** GET: повернути дані (список сутностей). */
    protected abstract Object handleGet(HttpServletRequest req) throws Exception;

    /** POST: виконати операцію (параметр action) і повернути актуальні дані. */
    protected abstract Object handlePost(HttpServletRequest req) throws Exception;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        respond(resp, () -> handleGet(req));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Кодування потрібно задати до першого читання параметрів форми
        req.setCharacterEncoding("UTF-8");
        respond(resp, () -> handlePost(req));
    }

    // ------------------------------------------------------------------------
    // Формування відповіді
    // ------------------------------------------------------------------------

    private void respond(HttpServletResponse resp, Action action) throws IOException {
        int status = HttpServletResponse.SC_OK;
        String json;
        try {
            // Спочатку серіалізуємо повністю: якщо щось піде не так, клієнт не отримає «половину» JSON
            json = MAPPER.writeValueAsString(action.execute());
        } catch (IllegalArgumentException e) {
            // Помилка вхідних даних: валідація Service, неправильний параметр, заборонений перехід стану
            LOG.warning("Некоректний запит: " + e.getMessage());
            status = HttpServletResponse.SC_BAD_REQUEST;
            json = errorJson(e.getMessage());
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Помилка роботи з базою даних", e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            json = errorJson("Помилка доступу до бази даних. Перевірте, чи запущено MySQL");
        } catch (Exception | LinkageError e) {
            // LinkageError охоплює, зокрема, відсутній JDBC-драйвер (ExceptionInInitializerError у DBUtil)
            LOG.log(Level.SEVERE, "Непередбачена помилка сервера", e);
            status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            json = errorJson("Внутрішня помилка сервера");
        }
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(json);
    }

    private static String errorJson(String message) {
        try {
            return MAPPER.writeValueAsString(Map.of("error", message == null ? "Помилка" : message));
        } catch (IOException e) {
            return "{\"error\":\"Помилка\"}";
        }
    }

    // ------------------------------------------------------------------------
    // Читання параметрів запиту
    // ------------------------------------------------------------------------

    /** Назва операції з параметра action (add, update, delete ...). */
    protected static String action(HttpServletRequest req) {
        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException("Не вказано операцію (параметр action)");
        }
        return action.trim();
    }

    protected static IllegalArgumentException unknownAction(String action) {
        return new IllegalArgumentException("Невідома операція: " + action);
    }

    /** Текстовий параметр без пробілів по краях; null, якщо параметра немає або він порожній. */
    protected static String text(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    protected static long requiredLong(HttpServletRequest req, String name) {
        String value = requiredValue(req, name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути цілим числом");
        }
    }

    protected static int requiredInt(HttpServletRequest req, String name) {
        String value = requiredValue(req, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути цілим числом");
        }
    }

    protected static double requiredDouble(HttpServletRequest req, String name) {
        String value = requiredValue(req, name).replace(',', '.');
        try {
            double number = Double.parseDouble(value);
            if (Double.isNaN(number) || Double.isInfinite(number)) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути числом");
        }
    }

    protected static BigDecimal requiredDecimal(HttpServletRequest req, String name) {
        String value = requiredValue(req, name).replace(',', '.');
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметр '" + name + "' має бути числом");
        }
    }

    protected static OrderState requiredState(HttpServletRequest req, String name) {
        String value = requiredValue(req, name);
        try {
            return OrderState.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Невідомий стан замовлення: " + value);
        }
    }

    private static String requiredValue(HttpServletRequest req, String name) {
        String value = text(req, name);
        if (value == null) {
            throw new IllegalArgumentException("Параметр '" + name + "' є обов'язковим");
        }
        return value;
    }
}
