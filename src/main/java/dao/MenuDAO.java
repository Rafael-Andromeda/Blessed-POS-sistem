package dao;

import database.DatabaseConnection;
import model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    public List<MenuItem> findAll(String keyword, String kategori, boolean activeOnly) {
        List<MenuItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM menu_items WHERE 1=1");
        if (activeOnly) sql.append(" AND status='Aktif'");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND nama_menu LIKE ?");
        if (kategori != null && !kategori.equals("Semua") && !kategori.isBlank()) sql.append(" AND kategori=?");
        sql.append(" ORDER BY nama_menu ASC");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i=1;
            if (keyword != null && !keyword.isBlank()) ps.setString(i++, "%"+keyword+"%");
            if (kategori != null && !kategori.equals("Semua") && !kategori.isBlank()) ps.setString(i, kategori);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(MenuItem item) {
        String sql = "INSERT INTO menu_items(nama_menu,kategori,harga,stok,gambar,status,updated_at) VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, item, false);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(MenuItem item) {
        String sql = "UPDATE menu_items SET nama_menu=?, kategori=?, harga=?, stok=?, gambar=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_menu=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, item, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        String sql = "UPDATE menu_items SET status='Nonaktif', updated_at=CURRENT_TIMESTAMP WHERE id_menu=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void fill(PreparedStatement ps, MenuItem item, boolean includeId) throws SQLException {
        ps.setString(1, item.getNamaMenu());
        ps.setString(2, item.getKategori());
        ps.setInt(3, item.getHarga());
        ps.setInt(4, item.getStok());
        ps.setString(5, item.getGambar());
        ps.setString(6, item.getStatus());
        if (includeId) ps.setInt(7, item.getIdMenu());
    }

    private MenuItem map(ResultSet rs) throws SQLException {
        return new MenuItem(rs.getInt("id_menu"), rs.getString("nama_menu"), rs.getString("kategori"),
                rs.getInt("harga"), rs.getInt("stok"), rs.getString("gambar"), rs.getString("status"),
                rs.getString("created_at"), rs.getString("updated_at"));
    }
}
