package dao;

import database.DatabaseConnection;
import model.Promo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PromoDAO {
    public List<Promo> findAll(boolean activeOnly) {
        List<Promo> list = new ArrayList<>();
        String sql = activeOnly ? "SELECT * FROM promos WHERE status='Aktif' ORDER BY nama_promo" : "SELECT * FROM promos ORDER BY nama_promo";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean insert(Promo p) {
        String sql = "INSERT INTO promos(nama_promo,jenis_promo,nilai_promo,minimal_pembelian,tanggal_mulai,tanggal_selesai,status,updated_at) VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, p, false);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Promo p) {
        String sql = "UPDATE promos SET nama_promo=?, jenis_promo=?, nilai_promo=?, minimal_pembelian=?, tanggal_mulai=?, tanggal_selesai=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_promo=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, p, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM promos WHERE id_promo=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void fill(PreparedStatement ps, Promo p, boolean includeId) throws SQLException {
        ps.setString(1, p.getNamaPromo());
        ps.setString(2, p.getJenisPromo());
        ps.setInt(3, p.getNilaiPromo());
        ps.setInt(4, p.getMinimalPembelian());
        ps.setString(5, p.getTanggalMulai());
        ps.setString(6, p.getTanggalSelesai());
        ps.setString(7, p.getStatus());
        if (includeId) ps.setInt(8, p.getIdPromo());
    }

    private Promo map(ResultSet rs) throws SQLException {
        return new Promo(rs.getInt("id_promo"), rs.getString("nama_promo"), rs.getString("jenis_promo"),
                rs.getInt("nilai_promo"), rs.getInt("minimal_pembelian"), rs.getString("tanggal_mulai"),
                rs.getString("tanggal_selesai"), rs.getString("status"), rs.getString("created_at"), rs.getString("updated_at"));
    }
}
