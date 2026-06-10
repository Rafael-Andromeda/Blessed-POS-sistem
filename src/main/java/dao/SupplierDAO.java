package dao;

import database.DatabaseConnection;
import model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class untuk entitas Supplier.
 * Mengimplementasikan BaseDAO sebagai penerapan abstraction & polymorphism.
 */
public class SupplierDAO implements BaseDAO<Supplier, Integer> {

    @Override
    public List<Supplier> findAll() {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM suppliers ORDER BY nama_supplier";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Supplier(rs.getInt("id_supplier"), rs.getString("nama_supplier"), rs.getString("kontak"), rs.getString("alamat"), rs.getString("status")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public boolean insert(Supplier s) {
        String sql = "INSERT INTO suppliers(nama_supplier,kontak,alamat,status,updated_at) VALUES(?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getNamaSupplier()); ps.setString(2, s.getKontak()); ps.setString(3, s.getAlamat()); ps.setString(4, s.getStatus()); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean update(Supplier s) {
        String sql = "UPDATE suppliers SET nama_supplier=?, kontak=?, alamat=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_supplier=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getNamaSupplier()); ps.setString(2, s.getKontak()); ps.setString(3, s.getAlamat()); ps.setString(4, s.getStatus()); ps.setInt(5, s.getIdSupplier()); return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(Integer id) {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM suppliers WHERE id_supplier=?")) { ps.setInt(1, id); return ps.executeUpdate() > 0; }
        catch (SQLException e) { e.printStackTrace(); return false; }
    }
}
