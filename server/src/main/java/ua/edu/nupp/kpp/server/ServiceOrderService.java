package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service-рівень сервісних замовлень - об'єкта чотиристанного життєвого циклу.
 * <p>
 * Відповідає за: валідацію, перевірку існування автомобіля, автоматичний розрахунок
 * факторів F1-F3 та індексу пріоритету (PriorityCalculator), початковий стан СТВОРЕНО,
 * перевірку допустимості переходів стану (isAllowedTransition, changeState) і логування.
 */
public class ServiceOrderService {

    private static final Logger LOG = Logger.getLogger(ServiceOrderService.class.getName());

    private final ServiceOrderDAO dao = new ServiceOrderDAO();
    private final CarDAO carDAO = new CarDAO();
    private final WorkDAO workDAO = new WorkDAO();

    public List<ServiceOrder> getAll() throws SQLException {
        return dao.getAll();
    }

    public ServiceOrder getById(long id) throws SQLException {
        return dao.getById(id);
    }

    /**
     * Створює нове замовлення для автомобіля. Початковий стан (СТВОРЕНО), дата створення
     * і значення факторів задаються сервером; користувач їх не вводить.
     */
    public ServiceOrder add(long carId, String description) throws SQLException {
        validate(carId, description);
        requireCar(carId);
        ServiceOrder order = fromWorks(null, carId, description, OrderState.CREATED,
                LocalDate.now(), List.of());
        dao.add(order);
        LOG.info("Створено сервісне замовлення (id=" + order.getId() + ")");
        return order;
    }

    /** Редагує дані замовлення; стан і дата створення зберігаються, фактори перераховуються. */
    public void update(long id, long carId, String description) throws SQLException {
        validate(carId, description);
        ServiceOrder old = dao.getById(id);
        if (old == null) {
            throw invalid("Замовлення не знайдено");
        }
        requireCar(carId);
        dao.update(fromWorks(id, carId, description, old.getState(),
                old.getCreatedDate(), workDAO.getByOrderId(id)));
        LOG.info("Оновлено сервісне замовлення (id=" + id + ")");
    }

    public void delete(long id) throws SQLException {
        if (dao.getById(id) == null) {
            throw invalid("Замовлення не знайдено");
        }
        if (!workDAO.getByOrderId(id).isEmpty()) {
            throw invalid("Неможливо видалити замовлення: у ньому є роботи");
        }
        dao.delete(id);
        LOG.info("Видалено сервісне замовлення (id=" + id + ")");
    }

    /**
     * Перераховує F1, F2, F3 та індекс пріоритету за поточними роботами замовлення.
     * Викликається після додавання, редагування або видалення роботи.
     */
    public void recalculate(long orderId) throws SQLException {
        if (dao.getById(orderId) == null) {
            throw invalid("Замовлення не знайдено");
        }
        List<Work> works = workDAO.getByOrderId(orderId);
        double f1 = PriorityCalculator.factor1(works);
        double f2 = PriorityCalculator.factor2(works);
        double f3 = PriorityCalculator.factor3(works);
        double index = PriorityCalculator.calculate(f1, f2, f3);
        dao.updateFactors(orderId, f1, f2, f3, index);
        LOG.info("Перераховано показник пріоритету замовлення (id=" + orderId + "): I = " + index);
    }

    /**
     * Чи дозволений перехід між станами життєвого циклу.
     * Допустимі лише послідовні переходи вперед:
     * СТВОРЕНО → ДІАГНОСТОВАНО → РЕМОНТ → ВИКОНАНО.
     */
    public static boolean isAllowedTransition(OrderState from, OrderState to) {
        return (from == OrderState.CREATED && to == OrderState.DIAGNOSED)
                || (from == OrderState.DIAGNOSED && to == OrderState.REPAIR)
                || (from == OrderState.REPAIR && to == OrderState.COMPLETED);
    }

    /** Змінює стан замовлення лише після перевірки допустимості переходу. */
    public void changeState(long id, OrderState next) throws SQLException {
        ServiceOrder current = dao.getById(id);
        if (current == null) {
            throw invalid("Замовлення не знайдено");
        }
        if (next == null || !isAllowedTransition(current.getState(), next)) {
            LOG.warning("Недопустимий перехід стану: " + current.getState() + " -> " + next);
            throw new IllegalArgumentException("Недопустимий перехід стану");
        }
        dao.updateState(id, next);
        LOG.info("Змінено стан замовлення (id=" + id + "): " + current.getState() + " -> " + next);
    }

    /** Перевірка власних полів замовлення. */
    static void validate(long carId, String description) {
        if (carId <= 0) {
            throw invalid("Некоректний carId замовлення");
        }
        if (description == null || description.isBlank()) {
            throw invalid("Опис замовлення є обов'язковим");
        }
        if (description.length() > 500) {
            throw invalid("Опис замовлення не може бути довшим за 500 символів");
        }
    }

    /** Формує замовлення з автоматично розрахованими факторами та індексом пріоритету. */
    private static ServiceOrder fromWorks(Long id, long carId, String description,
                                          OrderState state, LocalDate createdDate,
                                          List<Work> works) {
        double f1 = PriorityCalculator.factor1(works);
        double f2 = PriorityCalculator.factor2(works);
        double f3 = PriorityCalculator.factor3(works);
        double index = PriorityCalculator.calculate(f1, f2, f3);
        return new ServiceOrder(id, carId, description, state, createdDate, f1, f2, f3, index);
    }

    private void requireCar(long carId) throws SQLException {
        if (carDAO.getById(carId) == null) {
            throw invalid("Автомобіль не знайдено");
        }
    }

    private static IllegalArgumentException invalid(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
