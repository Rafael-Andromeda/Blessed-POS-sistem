package dao;

import database.DatabaseConnection;
import model.User;
import utils.PasswordUtil;

import java.sql.*;

public class UserDAO {
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE LOWER(TRIM(username)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(COALESCE(status,'Aktif'))) = 'aktif' LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username == null ? "" : username.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && PasswordUtil.verifyPassword(password, rs.getString("password"))) {
                return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id_user"), rs.getString("nama"), rs.getString("username"),
                rs.getString("password"), rs.getString("role"), rs.getString("status"), rs.getString("created_at")
        );
    }
}
