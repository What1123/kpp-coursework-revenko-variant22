package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для таблиці client (модель Client).
 * Усі SQL-запити виконуються через PreparedStatement, з'єднання береться з DBUtil.
 * Предметної логіки (валідації, правил) тут немає - вона належить Service-рівню.
 */
public class ClientDAO {

    public void add(Client client) throws SQLException {
        String sql = "INSERT INTO client(full_name, phone, email) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, client);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getLong(1));
                }
            }
        }
    }

    public List<Client> getAll() throws SQLException {
        String sql = "SELECT id, full_name, phone, email FROM client ORDER BY id";
        List<Client> list = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Client getById(long id) throws SQLException {
        String sql = "SELECT id, full_name, phone, email FROM client WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE client SET full_name=?, phone=?, email=? WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, client);
            ps.setLong(4, client.getId());
            ps.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM client WHERE id=?";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /** Java-об'єкт → параметри PreparedStatement (поля без id). */
    private static void bind(PreparedStatement ps, Client client) throws SQLException {
        ps.setString(1, client.getFullName());
        ps.setString(2, client.getPhone());
        ps.setString(3, client.getEmail());
    }

    /** Рядок ResultSet → Java-об'єкт. */
    private static Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"));
    }
}
