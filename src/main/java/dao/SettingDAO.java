package dao;

import database.DatabaseConnection;
import model.AppSetting;

import java.sql.*;

public class SettingDAO {
    public AppSetting getSetting() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM app_settings ORDER BY id_setting LIMIT 1")) {
            if (rs.next()) return new AppSetting(rs.getInt("id_setting"), rs.getString("nama_aplikasi"), rs.getString("nama_warung"), rs.getString("alamat"), rs.getString("telepon"), rs.getString("logo_path"), rs.getString("updated_at"));
        } catch (SQLException e) { e.printStackTrace(); }
        return new AppSetting(1, "NasiGoreng 71", "Warung Makan", "", "", "", "");
    }

    public boolean update(AppSetting s) {
        String sql = "UPDATE app_settings SET nama_aplikasi=?, nama_warung=?, alamat=?, telepon=?, logo_path=?, updated_at=CURRENT_TIMESTAMP WHERE id_setting=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getNamaAplikasi());
            ps.setString(2, s.getNamaWarung());
            ps.setString(3, s.getAlamat());
            ps.setString(4, s.getTelepon());
            ps.setString(5, s.getLogoPath());
            ps.setInt(6, s.getIdSetting());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
