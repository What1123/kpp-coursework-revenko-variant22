package ua.edu.nupp.kpp.server;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service-рівень для робіт сервісного замовлення: валідація (текстові поля, оцінки 0..100,
 * трудомісткість, вартість), перевірка існування замовлення, логування та перерахунок
 * факторів F1-F3 і індексу пріоритету замовлення після кожної зміни складу робіт.
 */
public class WorkService {

    private static final Logger LOG = Logger.getLogger(WorkService.class.getName());

    private static final BigDecimal MAX_COST = new BigDecimal("99999999.99");

    private final WorkDAO dao = new WorkDAO();
    private final ServiceOrderDAO orderDAO = new ServiceOrderDAO();
    private final ServiceOrderService orderService = new ServiceOrderService();

    public List<Work> getAll() throws SQLException {
        return dao.getAll();
    }

    public Work getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public List<Work> getByOrderId(long orderId) throws SQLException {
        return dao.getByOrderId(orderId);
    }

    public void add(Work work) throws SQLException {
        validate(work);
        requireOrder(work.getOrderId());
        dao.add(work);
        LOG.info("Додано роботу (id=" + work.getId() + ")");
        orderService.recalculate(work.getOrderId());
    }

    public void update(Work work) throws SQLException {
        validate(work);
        Work old = requireExisting(work.getId());
        requireOrder(work.getOrderId());
        dao.update(work);
        LOG.info("Оновлено роботу (id=" + work.getId() + ")");
        orderService.recalculate(work.getOrderId());
        if (old.getOrderId() != work.getOrderId()) {
            orderService.recalculate(old.getOrderId());
        }
    }

    public void delete(long id) throws SQLException {
        Work old = requireExisting(id);
        dao.delete(id);
        LOG.info("Видалено роботу (id=" + id + ")");
        orderService.recalculate(old.getOrderId());
    }

    /** Перевірка обов'язкових текстових полів і допустимих числових значень роботи. */
    static void validate(Work w) {
        if (w == null) {
            throw invalid("Роботу не вказано");
        }
        if (w.getOrderId() <= 0) {
            throw invalid("Некоректний orderId роботи");
        }
        if (w.getName() == null || w.getName().isBlank() || w.getName().length() > 160) {
            throw invalid("Назва роботи є обов'язковою (до 160 символів)");
        }
        if (!inRange(w.getSafetyScore()) || !inRange(w.getMobilityScore())) {
            throw invalid("Оцінки впливу на безпеку й мобільність мають бути в діапазоні 0..100");
        }
        if (!(w.getLaborHours() > 0.0)) {
            throw invalid("Трудомісткість роботи має бути більшою за 0");
        }
        if (w.getCost() == null || w.getCost().signum() < 0 || w.getCost().compareTo(MAX_COST) > 0) {
            throw invalid("Вартість роботи не може бути від'ємною (допустимо до 99999999.99)");
        }
    }

    private void requireOrder(long orderId) throws SQLException {
        if (orderDAO.getById(orderId) == null) {
            throw invalid("Замовлення не знайдено");
        }
    }

    private Work requireExisting(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw invalid("Некоректний ідентифікатор роботи");
        }
        Work work = dao.getById(id);
        if (work == null) {
            throw invalid("Роботу не знайдено");
        }
        return work;
    }

    private static boolean inRange(double value) {
        return value >= 0.0 && value <= 100.0;
    }

    private static IllegalArgumentException invalid(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
