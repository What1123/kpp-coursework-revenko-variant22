package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці car (модель Car).
 * Додатково до базового CRUD містить вузькі операції пошуку за VIN і державним номером:
 * обидва значення унікальні в базі даних, Service використовує їх для зрозумілої перевірки.
 */
public class CarDAO {

    private static final String SELECT =
            "SELECT id, client_id, vin, license_plate, brand, model, production_year, mileage_km FROM car";

    public void add(Car car) throws SQLException {
        String sql = "INSERT INTO car(client_id, vin, license_plate, brand, model, production_year, mileage_km) "
                + "VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, car);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    car.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Car> getAll() throws SQLException {
        String sql = SELECT + " ORDER BY id";
        List<Car> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Car getById(long id) throws SQLException {
        String sql = SELECT + " WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Повертає усі автомобілі вказаного клієнта. */
    public List<Car> getByClientId(long clientId) throws SQLException {
        String sql = SELECT + " WHERE client_id=? ORDER BY id";
        List<Car> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    /** Повертає автомобіль із указаним VIN або null. */
    public Car getByVin(String vin) throws SQLException {
        String sql = SELECT + " WHERE vin=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, vin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Повертає автомобіль із указаним державним номером або null. */
    public Car getByLicensePlate(String licensePlate) throws SQLException {
        String sql = SELECT + " WHERE license_plate=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, licensePlate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Car car) throws SQLException {
        String sql = "UPDATE car SET client_id=?, vin=?, license_plate=?, brand=?, model=?, "
                + "production_year=?, mileage_km=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, car);
            ps.setLong(8, car.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM car WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Java-об'єкт → параметри PreparedStatement (поля без id). */
    private static void bind(PreparedStatement ps, Car car) throws SQLException {
        ps.setLong(1, car.getClientId());
        ps.setString(2, car.getVin());
        ps.setString(3, car.getLicensePlate());
        ps.setString(4, car.getBrand());
        ps.setString(5, car.getModel());
        ps.setInt(6, car.getProductionYear());
        ps.setInt(7, car.getMileageKm());
    }

    /** Рядок ResultSet → Java-об'єкт. */
    private static Car map(ResultSet rs) throws SQLException {
        return new Car(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("vin"),
                rs.getString("license_plate"),
                rs.getString("brand"),
                rs.getString("model"),
                rs.getInt("production_year"),
                rs.getInt("mileage_km"));
    }
}
