package dao;

import database.DatabaseConnection;
import model.CartItem;
import model.Transaction;
import model.TransactionDetail;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TransactionDAO {
    private static class IngredientRequirement {
        int id;
        String name;
        String unit;
        double available;
        double required;

        IngredientRequirement(int id, String name, String unit, double available) {
            this.id = id;
            this.name = name;
            this.unit = unit;
            this.available = available;
        }
    }

    private static class MenuRequirement {
        int id;
        String name;
        int available;
        int required;

        MenuRequirement(int id, String name, int available) {
            this.id = id;
            this.name = name;
            this.available = available;
        }
    }

    public Transaction save(Transaction trx, List<CartItem> items) throws SQLException {
        String insertTrx = "INSERT INTO transactions(kode_transaksi,id_user,id_promo,tipe_order,metode_pembayaran,subtotal,diskon,total,tanggal) VALUES(?,?,?,?,?,?,?,?,datetime('now','localtime'))";
        String insertDetail = "INSERT INTO transaction_details(id_transaksi,id_menu,nama_menu,harga,qty,total) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                validateIngredientStockBeforeTransaction(conn, items);
                try (PreparedStatement ps = conn.prepareStatement(insertTrx, Statement.RETURN_GENERATED_KEYS)) {
                    trx.setKodeTransaksi(generateCode(conn));
                    ps.setString(1, trx.getKodeTransaksi());
                    ps.setInt(2, trx.getIdUser());
                    if (trx.getIdPromo() == null || trx.getIdPromo() == 0) ps.setNull(3, Types.INTEGER); else ps.setInt(3, trx.getIdPromo());
                    ps.setString(4, trx.getTipeOrder());
                    ps.setString(5, trx.getMetodePembayaran());
                    ps.setInt(6, trx.getSubtotal());
                    ps.setInt(7, trx.getDiskon());
                    ps.setInt(8, trx.getTotal());
                    ps.executeUpdate();
                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) trx.setIdTransaksi(keys.getInt(1));
                }
                try (PreparedStatement ps = conn.prepareStatement(insertDetail)) {
                    for (CartItem item : items) {
                        ps.setInt(1, trx.getIdTransaksi());
                        ps.setInt(2, item.getIdMenu());
                        ps.setString(3, item.getNamaMenu());
                        ps.setInt(4, item.getHarga());
                        ps.setInt(5, item.getQty());
                        ps.setInt(6, item.getTotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                reduceStockAfterTransaction(conn, items);
                MenuDAO.refreshMenuStockFromIngredients(conn, null);
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        Transaction saved = findById(trx.getIdTransaksi());
        return saved != null ? saved : trx;
    }

    private void validateIngredientStockBeforeTransaction(Connection conn, List<CartItem> items) throws SQLException {
        Map<Integer, IngredientRequirement> ingredientRequirements = new LinkedHashMap<>();
        Map<Integer, MenuRequirement> menuRequirements = new LinkedHashMap<>();

        String recipeSql = "SELECT mbb.bahan_baku_id, mbb.jumlah_dibutuhkan, i.nama_bahan, i.stok, i.satuan " +
                "FROM menu_bahan_baku mbb JOIN ingredients i ON i.id_bahan=mbb.bahan_baku_id WHERE mbb.menu_id=?";
        String menuSql = "SELECT nama_menu, stok, status, COALESCE(is_deleted,0) AS is_deleted FROM menu_items WHERE id_menu=?";

        for (CartItem item : items) {
            boolean hasRecipe = false;
            try (PreparedStatement ps = conn.prepareStatement(recipeSql)) {
                ps.setInt(1, item.getIdMenu());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    hasRecipe = true;
                    int bahanId = rs.getInt("bahan_baku_id");
                    IngredientRequirement requirement = ingredientRequirements.get(bahanId);
                    if (requirement == null) {
                        requirement = new IngredientRequirement(bahanId, rs.getString("nama_bahan"), rs.getString("satuan"), rs.getDouble("stok"));
                        ingredientRequirements.put(bahanId, requirement);
                    }
                    requirement.required += rs.getDouble("jumlah_dibutuhkan") * item.getQty();
                }
            }
            if (!hasRecipe) {
                try (PreparedStatement ps = conn.prepareStatement(menuSql)) {
                    ps.setInt(1, item.getIdMenu());
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next() || rs.getInt("is_deleted") == 1 || !"Aktif".equals(rs.getString("status"))) {
                        throw new SQLException("Menu " + item.getNamaMenu() + " sudah tidak aktif atau sudah dihapus.");
                    }
                    MenuRequirement requirement = menuRequirements.get(item.getIdMenu());
                    if (requirement == null) {
                        requirement = new MenuRequirement(item.getIdMenu(), rs.getString("nama_menu"), rs.getInt("stok"));
                        menuRequirements.put(item.getIdMenu(), requirement);
                    }
                    requirement.required += item.getQty();
                }
            }
        }

        for (IngredientRequirement requirement : ingredientRequirements.values()) {
            if (requirement.required > requirement.available) {
                throw new SQLException("Stok bahan baku " + requirement.name +
                        " tidak mencukupi. Dibutuhkan " + requirement.required + " " + requirement.unit +
                        ", tersedia " + requirement.available + " " + requirement.unit + ".");
            }
        }
        for (MenuRequirement requirement : menuRequirements.values()) {
            if (requirement.required > requirement.available) {
                throw new SQLException("Stok menu " + requirement.name + " tidak mencukupi. Tersedia " +
                        requirement.available + ", diminta " + requirement.required + ".");
            }
        }
    }

    private void reduceStockAfterTransaction(Connection conn, List<CartItem> items) throws SQLException {
        String recipeSql = "SELECT mbb.bahan_baku_id, mbb.jumlah_dibutuhkan, i.stok, i.batas_minimum " +
                "FROM menu_bahan_baku mbb JOIN ingredients i ON i.id_bahan=mbb.bahan_baku_id WHERE mbb.menu_id=?";
        String updateIngredient = "UPDATE ingredients SET stok=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id_bahan=?";
        String updateMenuFallback = "UPDATE menu_items SET stok=stok-?, updated_at=CURRENT_TIMESTAMP WHERE id_menu=? AND stok>=?";
        for (CartItem item : items) {
            boolean hasRecipe = false;
            try (PreparedStatement ps = conn.prepareStatement(recipeSql)) {
                ps.setInt(1, item.getIdMenu());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    hasRecipe = true;
                    double newStock = rs.getDouble("stok") - (rs.getDouble("jumlah_dibutuhkan") * item.getQty());
                    try (PreparedStatement update = conn.prepareStatement(updateIngredient)) {
                        update.setDouble(1, newStock);
                        update.setString(2, IngredientDAO.statusFor(newStock, rs.getDouble("batas_minimum")));
                        update.setInt(3, rs.getInt("bahan_baku_id"));
                        update.executeUpdate();
                    }
                }
            }
            if (!hasRecipe) {
                try (PreparedStatement ps = conn.prepareStatement(updateMenuFallback)) {
                    ps.setInt(1, item.getQty());
                    ps.setInt(2, item.getIdMenu());
                    ps.setInt(3, item.getQty());
                    if (ps.executeUpdate() == 0) throw new SQLException("Stok menu " + item.getNamaMenu() + " tidak mencukupi.");
                }
            }
        }
    }

    private String generateCode(Connection conn) throws SQLException {
        String prefix = "TRX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM transactions WHERE kode_transaksi LIKE ?")) {
            ps.setString(1, prefix + "%");
            ResultSet rs = ps.executeQuery();
            int next = rs.next() ? rs.getInt(1) + 1 : 1;
            return prefix + String.format("%04d", next);
        }
    }

    public Transaction findById(int id) {
        String sql = "SELECT t.*, u.nama AS kasir, " +
                "(SELECT GROUP_CONCAT(nama_menu || ' x' || qty, ', ') FROM transaction_details WHERE id_transaksi=t.id_transaksi) AS items " +
                "FROM transactions t JOIN users u ON t.id_user=u.id_user WHERE t.id_transaksi=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Transaction> findAll(String keyword, String month) {
        return findAll(keyword, month, "Semua");
    }

    public List<Transaction> findAll(String keyword, String month, String year) {
        List<Transaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.*, u.nama AS kasir, " +
                "(SELECT GROUP_CONCAT(nama_menu || ' x' || qty, ', ') FROM transaction_details WHERE id_transaksi=t.id_transaksi) AS items " +
                "FROM transactions t JOIN users u ON t.id_user=u.id_user WHERE 1=1");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (t.kode_transaksi LIKE ? OR u.nama LIKE ? OR t.metode_pembayaran LIKE ? OR t.tanggal LIKE ? OR EXISTS " +
                    "(SELECT 1 FROM transaction_details td WHERE td.id_transaksi=t.id_transaksi AND td.nama_menu LIKE ?))");
        }
        appendDateFilter(sql, month, year, "t.tanggal");
        sql.append(" ORDER BY t.tanggal DESC");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i=1;
            if (keyword != null && !keyword.isBlank()) {
                for (int x=0;x<5;x++) ps.setString(i++, "%"+keyword+"%");
            }
            i = bindDateFilter(ps, i, month, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<TransactionDetail> findDetails(int idTransaksi) {
        List<TransactionDetail> list = new ArrayList<>();
        String sql = "SELECT td.*, COALESCE(m.kategori, '-') AS kategori " +
                "FROM transaction_details td LEFT JOIN menu_items m ON td.id_menu=m.id_menu WHERE td.id_transaksi=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new TransactionDetail(rs.getInt("id_detail"), rs.getInt("id_transaksi"), rs.getInt("id_menu"),
                    rs.getString("nama_menu"), rs.getString("kategori"), rs.getInt("harga"), rs.getInt("qty"), rs.getInt("total")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getTodayRevenue() { return singleInt("SELECT COALESCE(SUM(total),0) FROM transactions WHERE DATE(tanggal)=DATE('now','localtime')"); }
    public int getTodayCount() { return singleInt("SELECT COUNT(*) FROM transactions WHERE DATE(tanggal)=DATE('now','localtime')"); }
    public int getTodayAverage() { return singleInt("SELECT COALESCE(AVG(total),0) FROM transactions WHERE DATE(tanggal)=DATE('now','localtime')"); }
    public int getMonthlyRevenue() { return singleInt("SELECT COALESCE(SUM(total),0) FROM transactions WHERE strftime('%Y-%m',tanggal)=strftime('%Y-%m','now','localtime')"); }
    public int getTotalTransactions() { return singleInt("SELECT COUNT(*) FROM transactions"); }
    public int getAverageTransaction() { return singleInt("SELECT COALESCE(AVG(total),0) FROM transactions"); }

    public int getRevenue(String month, String year) { return filteredInt("SUM(total)", month, year); }
    public int getTotalTransactions(String month, String year) { return filteredInt("COUNT(*)", month, year); }
    public int getAverageTransaction(String month, String year) { return filteredInt("AVG(total)", month, year); }

    private int filteredInt(String aggregate, String month, String year) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(" + aggregate + ",0) FROM transactions WHERE 1=1");
        appendDateFilter(sql, month, year, "tanggal");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            bindDateFilter(ps, 1, month, year);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    private int singleInt(String sql) {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public Map<String, Integer> salesDaily() { return salesDaily("Semua", "Semua"); }
    public Map<String, Integer> salesWeekly() { return mapQuery("SELECT DATE(tanggal) AS label, SUM(total) AS value FROM transactions WHERE DATE(tanggal) >= DATE('now','localtime','-6 days') GROUP BY DATE(tanggal) ORDER BY label ASC"); }
    public Map<String, Integer> bestSellingMenu() { return bestSellingMenu("Semua", "Semua"); }
    public Map<String, Integer> salesByCategory() { return salesByCategory("Semua", "Semua"); }

    public Map<String, Integer> salesDaily(String month, String year) {
        StringBuilder sql = new StringBuilder("SELECT DATE(tanggal) AS label, SUM(total) AS value FROM transactions WHERE 1=1");
        appendDateFilter(sql, month, year, "tanggal");
        sql.append(" GROUP BY DATE(tanggal) ORDER BY label ASC");
        return mapQuery(sql.toString(), month, year);
    }

    public Map<String, Integer> bestSellingMenu(String month, String year) {
        StringBuilder sql = new StringBuilder("SELECT td.nama_menu AS label, SUM(td.qty) AS value " +
                "FROM transaction_details td JOIN transactions t ON td.id_transaksi=t.id_transaksi WHERE 1=1");
        appendDateFilter(sql, month, year, "t.tanggal");
        sql.append(" GROUP BY td.nama_menu ORDER BY value DESC LIMIT 5");
        return mapQuery(sql.toString(), month, year);
    }

    public Map<String, Integer> salesByCategory(String month, String year) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(m.kategori,'Lainnya') AS label, SUM(td.total) AS value " +
                "FROM transaction_details td JOIN transactions t ON td.id_transaksi=t.id_transaksi " +
                "LEFT JOIN menu_items m ON td.id_menu=m.id_menu WHERE 1=1");
        appendDateFilter(sql, month, year, "t.tanggal");
        sql.append(" GROUP BY COALESCE(m.kategori,'Lainnya')");
        return mapQuery(sql.toString(), month, year);
    }

    public List<String> findAvailableYears() {
        List<String> years = new ArrayList<>();
        String sql = "SELECT DISTINCT strftime('%Y', tanggal) AS tahun FROM transactions WHERE tanggal IS NOT NULL ORDER BY tahun DESC";
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) years.add(rs.getString("tahun"));
        } catch (SQLException e) { e.printStackTrace(); }
        if (years.isEmpty()) years.add(String.valueOf(LocalDateTime.now().getYear()));
        return years;
    }

    private Map<String, Integer> mapQuery(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("label"), rs.getInt("value"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Map<String, Integer> mapQuery(String sql, String month, String year) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindDateFilter(ps, 1, month, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("label"), rs.getInt("value"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private void appendDateFilter(StringBuilder sql, String month, String year, String column) {
        if (month != null && !month.equals("Semua") && !month.isBlank()) sql.append(" AND strftime('%m', ").append(column).append(")=?");
        if (year != null && !year.equals("Semua") && !year.isBlank()) sql.append(" AND strftime('%Y', ").append(column).append(")=?");
    }

    private int bindDateFilter(PreparedStatement ps, int index, String month, String year) throws SQLException {
        if (month != null && !month.equals("Semua") && !month.isBlank()) ps.setString(index++, month);
        if (year != null && !year.equals("Semua") && !year.isBlank()) ps.setString(index++, year);
        return index;
    }

    private Transaction map(ResultSet rs) throws SQLException {
        int promo = rs.getInt("id_promo");
        Integer idPromo = rs.wasNull() ? null : promo;
        return new Transaction(rs.getInt("id_transaksi"), rs.getString("kode_transaksi"), rs.getInt("id_user"), rs.getString("kasir"), idPromo,
                rs.getString("tipe_order"), rs.getString("metode_pembayaran"), rs.getInt("subtotal"), rs.getInt("diskon"), rs.getInt("total"), rs.getString("tanggal"), rs.getString("items"));
    }
}
