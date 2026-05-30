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
    public Transaction save(Transaction trx, List<CartItem> items) throws SQLException {
        String insertTrx = "INSERT INTO transactions(kode_transaksi,id_user,id_promo,tipe_order,metode_pembayaran,subtotal,diskon,total,tanggal) VALUES(?,?,?,?,?,?,?,?,datetime('now','localtime'))";
        String insertDetail = "INSERT INTO transaction_details(id_transaksi,id_menu,nama_menu,harga,qty,total) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
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
            conn.commit();
            Transaction saved = findById(trx.getIdTransaksi());
            return saved != null ? saved : trx;
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
        List<Transaction> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t.*, u.nama AS kasir, " +
                "(SELECT GROUP_CONCAT(nama_menu || ' x' || qty, ', ') FROM transaction_details WHERE id_transaksi=t.id_transaksi) AS items " +
                "FROM transactions t JOIN users u ON t.id_user=u.id_user WHERE 1=1");
        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (t.kode_transaksi LIKE ? OR u.nama LIKE ? OR t.metode_pembayaran LIKE ? OR t.tanggal LIKE ? OR EXISTS " +
                    "(SELECT 1 FROM transaction_details td WHERE td.id_transaksi=t.id_transaksi AND td.nama_menu LIKE ?))");
        }
        if (month != null && !month.equals("Semua") && !month.isBlank()) {
            sql.append(" AND strftime('%m', t.tanggal)=?");
        }
        sql.append(" ORDER BY t.tanggal DESC");
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i=1;
            if (keyword != null && !keyword.isBlank()) {
                for (int x=0;x<5;x++) ps.setString(i++, "%"+keyword+"%");
            }
            if (month != null && !month.equals("Semua") && !month.isBlank()) ps.setString(i, month);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<TransactionDetail> findDetails(int idTransaksi) {
        List<TransactionDetail> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM transaction_details WHERE id_transaksi=?")) {
            ps.setInt(1, idTransaksi);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(new TransactionDetail(rs.getInt("id_detail"), rs.getInt("id_transaksi"), rs.getInt("id_menu"), rs.getString("nama_menu"), rs.getInt("harga"), rs.getInt("qty"), rs.getInt("total")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getTodayRevenue() { return singleInt("SELECT COALESCE(SUM(total),0) FROM transactions WHERE DATE(tanggal)=DATE('now','localtime')"); }
    public int getTodayCount() { return singleInt("SELECT COUNT(*) FROM transactions WHERE DATE(tanggal)=DATE('now','localtime')"); }
    public int getTodayAverage() { return singleInt("SELECT COALESCE(AVG(total),0) FROM transactions WHERE DATE(tanggal)=DATE('now','localtime')"); }
    public int getMonthlyRevenue() { return singleInt("SELECT COALESCE(SUM(total),0) FROM transactions WHERE strftime('%Y-%m',tanggal)=strftime('%Y-%m','now','localtime')"); }
    public int getTotalTransactions() { return singleInt("SELECT COUNT(*) FROM transactions"); }
    public int getAverageTransaction() { return singleInt("SELECT COALESCE(AVG(total),0) FROM transactions"); }

    private int singleInt(String sql) {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public Map<String, Integer> salesDaily() { return mapQuery("SELECT DATE(tanggal) AS label, SUM(total) AS value FROM transactions GROUP BY DATE(tanggal) ORDER BY label ASC"); }
    public Map<String, Integer> salesWeekly() { return mapQuery("SELECT DATE(tanggal) AS label, SUM(total) AS value FROM transactions WHERE DATE(tanggal) >= DATE('now','localtime','-6 days') GROUP BY DATE(tanggal) ORDER BY label ASC"); }
    public Map<String, Integer> bestSellingMenu() { return mapQuery("SELECT nama_menu AS label, SUM(qty) AS value FROM transaction_details GROUP BY nama_menu ORDER BY value DESC LIMIT 5"); }
    public Map<String, Integer> salesByCategory() { return mapQuery("SELECT m.kategori AS label, SUM(td.total) AS value FROM transaction_details td JOIN menu_items m ON td.id_menu=m.id_menu GROUP BY m.kategori"); }

    private Map<String, Integer> mapQuery(String sql) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) map.put(rs.getString("label"), rs.getInt("value"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    private Transaction map(ResultSet rs) throws SQLException {
        int promo = rs.getInt("id_promo");
        Integer idPromo = rs.wasNull() ? null : promo;
        return new Transaction(rs.getInt("id_transaksi"), rs.getString("kode_transaksi"), rs.getInt("id_user"), rs.getString("kasir"), idPromo,
                rs.getString("tipe_order"), rs.getString("metode_pembayaran"), rs.getInt("subtotal"), rs.getInt("diskon"), rs.getInt("total"), rs.getString("tanggal"), rs.getString("items"));
    }
}
