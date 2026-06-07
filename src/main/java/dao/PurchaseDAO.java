package dao;

import database.DatabaseConnection;
import model.Purchase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {
    public List<Purchase> findAll() {
        List<Purchase> list = new ArrayList<>();
        String sql = "SELECT p.*, COALESCE(s.nama_supplier,'-') nama_supplier, i.nama_bahan, i.satuan FROM purchases p " +
                "LEFT JOIN suppliers s ON p.id_supplier=s.id_supplier JOIN ingredients i ON p.id_bahan=i.id_bahan ORDER BY p.tanggal DESC";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
    public boolean save(Purchase p) throws SQLException {
        String ins = "INSERT INTO purchases(id_supplier,id_bahan,jumlah,harga_satuan,total,tanggal,catatan) VALUES(?,?,?,?,?,datetime('now','localtime'),?)";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int total = (int)Math.round(p.getJumlah() * p.getHargaSatuan());
                try (PreparedStatement ps = c.prepareStatement(ins)) {
                    if (p.getIdSupplier() <= 0) ps.setNull(1, Types.INTEGER); else ps.setInt(1, p.getIdSupplier());
                    ps.setInt(2, p.getIdBahan()); ps.setDouble(3, p.getJumlah()); ps.setInt(4, p.getHargaSatuan()); ps.setInt(5, total); ps.setString(6, p.getCatatan()); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE ingredients SET stok=stok+?, status=CASE WHEN stok+?<=0 THEN 'Out of Stock' WHEN stok+?<=batas_minimum THEN 'Low Stock' ELSE 'In Stock' END, updated_at=CURRENT_TIMESTAMP WHERE id_bahan=?")) {
                    ps.setDouble(1, p.getJumlah()); ps.setDouble(2, p.getJumlah()); ps.setDouble(3, p.getJumlah()); ps.setInt(4, p.getIdBahan()); ps.executeUpdate();
                }
                MenuDAO.refreshMenuStockFromIngredients(c, null);
                c.commit(); return true;
            } catch (SQLException e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }
    private Purchase map(ResultSet rs) throws SQLException {
        return new Purchase(rs.getInt("id_pembelian"), rs.getInt("id_supplier"), rs.getString("nama_supplier"), rs.getInt("id_bahan"), rs.getString("nama_bahan"), rs.getDouble("jumlah"), rs.getString("satuan"), rs.getInt("harga_satuan"), rs.getInt("total"), rs.getString("tanggal"), rs.getString("catatan"));
    }
}
