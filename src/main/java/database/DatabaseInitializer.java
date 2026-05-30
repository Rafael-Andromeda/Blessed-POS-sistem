package database;

import utils.PasswordUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            createTables(st);
            seedData(st);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Statement st) throws SQLException {
        st.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id_user INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama TEXT NOT NULL," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL CHECK(role IN ('Admin', 'Kasir'))," +
                "status TEXT DEFAULT 'Aktif'," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP)");

        st.execute("CREATE TABLE IF NOT EXISTS menu_items (" +
                "id_menu INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama_menu TEXT NOT NULL," +
                "kategori TEXT NOT NULL CHECK(kategori IN ('Makanan', 'Minuman', 'Side Dish'))," +
                "harga INTEGER NOT NULL," +
                "stok INTEGER DEFAULT 0," +
                "gambar TEXT," +
                "status TEXT DEFAULT 'Aktif'," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS ingredients (" +
                "id_bahan INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama_bahan TEXT NOT NULL," +
                "stok REAL NOT NULL," +
                "satuan TEXT NOT NULL," +
                "batas_minimum REAL NOT NULL," +
                "status TEXT DEFAULT 'In Stock'," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS promos (" +
                "id_promo INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama_promo TEXT NOT NULL," +
                "jenis_promo TEXT NOT NULL CHECK(jenis_promo IN ('Persen', 'Nominal'))," +
                "nilai_promo INTEGER NOT NULL," +
                "minimal_pembelian INTEGER DEFAULT 0," +
                "tanggal_mulai TEXT," +
                "tanggal_selesai TEXT," +
                "status TEXT DEFAULT 'Aktif'," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                "id_transaksi INTEGER PRIMARY KEY AUTOINCREMENT," +
                "kode_transaksi TEXT UNIQUE NOT NULL," +
                "id_user INTEGER NOT NULL," +
                "id_promo INTEGER," +
                "tipe_order TEXT NOT NULL CHECK(tipe_order IN ('Dine In', 'Take Away'))," +
                "metode_pembayaran TEXT NOT NULL CHECK(metode_pembayaran IN ('Cash', 'QRIS', 'Debit', 'E-Wallet'))," +
                "subtotal INTEGER NOT NULL," +
                "diskon INTEGER DEFAULT 0," +
                "total INTEGER NOT NULL," +
                "tanggal TEXT DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (id_user) REFERENCES users(id_user)," +
                "FOREIGN KEY (id_promo) REFERENCES promos(id_promo))");

        st.execute("CREATE TABLE IF NOT EXISTS transaction_details (" +
                "id_detail INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_transaksi INTEGER NOT NULL," +
                "id_menu INTEGER NOT NULL," +
                "nama_menu TEXT NOT NULL," +
                "harga INTEGER NOT NULL," +
                "qty INTEGER NOT NULL," +
                "total INTEGER NOT NULL," +
                "FOREIGN KEY (id_transaksi) REFERENCES transactions(id_transaksi)," +
                "FOREIGN KEY (id_menu) REFERENCES menu_items(id_menu))");

        st.execute("CREATE TABLE IF NOT EXISTS app_settings (" +
                "id_setting INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama_aplikasi TEXT DEFAULT 'NasiGoreng 71'," +
                "nama_warung TEXT DEFAULT 'Warung Makan'," +
                "alamat TEXT," +
                "telepon TEXT," +
                "logo_path TEXT," +
                "updated_at TEXT)");
    }

    private static boolean isEmpty(Statement st, String table) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table);
        return rs.next() && rs.getInt(1) == 0;
    }

    private static void seedData(Statement st) throws SQLException {
        if (isEmpty(st, "users")) {
            st.executeUpdate("INSERT INTO users(nama, username, password, role) VALUES " +
                    "('Admin', 'admin', '" + PasswordUtil.hashPassword("admin123") + "', 'Admin')," +
                    "('Kasir', 'kasir', '" + PasswordUtil.hashPassword("kasir123") + "', 'Kasir')");
        }
        if (isEmpty(st, "menu_items")) {
            st.executeUpdate("INSERT INTO menu_items(nama_menu, kategori, harga, stok, gambar, status) VALUES " +
                    "('Nasi Goreng','Makanan',18000,50,'','Aktif')," +
                    "('Nasi Goreng Spesial','Makanan',22000,40,'','Aktif')," +
                    "('Mie Goreng','Makanan',16000,35,'','Aktif')," +
                    "('Ayam Geprek','Makanan',20000,30,'','Aktif')," +
                    "('Es Teh','Minuman',5000,100,'','Aktif')," +
                    "('Es Jeruk','Minuman',7000,80,'','Aktif')," +
                    "('Kopi Hitam','Minuman',8000,60,'','Aktif')," +
                    "('Kentang Goreng','Side Dish',12000,50,'','Aktif')," +
                    "('Telur Ceplok','Side Dish',5000,100,'','Aktif')");
        }
        if (isEmpty(st, "ingredients")) {
            st.executeUpdate("INSERT INTO ingredients(nama_bahan, stok, satuan, batas_minimum, status) VALUES " +
                    "('Beras',50,'kg',10,'In Stock')," +
                    "('Ayam',20,'kg',5,'In Stock')," +
                    "('Telur',100,'pcs',20,'In Stock')," +
                    "('Minyak',10,'liter',3,'In Stock')," +
                    "('Gula',15,'kg',5,'In Stock')," +
                    "('Bumbu Nasi Goreng',10,'kg',2,'In Stock')," +
                    "('Mie',30,'pcs',10,'In Stock')," +
                    "('Teh',5,'kg',1,'In Stock')");
        }
        if (isEmpty(st, "promos")) {
            st.executeUpdate("INSERT INTO promos(nama_promo, jenis_promo, nilai_promo, minimal_pembelian, status) VALUES " +
                    "('Diskon 10%','Persen',10,30000,'Aktif')," +
                    "('Diskon Rp 5.000','Nominal',5000,25000,'Aktif')");
        }
        if (isEmpty(st, "app_settings")) {
            st.executeUpdate("INSERT INTO app_settings(nama_aplikasi, nama_warung, alamat, telepon, logo_path, updated_at) " +
                    "VALUES ('NasiGoreng 71','Warung Makan','Jl. Contoh No. 71','081234567890','',CURRENT_TIMESTAMP)");
        }
    }
}
