package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service-рівень для клієнтів: валідація даних, логування, виклик ClientDAO.
 * SQL у Service не використовується.
 */
public class ClientService {

    private static final Logger LOG = Logger.getLogger(ClientService.class.getName());

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ()\\-]{7,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final ClientDAO dao = new ClientDAO();
    private final CarDAO carDAO = new CarDAO();

    public List<Client> getAll() throws SQLException {
        return dao.getAll();
    }

    public Client getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Client client) throws SQLException {
        validate(client);
        dao.add(client);
        LOG.info("Додано клієнта (id=" + client.getId() + ")");
    }

    public void update(Client client) throws SQLException {
        validate(client);
        requireExisting(client.getId());
        dao.update(client);
        LOG.info("Оновлено клієнта (id=" + client.getId() + ")");
    }

    public void delete(long id) throws SQLException {
        requireExisting(id);
        if (!carDAO.getByClientId(id).isEmpty()) {
            throw invalid("Неможливо видалити клієнта: у нього є автомобілі");
        }
        dao.delete(id);
        LOG.info("Видалено клієнта (id=" + id + ")");
    }

    /** Перевірка обов'язкових полів і допустимих значень клієнта. */
    static void validate(Client c) {
        if (c == null) {
            throw invalid("Клієнта не вказано");
        }
        if (blank(c.getFullName())) {
            throw invalid("ПІБ клієнта є обов'язковим");
        }
        if (c.getFullName().length() > 160) {
            throw invalid("ПІБ клієнта не може бути довшим за 160 символів");
        }
        if (blank(c.getPhone()) || !PHONE.matcher(c.getPhone()).matches()) {
            throw invalid("Некоректний номер телефону клієнта");
        }
        if (!blank(c.getEmail())
                && (c.getEmail().length() > 120 || !EMAIL.matcher(c.getEmail()).matches())) {
            throw invalid("Некоректна електронна адреса клієнта");
        }
    }

    private void requireExisting(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw invalid("Некоректний ідентифікатор клієнта");
        }
        if (dao.getById(id) == null) {
            throw invalid("Клієнта не знайдено");
        }
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }

    private static IllegalArgumentException invalid(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
