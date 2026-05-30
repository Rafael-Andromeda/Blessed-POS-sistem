package dao;

import database.DatabaseConnection;
import model.MenuIngredient;
import model.MenuItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    public List<MenuItem> findAll(String keyword, String kategori, boolean activeOnly) {
        List<MenuItem> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.*, " +
                "COALESCE((" +
                "   SELECT CAST(MIN(i.stok / mbb.jumlah_dibutuhkan) AS INTEGER) " +
                "   FROM menu_bahan_baku mbb " +
                "   JOIN ingredients i ON i.id_bahan = mbb.bahan_baku_id " +
                "   WHERE mbb.menu_id = m.id_menu AND mbb.jumlah_dibutuhkan > 0" +
                "), m.stok) AS calculated_stok " +
                "FROM menu_items m WHERE COALESCE(m.is_deleted,0)=0");
        if (activeOnly) sql.append(" AND m.status='Aktif'");
        if (keyword != null && !keyword.isBlank()) sql.append(" AND m.nama_menu LIKE ?");
        if (kategori != null && !kategori.equals("Semua") && !kategori.isBlank()) sql.append(" AND m.kategori=?");
        sql.append(" ORDER BY m.nama_menu ASC");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i=1;
            if (keyword != null && !keyword.isBlank()) ps.setString(i++, "%"+keyword+"%");
            if (kategori != null && !kategori.equals("Semua") && !kategori.isBlank()) ps.setString(i, kategori);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public MenuItem findById(int idMenu) {
        String sql = "SELECT m.*, COALESCE((" +
                "SELECT CAST(MIN(i.stok / mbb.jumlah_dibutuhkan) AS INTEGER) " +
                "FROM menu_bahan_baku mbb JOIN ingredients i ON i.id_bahan=mbb.bahan_baku_id " +
                "WHERE mbb.menu_id=m.id_menu AND mbb.jumlah_dibutuhkan>0), m.stok) AS calculated_stok " +
                "FROM menu_items m WHERE m.id_menu=? AND COALESCE(m.is_deleted,0)=0";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idMenu);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int getCalculatedMenuStock(int menuId) {
        String sql = "SELECT COALESCE((" +
                "SELECT CAST(MIN(i.stok / mbb.jumlah_dibutuhkan) AS INTEGER) " +
                "FROM menu_bahan_baku mbb JOIN ingredients i ON i.id_bahan=mbb.bahan_baku_id " +
                "WHERE mbb.menu_id=? AND mbb.jumlah_dibutuhkan>0), " +
                "(SELECT stok FROM menu_items WHERE id_menu=?))";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, menuId);
            ps.setInt(2, menuId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Math.max(0, rs.getInt(1)) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public boolean insert(MenuItem item) {
        String sql = "INSERT INTO menu_items(nama_menu,kategori,harga,stok,gambar,status,is_deleted,updated_at) VALUES(?,?,?,?,?,?,0,CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, item, false);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean insertWithIngredients(MenuItem item, List<MenuIngredient> ingredients) {
        String sql = "INSERT INTO menu_items(nama_menu,kategori,harga,stok,gambar,status,is_deleted,updated_at) VALUES(?,?,?,?,?,?,0,CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    fill(ps, item, false);
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) item.setIdMenu(keys.getInt(1));
                }
                replaceIngredients(conn, item.getIdMenu(), ingredients);
                refreshMenuStockFromIngredients(conn, item.getIdMenu());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(MenuItem item) {
        String sql = "UPDATE menu_items SET nama_menu=?, kategori=?, harga=?, stok=?, gambar=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_menu=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            fill(ps, item, true);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateWithIngredients(MenuItem item, List<MenuIngredient> ingredients) {
        String sql = "UPDATE menu_items SET nama_menu=?, kategori=?, harga=?, stok=?, gambar=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_menu=? AND COALESCE(is_deleted,0)=0";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    fill(ps, item, true);
                    ps.executeUpdate();
                }
                replaceIngredients(conn, item.getIdMenu(), ingredients);
                refreshMenuStockFromIngredients(conn, item.getIdMenu());
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        String sql = "UPDATE menu_items SET is_deleted=1, status='Nonaktif', updated_at=CURRENT_TIMESTAMP WHERE id_menu=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<MenuIngredient> findIngredientsByMenuId(int menuId) {
        List<MenuIngredient> list = new ArrayList<>();
        String sql = "SELECT mbb.id, mbb.menu_id, mbb.bahan_baku_id, i.nama_bahan, i.satuan, mbb.jumlah_dibutuhkan " +
                "FROM menu_bahan_baku mbb JOIN ingredients i ON i.id_bahan=mbb.bahan_baku_id " +
                "WHERE mbb.menu_id=? ORDER BY i.nama_bahan";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, menuId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new MenuIngredient(
                    rs.getInt("id"), rs.getInt("menu_id"), rs.getInt("bahan_baku_id"),
                    rs.getString("nama_bahan"), rs.getString("satuan"), rs.getDouble("jumlah_dibutuhkan")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void refreshAllMenuStocks() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            refreshMenuStockFromIngredients(conn, null);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void refreshMenuStockFromIngredients(Connection conn, Integer menuId) throws SQLException {
        String sql = "UPDATE menu_items SET stok = COALESCE((" +
                "SELECT CAST(MIN(i.stok / mbb.jumlah_dibutuhkan) AS INTEGER) " +
                "FROM menu_bahan_baku mbb JOIN ingredients i ON i.id_bahan = mbb.bahan_baku_id " +
                "WHERE mbb.menu_id = menu_items.id_menu AND mbb.jumlah_dibutuhkan > 0" +
                "), stok), updated_at=CURRENT_TIMESTAMP " +
                "WHERE id_menu IN (SELECT DISTINCT menu_id FROM menu_bahan_baku)";
        if (menuId != null) sql += " AND id_menu=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (menuId != null) ps.setInt(1, menuId);
            ps.executeUpdate();
        }
    }

    private void replaceIngredients(Connection conn, int menuId, List<MenuIngredient> ingredients) throws SQLException {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM menu_bahan_baku WHERE menu_id=?")) {
            delete.setInt(1, menuId);
            delete.executeUpdate();
        }
        if (ingredients == null || ingredients.isEmpty()) return;
        String sql = "INSERT INTO menu_bahan_baku(menu_id,bahan_baku_id,jumlah_dibutuhkan) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (MenuIngredient ingredient : ingredients) {
                if (ingredient.getBahanBakuId() <= 0 || ingredient.getJumlahDibutuhkan() <= 0) continue;
                ps.setInt(1, menuId);
                ps.setInt(2, ingredient.getBahanBakuId());
                ps.setDouble(3, ingredient.getJumlahDibutuhkan());
                ps.addBatch();
            }
            ps.executeBatch();
        }
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
        int stock = hasColumn(rs, "calculated_stok") ? rs.getInt("calculated_stok") : rs.getInt("stok");
        int deleted = hasColumn(rs, "is_deleted") ? rs.getInt("is_deleted") : 0;
        return new MenuItem(rs.getInt("id_menu"), rs.getString("nama_menu"), rs.getString("kategori"),
                rs.getInt("harga"), Math.max(stock, 0), rs.getString("gambar"), rs.getString("status"), deleted,
                rs.getString("created_at"), rs.getString("updated_at"));
    }

    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
