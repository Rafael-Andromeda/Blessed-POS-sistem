package dao;

import database.DatabaseConnection;
import model.Ingredient;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class untuk entitas Ingredient (Bahan Baku).
 * Mengimplementasikan BaseDAO sebagai penerapan abstraction & polymorphism.
 */
public class IngredientDAO implements BaseDAO<Ingredient, Integer> {

    @Override
    public List<Ingredient> findAll() {
        List<Ingredient> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM ingredients ORDER BY nama_bahan")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public boolean insert(Ingredient i) {
        String sql = "INSERT INTO ingredients(nama_bahan,stok,satuan,batas_minimum,status,updated_at) VALUES(?,?,?,?,?,CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, i, false);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) new MenuDAO().refreshAllMenuStocks();
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean update(Ingredient i) {
        String sql = "UPDATE ingredients SET nama_bahan=?, stok=?, satuan=?, batas_minimum=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_bahan=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, i, true);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) new MenuDAO().refreshAllMenuStocks();
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean delete(Integer id) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM ingredients WHERE id_bahan=?")) {
            ps.setInt(1, id);
            boolean ok = ps.executeUpdate() > 0;
            if (ok) new MenuDAO().refreshAllMenuStocks();
            return ok;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean hasLowStock() { return getLowStockCount() > 0; }

    public int getLowStockCount() {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM ingredients WHERE stok <= batas_minimum")) {
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public String getLowStockSummary() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT nama_bahan, stok, satuan, batas_minimum FROM ingredients WHERE stok <= batas_minimum ORDER BY stok ASC LIMIT 5";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(rs.getString("nama_bahan")).append(" ").append(rs.getDouble("stok")).append(" ").append(rs.getString("satuan"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return sb.length() == 0 ? "Semua aman" : sb.toString();
    }

    private void fill(PreparedStatement ps, Ingredient i, boolean includeId) throws SQLException {
        String status = statusFor(i.getStok(), i.getBatasMinimum());
        ps.setString(1, i.getNamaBahan());
        ps.setDouble(2, i.getStok());
        ps.setString(3, i.getSatuan());
        ps.setDouble(4, i.getBatasMinimum());
        ps.setString(5, status);
        if (includeId) ps.setInt(6, i.getIdBahan());
    }

    public static String statusFor(double stok, double min) {
        if (stok <= 0) return "Out of Stock";
        if (stok <= min) return "Low Stock";
        return "In Stock";
    }

    private Ingredient map(ResultSet rs) throws SQLException {
        return new Ingredient(rs.getInt("id_bahan"), rs.getString("nama_bahan"), rs.getDouble("stok"),
                rs.getString("satuan"), rs.getDouble("batas_minimum"), rs.getString("status"),
                rs.getString("created_at"), rs.getString("updated_at"));
    }
}
