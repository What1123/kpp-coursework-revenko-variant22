package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці work (модель Work).
 * Додатково до базового CRUD містить getByOrderId(): список робіт одного замовлення
 * потрібен Service-рівню для розрахунку факторів F1-F3.
 */
public class WorkDAO {

    private static final String SELECT =
            "SELECT id, order_id, name, safety_score, mobility_score, labor_hours, cost FROM work";

    public void add(Work work) throws SQLException {
        String sql = "INSERT INTO work(order_id, name, safety_score, mobility_score, labor_hours, cost) "
                + "VALUES(?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, work);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    work.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Work> getAll() throws SQLException {
        String sql = SELECT + " ORDER BY id";
        List<Work> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Work getById(long id) throws SQLException {
        String sql = SELECT + " WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Повертає усі роботи вказаного замовлення. */
    public List<Work> getByOrderId(long orderId) throws SQLException {
        String sql = SELECT + " WHERE order_id=? ORDER BY id";
        List<Work> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public void update(Work work) throws SQLException {
        String sql = "UPDATE work SET order_id=?, name=?, safety_score=?, mobility_score=?, "
                + "labor_hours=?, cost=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, work);
            ps.setLong(7, work.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM work WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Java-об'єкт → параметри PreparedStatement (поля без id). */
    private static void bind(PreparedStatement ps, Work work) throws SQLException {
        ps.setLong(1, work.getOrderId());
        ps.setString(2, work.getName());
        ps.setDouble(3, work.getSafetyScore());
        ps.setDouble(4, work.getMobilityScore());
        ps.setDouble(5, work.getLaborHours());
        ps.setBigDecimal(6, work.getCost());
    }

    /** Рядок ResultSet → Java-об'єкт. */
    private static Work map(ResultSet rs) throws SQLException {
        return new Work(
                rs.getLong("id"),
                rs.getLong("order_id"),
                rs.getString("name"),
                rs.getDouble("safety_score"),
                rs.getDouble("mobility_score"),
                rs.getDouble("labor_hours"),
                rs.getBigDecimal("cost"));
    }
}
