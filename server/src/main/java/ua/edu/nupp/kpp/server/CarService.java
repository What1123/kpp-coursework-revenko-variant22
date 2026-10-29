package ua.edu.nupp.kpp.server;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Service-рівень для автомобілів: валідація даних, перевірка існування клієнта-власника,
 * унікальності VIN і державного номера, логування, виклик CarDAO.
 */
public class CarService {

    private static final Logger LOG = Logger.getLogger(CarService.class.getName());

    /** VIN: 17 символів, великі латинські літери (без I, O, Q) і цифри. */
    private static final Pattern VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private final CarDAO dao = new CarDAO();
    private final ClientDAO clientDAO = new ClientDAO();
    private final ServiceOrderDAO orderDAO = new ServiceOrderDAO();

    public List<Car> getAll() throws SQLException {
        return dao.getAll();
    }

    public Car getById(long id) throws SQLException {
        return dao.getById(id);
    }

    public void add(Car car) throws SQLException {
        validate(car);
        requireClient(car.getClientId());
        requireUnique(car);
        dao.add(car);
        LOG.info("Додано автомобіль (id=" + car.getId() + ")");
    }

    public void update(Car car) throws SQLException {
        validate(car);
        requireExisting(car.getId());
        requireClient(car.getClientId());
        requireUnique(car);
        dao.update(car);
        LOG.info("Оновлено автомобіль (id=" + car.getId() + ")");
    }

    public void delete(long id) throws SQLException {
        requireExisting(id);
        if (!orderDAO.getByCarId(id).isEmpty()) {
            throw invalid("Неможливо видалити автомобіль: для нього є сервісні замовлення");
        }
        dao.delete(id);
        LOG.info("Видалено автомобіль (id=" + id + ")");
    }

    /** Перевірка обов'язкових полів і допустимих числових значень автомобіля. */
    static void validate(Car c) {
        if (c == null) {
            throw invalid("Автомобіль не вказано");
        }
        if (c.getClientId() <= 0) {
            throw invalid("Некоректний clientId автомобіля");
        }
        if (c.getVin() == null || !VIN.matcher(c.getVin()).matches()) {
            throw invalid("VIN має складатися з 17 символів: великих латинських літер (без I, O, Q) і цифр");
        }
        if (blank(c.getLicensePlate()) || c.getLicensePlate().length() > 15) {
            throw invalid("Державний номер є обов'язковим (до 15 символів)");
        }
        if (blank(c.getBrand()) || c.getBrand().length() > 60) {
            throw invalid("Марка автомобіля є обов'язковою (до 60 символів)");
        }
        if (blank(c.getModel()) || c.getModel().length() > 60) {
            throw invalid("Модель автомобіля є обов'язковою (до 60 символів)");
        }
        int maxYear = Year.now().getValue() + 1;
        if (c.getProductionYear() < 1950 || c.getProductionYear() > maxYear) {
            throw invalid("Рік випуску має бути в межах 1950.." + maxYear);
        }
        if (c.getMileageKm() < 0) {
            throw invalid("Пробіг не може бути від'ємним");
        }
    }

    private void requireClient(long clientId) throws SQLException {
        if (clientDAO.getById(clientId) == null) {
            throw invalid("Клієнта не знайдено");
        }
    }

    private void requireExisting(Long id) throws SQLException {
        if (id == null || id <= 0) {
            throw invalid("Некоректний ідентифікатор автомобіля");
        }
        if (dao.getById(id) == null) {
            throw invalid("Автомобіль не знайдено");
        }
    }

    /** VIN і державний номер не повинні належати іншому автомобілю. */
    private void requireUnique(Car car) throws SQLException {
        Car byVin = dao.getByVin(car.getVin());
        if (byVin != null && !sameCar(byVin, car)) {
            throw invalid("Автомобіль із таким VIN уже існує");
        }
        Car byPlate = dao.getByLicensePlate(car.getLicensePlate());
        if (byPlate != null && !sameCar(byPlate, car)) {
            throw invalid("Автомобіль із таким державним номером уже існує");
        }
    }

    private static boolean sameCar(Car found, Car edited) {
        return edited.getId() != null && edited.getId().equals(found.getId());
    }

    private static boolean blank(String v) {
        return v == null || v.isBlank();
    }

    private static IllegalArgumentException invalid(String message) {
        LOG.warning(message);
        return new IllegalArgumentException(message);
    }
}
