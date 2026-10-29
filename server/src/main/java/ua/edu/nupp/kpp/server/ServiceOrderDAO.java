package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці service_order (модель ServiceOrder).
 * Стан замовлення зберігається як текст: state.name() під час запису
 * і OrderState.valueOf(...) під час читання.
 * Вузькі операції: updateState() змінює лише стан, updateFactors() - лише розраховані
 * значення F1, F2, F3 та індексу пріоритету. Допустимість переходів стану DAO не перевіряє.
 */
public class ServiceOrderDAO {

    private static final String SELECT =
            "SELECT id, car_id, description, state, created_date, f1, f2, f3, priority_index FROM service_order";

    public void add(ServiceOrder order) throws SQLException {
        String sql = "INSERT INTO service_order(car_id, description, state, created_date, f1, f2, f3, priority_index) "
                + "VALUES(?,?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, order);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    order.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<ServiceOrder> getAll() throws SQLException {
        String sql = SELECT + " ORDER BY id";
        List<ServiceOrder> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public ServiceOrder getById(long id) throws SQLException {
        String sql = SELECT + " WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Повертає усі замовлення вказаного автомобіля. */
    public List<ServiceOrder> getByCarId(long carId) throws SQLException {
        String sql = SELECT + " WHERE car_id=? ORDER BY id";
        List<ServiceOrder> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void update(ServiceOrder order) throws SQLException {
        String sql = "UPDATE service_order SET car_id=?, description=?, state=?, created_date=?, "
                + "f1=?, f2=?, f3=?, priority_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, order);
            ps.setLong(9, order.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM service_order WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Змінює лише стан замовлення. */
    public void updateState(long id, OrderState state) throws SQLException {
        String sql = "UPDATE service_order SET state=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, state.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /** Змінює лише розраховані значення F1, F2, F3 та індексу пріоритету. */
    public void updateFactors(long id, double f1, double f2, double f3, double priorityIndex)
            throws SQLException {
        String sql = "UPDATE service_order SET f1=?, f2=?, f3=?, priority_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, f1);
            ps.setDouble(2, f2);
            ps.setDouble(3, f3);
            ps.setDouble(4, priorityIndex);
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    /** Java-об'єкт → параметри PreparedStatement (поля без id). */
    private static void bind(PreparedStatement ps, ServiceOrder order) throws SQLException {
        ps.setLong(1, order.getCarId());
        ps.setString(2, order.getDescription());
        ps.setString(3, order.getState().name());
        ps.setDate(4, Date.valueOf(order.getCreatedDate()));
        ps.setDouble(5, order.getF1());
        ps.setDouble(6, order.getF2());
        ps.setDouble(7, order.getF3());
        ps.setDouble(8, order.getPriorityIndex());
    }

    /** Рядок ResultSet → Java-об'єкт. */
    private static ServiceOrder map(ResultSet rs) throws SQLException {
        return new ServiceOrder(
                rs.getLong("id"),
                rs.getLong("car_id"),
                rs.getString("description"),
                OrderState.valueOf(rs.getString("state")),
                rs.getDate("created_date").toLocalDate(),
                rs.getDouble("f1"),
                rs.getDouble("f2"),
                rs.getDouble("f3"),
                rs.getDouble("priority_index"));
    }
}
