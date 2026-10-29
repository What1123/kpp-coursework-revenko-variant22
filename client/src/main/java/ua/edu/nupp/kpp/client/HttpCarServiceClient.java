package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-клієнт JavaFX-застосунку. Це єдине місце клієнта, яке знає про URL сервера та формат
 * HTTP-обміну: JavaFX викликає звичайні методи (getCars(), addWork(...), changeOrderState(...)),
 * а клас формує запит, надсилає його Servlet-у і перетворює JSON-відповідь на клієнтські об'єкти.
 * <p>
 * Параметри передаються як application/x-www-form-urlencoded (як форма), відповідь —
 * JSON. SQL і прямого підключення до MySQL у клієнті немає.
 */
public class HttpCarServiceClient {

    /** Адреса застосунку в Tomcat: http://localhost:8080/ + назва WAR-файла (finalName). */
    public static final String DEFAULT_BASE_URL = "http://localhost:8080/car-service-server";

    private final String baseUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public HttpCarServiceClient() {
        this(DEFAULT_BASE_URL);
    }

    public HttpCarServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // ------------------------------------------------------------------------
    // Клієнти  (/clients)
    // ------------------------------------------------------------------------

    public List<Client> getClients() {
        return Arrays.asList(read(get("/clients"), Client[].class));
    }

    public void addClient(Client c) {
        post("/clients", form("action", "add", "fullName", c.getFullName(),
                "phone", c.getPhone(), "email", c.getEmail()));
    }

    public void updateClient(Client c) {
        post("/clients", form("action", "update", "id", c.getId(), "fullName", c.getFullName(),
                "phone", c.getPhone(), "email", c.getEmail()));
    }

    public void deleteClient(long id) {
        post("/clients", form("action", "delete", "id", id));
    }

    // ------------------------------------------------------------------------
    // Автомобілі  (/cars)
    // ------------------------------------------------------------------------

    public List<Car> getCars() {
        return Arrays.asList(read(get("/cars"), Car[].class));
    }

    public void addCar(Car c) {
        post("/cars", carForm("add", c));
    }

    public void updateCar(Car c) {
        post("/cars", carForm("update", c));
    }

    public void deleteCar(long id) {
        post("/cars", form("action", "delete", "id", id));
    }

    private static Map<String, String> carForm(String action, Car c) {
        return form("action", action, "id", c.getId(), "clientId", c.getClientId(),
                "vin", c.getVin(), "licensePlate", c.getLicensePlate(),
                "brand", c.getBrand(), "model", c.getModel(),
                "productionYear", c.getProductionYear(), "mileageKm", c.getMileageKm());
    }

    // ------------------------------------------------------------------------
    // Сервісні замовлення  (/orders)
    // ------------------------------------------------------------------------

    public List<ServiceOrder> getOrders() {
        return Arrays.asList(read(get("/orders"), ServiceOrder[].class));
    }

    public void addOrder(long carId, String description) {
        post("/orders", form("action", "add", "carId", carId, "description", description));
    }

    public void updateOrder(long id, long carId, String description) {
        post("/orders", form("action", "update", "id", id, "carId", carId,
                "description", description));
    }

    /** Клієнт лише передає бажаний стан; допустимість переходу перевіряє Service на сервері. */
    public void changeOrderState(long id, OrderState state) {
        post("/orders", form("action", "transition", "id", id, "state", state.name()));
    }

    public void recalculateOrder(long id) {
        post("/orders", form("action", "recalculate", "id", id));
    }

    public void deleteOrder(long id) {
        post("/orders", form("action", "delete", "id", id));
    }

    // ------------------------------------------------------------------------
    // Роботи  (/works)
    // ------------------------------------------------------------------------

    public List<Work> getWorks() {
        return Arrays.asList(read(get("/works"), Work[].class));
    }

    public List<Work> getWorksByOrder(long orderId) {
        return Arrays.asList(read(get("/works?orderId=" + orderId), Work[].class));
    }

    public void addWork(Work w) {
        post("/works", workForm("add", w));
    }

    public void updateWork(Work w) {
        post("/works", workForm("update", w));
    }

    public void deleteWork(long id) {
        post("/works", form("action", "delete", "id", id));
    }

    private static Map<String, String> workForm(String action, Work w) {
        return form("action", action, "id", w.getId(), "orderId", w.getOrderId(),
                "name", w.getName(), "safetyScore", w.getSafetyScore(),
                "mobilityScore", w.getMobilityScore(), "laborHours", w.getLaborHours(),
                "cost", w.getCost() == null ? null : w.getCost().toPlainString());
    }

    // ------------------------------------------------------------------------
    // Низькорівневі HTTP-методи
    // ------------------------------------------------------------------------

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(Duration.ofSeconds(15));
    }

    private String get(String path) {
        return send(request(path).GET().build());
    }

    private String post(String path, Map<String, String> params) {
        return send(request(path)
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(encode(params), StandardCharsets.UTF_8))
                .build());
    }

    /**
     * Надсилає запит. Відповідь із кодом 400 і вище перетворюється на ApiException з текстом
     * поля "error" із JSON, тому JavaFX може показати це повідомлення користувачу.
     */
    private String send(HttpRequest request) {
        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Запит до сервера перервано");
        } catch (IOException e) {
            throw new ApiException(0, "Сервер недоступний (" + baseUrl + "). "
                    + "Перевірте, що Tomcat запущено і WAR-файл розгорнуто");
        }
        if (response.statusCode() >= 400) {
            throw new ApiException(response.statusCode(), errorMessage(response));
        }
        return response.body();
    }

    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode error = mapper.readTree(response.body()).get("error");
            if (error != null && !error.asText().isBlank()) {
                return error.asText();
            }
        } catch (IOException | RuntimeException ignored) {
            // тіло відповіді не є очікуваним JSON — показуємо загальне повідомлення нижче
        }
        if (response.statusCode() == 404) {
            return "Ресурс не знайдено (HTTP 404). Перевірте адресу " + baseUrl
                    + " і те, що WAR-файл розгорнуто в Tomcat";
        }
        return "Помилка HTTP " + response.statusCode();
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new ApiException(0, "Помилка JSON: сервер повернув неочікувану відповідь");
        }
    }

    /** Збирає параметри форми з пар «назва, значення»; значення null не надсилаються. */
    private static Map<String, String> form(Object... keyValues) {
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object value = keyValues[i + 1];
            if (value != null) {
                params.put(String.valueOf(keyValues[i]), String.valueOf(value));
            }
        }
        return params;
    }

    private static String encode(Map<String, String> params) {
        StringBuilder body = new StringBuilder();
        for (Map.Entry<String, String> p : params.entrySet()) {
            if (body.length() > 0) {
                body.append('&');
            }
            body.append(enc(p.getKey())).append('=').append(enc(p.getValue()));
        }
        return body.toString();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
