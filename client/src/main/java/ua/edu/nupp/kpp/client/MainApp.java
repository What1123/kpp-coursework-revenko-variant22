package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Головне вікно JavaFX-клієнта «Сервісне обслуговування автомобілів».
 * <p>
 * Вікно містить чотири вкладки (клієнти, автомобілі, сервісні замовлення, роботи). Кожна
 * вкладка — це TableView зі списком і форма введення з кнопками «Додати», «Змінити»,
 * «Видалити», «Оновити». Усі дані клієнт отримує та змінює тільки через HttpCarServiceClient:
 * <p>
 * JavaFX → HttpCarServiceClient → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL.
 * <p>
 * У класі немає SQL, підключення до MySQL, правил життєвого циклу та формул F1–F3: стан
 * замовлення, показники й перевірки визначає сервер, а вікно лише відображає результат.
 */
public class MainApp extends Application {

    private static final Logger LOG = Logger.getLogger(MainApp.class.getName());

    private final HttpCarServiceClient api = new HttpCarServiceClient();

    private final TableView<Client> clients = new TableView<>();
    private final TableView<Car> cars = new TableView<>();
    private final TableView<ServiceOrder> orders = new TableView<>();
    private final TableView<Work> works = new TableView<>();

    @Override
    public void start(Stage stage) {
        TabPane tabs = new TabPane(
                new Tab("Клієнти", clientPane()),
                new Tab("Автомобілі", carPane()),
                new Tab("Сервісні замовлення", orderPane()),
                new Tab("Роботи", workPane())
        );
        tabs.getTabs().forEach(t -> t.setClosable(false));
        stage.setScene(new Scene(tabs, 1200, 700));
        stage.setTitle("Сервісне обслуговування автомобілів");
        stage.show();
        refreshAll();
    }

    // ------------------------------------------------------------------------
    // Вкладка «Клієнти»
    // ------------------------------------------------------------------------

    private Pane clientPane() {
        clients.getColumns().setAll(
                col("ID", "id", 60), col("ПІБ", "fullName", 260),
                col("Телефон", "phone", 150), col("E-mail", "email", 200));

        TextField fullName = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> safe(() -> {
            api.addClient(new Client(null, fullName.getText(), phone.getText(), email.getText()));
            refreshClients();
        }));
        update.setOnAction(e -> safe(() -> {
            Client c = selected(clients);
            api.updateClient(new Client(c.getId(), fullName.getText(), phone.getText(), email.getText()));
            refreshClients();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteClient(selected(clients).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshClients());
        clients.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) {
                fullName.setText(c.getFullName());
                phone.setText(c.getPhone());
                email.setText(c.getEmail());
            }
        });

        return layout(clients, form(
                new Label("ПІБ клієнта"), fullName,
                new Label("Телефон"), phone,
                new Label("E-mail (необов'язково)"), email,
                new HBox(8, add, update, delete, refresh)));
    }

    // ------------------------------------------------------------------------
    // Вкладка «Автомобілі» (+ початок CSV-імпорту)
    // ------------------------------------------------------------------------

    private Pane carPane() {
        cars.getColumns().setAll(
                col("ID", "id", 50), col("Клієнт", "clientId", 70), col("VIN", "vin", 170),
                col("Держ. номер", "licensePlate", 100), col("Марка", "brand", 100),
                col("Модель", "model", 100), col("Рік", "productionYear", 60),
                col("Пробіг, км", "mileageKm", 90));

        TextField clientId = new TextField();
        TextField vin = new TextField();
        TextField plate = new TextField();
        TextField brand = new TextField();
        TextField model = new TextField();
        TextField year = new TextField();
        TextField mileage = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");

        TextField csvFile = new TextField(Path.of("sample-data", "cars.csv").toString());
        Button importCsv = new Button("Імпортувати CSV");
        Label csvStatus = new Label("CSV-імпорт ще не запускався");
        csvStatus.setWrapText(true);

        add.setOnAction(e -> safe(() -> {
            api.addCar(readCar(null, clientId, vin, plate, brand, model, year, mileage));
            refreshCars();
        }));
        update.setOnAction(e -> safe(() -> {
            Car c = selected(cars);
            api.updateCar(readCar(c.getId(), clientId, vin, plate, brand, model, year, mileage));
            refreshCars();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteCar(selected(cars).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshCars());
        cars.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c != null) {
                clientId.setText(String.valueOf(c.getClientId()));
                vin.setText(c.getVin());
                plate.setText(c.getLicensePlate());
                brand.setText(c.getBrand());
                model.setText(c.getModel());
                year.setText(String.valueOf(c.getProductionYear()));
                mileage.setText(String.valueOf(c.getMileageKm()));
            }
        });

        // Початок CSV-імпорту: файл читається, розбирається, коректні рядки йдуть на сервер.
        // Фонове виконання (Task) і ProgressBar — наступний етап.
        importCsv.setOnAction(e -> safe(() -> {
            CarCsvImporter.Result result = new CarCsvImporter(api).importFile(Path.of(csvFile.getText().trim()));
            csvStatus.setText(result.summary());
            refreshCars();
            if (!result.getMessages().isEmpty()) {
                showInfo(String.join("\n", limit(result.getMessages(), 12)));
            }
        }));

        return layout(cars, form(
                new Label("ID клієнта-власника"), clientId,
                new Label("VIN (17 символів)"), vin,
                new Label("Державний номер"), plate,
                new Label("Марка"), brand,
                new Label("Модель"), model,
                new Label("Рік випуску"), year,
                new Label("Пробіг, км"), mileage,
                new HBox(8, add, update, delete, refresh),
                new Label("Файл CSV (відносний шлях)"), csvFile, importCsv, csvStatus));
    }

    private Car readCar(Long id, TextField clientId, TextField vin, TextField plate, TextField brand,
                        TextField model, TextField year, TextField mileage) {
        return new Car(id, longValue(clientId, "ID клієнта-власника"), vin.getText(), plate.getText(),
                brand.getText(), model.getText(), intValue(year, "Рік випуску"), intValue(mileage, "Пробіг, км"));
    }

    // ------------------------------------------------------------------------
    // Вкладка «Сервісні замовлення» (стан, F1–F3 та індекс пріоритету)
    // ------------------------------------------------------------------------

    private Pane orderPane() {
        orders.getColumns().setAll(
                col("ID", "id", 50), col("Авто", "carId", 55), col("Опис", "description", 230),
                col("Стан", "state", 120), col("Створено", "createdDate", 90),
                numberCol("F1", "f1", 60), numberCol("F2", "f2", 60), numberCol("F3", "f3", 60),
                numberCol("Індекс I", "priorityIndex", 80));

        TextField carId = new TextField();
        TextField description = new TextField();
        ComboBox<OrderState> state = new ComboBox<>(FXCollections.observableArrayList(OrderState.values()));
        Button add = new Button("Створити");
        Button update = new Button("Змінити");
        Button change = new Button("Змінити стан");
        Button recalc = new Button("Перерахувати");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        Label hint = new Label("Допустимі переходи: СТВОРЕНО → ДІАГНОСТОВАНО → РЕМОНТ → ВИКОНАНО. "
                + "Перевірку виконує сервер. F1–F3 та індекс I розраховуються автоматично.");
        hint.setWrapText(true);

        add.setOnAction(e -> safe(() -> {
            api.addOrder(longValue(carId, "ID автомобіля"), description.getText());
            refreshOrders();
        }));
        update.setOnAction(e -> safe(() -> {
            api.updateOrder(selected(orders).getId(), longValue(carId, "ID автомобіля"), description.getText());
            refreshOrders();
        }));
        change.setOnAction(e -> safe(() -> {
            ServiceOrder order = selected(orders);
            if (state.getValue() == null) {
                throw new IllegalArgumentException("Виберіть новий стан замовлення");
            }
            api.changeOrderState(order.getId(), state.getValue());
            refreshOrders();
        }));
        recalc.setOnAction(e -> safe(() -> {
            api.recalculateOrder(selected(orders).getId());
            refreshOrders();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteOrder(selected(orders).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshOrders());
        orders.getSelectionModel().selectedItemProperty().addListener((o, a, v) -> {
            if (v != null) {
                carId.setText(String.valueOf(v.getCarId()));
                description.setText(v.getDescription());
            }
        });

        return layout(orders, form(
                new Label("ID автомобіля"), carId,
                new Label("Опис проблеми"), description,
                new HBox(8, add, update, delete),
                new Label("Новий стан замовлення"), state,
                new HBox(8, change, recalc, refresh),
                hint));
    }

    // ------------------------------------------------------------------------
    // Вкладка «Роботи»
    // ------------------------------------------------------------------------

    private Pane workPane() {
        works.getColumns().setAll(
                col("ID", "id", 50), col("Замовл.", "orderId", 70), col("Назва роботи", "name", 250),
                numberCol("Безпека", "safetyScore", 75), numberCol("Мобільність", "mobilityScore", 95),
                numberCol("Нормо-год", "laborHours", 85), col("Вартість, грн", "cost", 100));

        TextField orderId = new TextField();
        TextField name = new TextField();
        TextField safety = new TextField();
        TextField mobility = new TextField();
        TextField hours = new TextField();
        TextField cost = new TextField();
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button refresh = new Button("Оновити");
        Button byOrder = new Button("Роботи замовлення");

        add.setOnAction(e -> safe(() -> {
            api.addWork(readWork(null, orderId, name, safety, mobility, hours, cost));
            refreshAll();       // після зміни робіт сервер перераховує F1–F3 та індекс замовлення
        }));
        update.setOnAction(e -> safe(() -> {
            Work w = selected(works);
            api.updateWork(readWork(w.getId(), orderId, name, safety, mobility, hours, cost));
            refreshAll();
        }));
        delete.setOnAction(e -> safe(() -> {
            api.deleteWork(selected(works).getId());
            refreshAll();
        }));
        refresh.setOnAction(e -> refreshWorks());
        byOrder.setOnAction(e -> safe(() -> works.setItems(FXCollections.observableArrayList(
                api.getWorksByOrder(longValue(orderId, "ID замовлення"))))));
        works.getSelectionModel().selectedItemProperty().addListener((o, a, w) -> {
            if (w != null) {
                orderId.setText(String.valueOf(w.getOrderId()));
                name.setText(w.getName());
                safety.setText(String.valueOf(w.getSafetyScore()));
                mobility.setText(String.valueOf(w.getMobilityScore()));
                hours.setText(String.valueOf(w.getLaborHours()));
                cost.setText(w.getCost() == null ? "" : w.getCost().toPlainString());
            }
        });

        return layout(works, form(
                new Label("ID замовлення"), orderId,
                new Label("Назва роботи"), name,
                new Label("Вплив на безпеку (0–100)"), safety,
                new Label("Вплив на мобільність (0–100)"), mobility,
                new Label("Трудомісткість, нормо-год"), hours,
                new Label("Вартість, грн"), cost,
                new HBox(8, add, update, delete, refresh),
                byOrder));
    }

    private Work readWork(Long id, TextField orderId, TextField name, TextField safety,
                          TextField mobility, TextField hours, TextField cost) {
        return new Work(id, longValue(orderId, "ID замовлення"), name.getText(),
                doubleValue(safety, "Вплив на безпеку"), doubleValue(mobility, "Вплив на мобільність"),
                doubleValue(hours, "Трудомісткість"), decimalValue(cost, "Вартість"));
    }

    // ------------------------------------------------------------------------
    // Оновлення таблиць: після кожної операції дані знову запитуються в сервера
    // ------------------------------------------------------------------------

    private void refreshAll() {
        safe(() -> {
            clients.setItems(FXCollections.observableArrayList(api.getClients()));
            cars.setItems(FXCollections.observableArrayList(api.getCars()));
            orders.setItems(FXCollections.observableArrayList(api.getOrders()));
            works.setItems(FXCollections.observableArrayList(api.getWorks()));
        });
    }

    private void refreshClients() {
        safe(() -> clients.setItems(FXCollections.observableArrayList(api.getClients())));
    }

    private void refreshCars() {
        safe(() -> cars.setItems(FXCollections.observableArrayList(api.getCars())));
    }

    private void refreshOrders() {
        safe(() -> orders.setItems(FXCollections.observableArrayList(api.getOrders())));
    }

    private void refreshWorks() {
        safe(() -> works.setItems(FXCollections.observableArrayList(api.getWorks())));
    }

    // ------------------------------------------------------------------------
    // Допоміжні методи інтерфейсу
    // ------------------------------------------------------------------------

    private <T> T selected(TableView<T> table) {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть запис у таблиці");
        }
        return value;
    }

    /** Виконує дію й показує будь-яку помилку у вікні, а не в консолі. */
    private void safe(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            LOG.warning(String.valueOf(e.getMessage()));
            showError(e);
        }
    }

    private void showError(Throwable e) {
        String text = e == null || e.getMessage() == null ? "Помилка" : e.getMessage();
        new Alert(Alert.AlertType.ERROR, text, ButtonType.OK).showAndWait();
    }

    private void showInfo(String text) {
        new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK).showAndWait();
    }

    private static List<String> limit(List<String> messages, int max) {
        if (messages.size() <= max) {
            return messages;
        }
        List<String> shortList = new ArrayList<>(messages.subList(0, max));
        shortList.add("... та ще " + (messages.size() - max));
        return shortList;
    }

    private static VBox form(Node... children) {
        VBox box = new VBox(6, children);
        box.setPadding(new Insets(0, 0, 0, 10));
        return box;
    }

    private Pane layout(TableView<?> table, Pane form) {
        SplitPane split = new SplitPane(table, form);
        split.setDividerPositions(0.66);
        BorderPane root = new BorderPane(split);
        root.setPadding(new Insets(10));
        return root;
    }

    private <S, T> TableColumn<S, T> col(String title, String property, double width) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        return column;
    }

    /** Числовий стовпець: значення відображається з двома знаками після коми (F1–F3, індекс I). */
    private <S> TableColumn<S, Number> numberCol(String title, String property, double width) {
        TableColumn<S, Number> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setPrefWidth(width);
        column.setCellFactory(c -> new TableCell<S, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format(Locale.US, "%.2f", item.doubleValue()));
            }
        });
        return column;
    }

    // Перетворення тексту полів у числа. Це лише розбір введення; правила й межі значень
    // перевіряє Service на сервері.

    private static long longValue(TextField field, String label) {
        try {
            return Long.parseLong(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    private static int intValue(TextField field, String label) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути цілим числом");
        }
    }

    private static double doubleValue(TextField field, String label) {
        try {
            return Double.parseDouble(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути числом");
        }
    }

    private static BigDecimal decimalValue(TextField field, String label) {
        try {
            return new BigDecimal(field.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + label + "» має бути числом");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
