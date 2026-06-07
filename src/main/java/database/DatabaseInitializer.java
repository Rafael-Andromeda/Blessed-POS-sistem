package database;

import dao.MenuDAO;
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
            migrateExistingDatabase(conn, st);
            seedData(st);
            seedSupportIngredients(st);
            seedDefaultMenuRecipes(st);
            MenuDAO.refreshMenuStockFromIngredients(conn, null);
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
                "is_deleted INTEGER DEFAULT 0," +
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

        st.execute("CREATE TABLE IF NOT EXISTS menu_bahan_baku (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "menu_id INTEGER NOT NULL," +
                "bahan_baku_id INTEGER NOT NULL," +
                "jumlah_dibutuhkan REAL NOT NULL," +
                "FOREIGN KEY (menu_id) REFERENCES menu_items(id_menu)," +
                "FOREIGN KEY (bahan_baku_id) REFERENCES ingredients(id_bahan))");

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
                "status TEXT DEFAULT 'Selesai'," +
                "cancel_reason TEXT," +
                "cancelled_at TEXT," +
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

        st.execute("CREATE TABLE IF NOT EXISTS suppliers (" +
                "id_supplier INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama_supplier TEXT NOT NULL," +
                "kontak TEXT," +
                "alamat TEXT," +
                "status TEXT DEFAULT 'Aktif'," +
                "created_at TEXT DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TEXT)");

        st.execute("CREATE TABLE IF NOT EXISTS purchases (" +
                "id_pembelian INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id_supplier INTEGER," +
                "id_bahan INTEGER NOT NULL," +
                "jumlah REAL NOT NULL," +
                "harga_satuan INTEGER DEFAULT 0," +
                "total INTEGER DEFAULT 0," +
                "tanggal TEXT DEFAULT CURRENT_TIMESTAMP," +
                "catatan TEXT," +
                "FOREIGN KEY (id_supplier) REFERENCES suppliers(id_supplier)," +
                "FOREIGN KEY (id_bahan) REFERENCES ingredients(id_bahan))");

        st.execute("CREATE TABLE IF NOT EXISTS app_settings (" +
                "id_setting INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nama_aplikasi TEXT DEFAULT 'NasiGoreng 71'," +
                "nama_warung TEXT DEFAULT 'Warung Makan'," +
                "alamat TEXT," +
                "telepon TEXT," +
                "logo_path TEXT," +
                "updated_at TEXT)");
    }

    private static void migrateExistingDatabase(Connection conn, Statement st) throws SQLException {
        if (!columnExists(conn, "menu_items", "is_deleted")) {
            st.execute("ALTER TABLE menu_items ADD COLUMN is_deleted INTEGER DEFAULT 0");
        }
        if (!columnExists(conn, "transactions", "status")) {
            st.execute("ALTER TABLE transactions ADD COLUMN status TEXT DEFAULT 'Selesai'");
        }
        if (!columnExists(conn, "transactions", "cancel_reason")) {
            st.execute("ALTER TABLE transactions ADD COLUMN cancel_reason TEXT");
        }
        if (!columnExists(conn, "transactions", "cancelled_at")) {
            st.execute("ALTER TABLE transactions ADD COLUMN cancelled_at TEXT");
        }
        st.execute("UPDATE menu_items SET is_deleted=0 WHERE is_deleted IS NULL");
        st.execute("UPDATE transactions SET status='Selesai' WHERE status IS NULL OR status=''");
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = conn.createStatement().executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) if (column.equalsIgnoreCase(rs.getString("name"))) return true;
        }
        return false;
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
            st.executeUpdate("INSERT INTO menu_items(nama_menu, kategori, harga, stok, gambar, status, is_deleted) VALUES " +
                    "('Nasi Goreng','Makanan',18000,50,'','Aktif',0)," +
                    "('Nasi Goreng Spesial','Makanan',22000,40,'','Aktif',0)," +
                    "('Mie Goreng','Makanan',16000,35,'','Aktif',0)," +
                    "('Ayam Geprek','Makanan',20000,30,'','Aktif',0)," +
                    "('Es Teh','Minuman',5000,100,'','Aktif',0)," +
                    "('Es Jeruk','Minuman',7000,80,'','Aktif',0)," +
                    "('Kopi Hitam','Minuman',8000,60,'','Aktif',0)," +
                    "('Kentang Goreng','Side Dish',12000,50,'','Aktif',0)," +
                    "('Telur Ceplok','Side Dish',5000,100,'','Aktif',0)");
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

    private static void seedSupportIngredients(Statement st) throws SQLException {
        insertIngredientIfMissing(st, "Jeruk", 80, "pcs", 20);
        insertIngredientIfMissing(st, "Kopi", 5, "kg", 1);
        insertIngredientIfMissing(st, "Kentang", 25, "kg", 5);
    }

    private static void insertIngredientIfMissing(Statement st, String name, double stock, String unit, double min) throws SQLException {
        st.executeUpdate("INSERT INTO ingredients(nama_bahan, stok, satuan, batas_minimum, status, updated_at) " +
                "SELECT '" + name + "', " + stock + ", '" + unit + "', " + min + ", 'In Stock', CURRENT_TIMESTAMP " +
                "WHERE NOT EXISTS (SELECT 1 FROM ingredients WHERE nama_bahan='" + name + "')");
    }

    private static void seedDefaultMenuRecipes(Statement st) throws SQLException {
        if (!isEmpty(st, "menu_bahan_baku")) return;
        addRecipe(st, "Nasi Goreng", "Beras", 0.20);
        addRecipe(st, "Nasi Goreng", "Telur", 1);
        addRecipe(st, "Nasi Goreng", "Bumbu Nasi Goreng", 0.05);
        addRecipe(st, "Nasi Goreng", "Minyak", 0.03);

        addRecipe(st, "Nasi Goreng Spesial", "Beras", 0.25);
        addRecipe(st, "Nasi Goreng Spesial", "Telur", 2);
        addRecipe(st, "Nasi Goreng Spesial", "Ayam", 0.15);
        addRecipe(st, "Nasi Goreng Spesial", "Bumbu Nasi Goreng", 0.07);
        addRecipe(st, "Nasi Goreng Spesial", "Minyak", 0.04);

        addRecipe(st, "Mie Goreng", "Mie", 1);
        addRecipe(st, "Mie Goreng", "Telur", 1);
        addRecipe(st, "Mie Goreng", "Bumbu Nasi Goreng", 0.05);
        addRecipe(st, "Mie Goreng", "Minyak", 0.03);

        addRecipe(st, "Ayam Geprek", "Ayam", 0.25);
        addRecipe(st, "Ayam Geprek", "Beras", 0.20);
        addRecipe(st, "Ayam Geprek", "Minyak", 0.05);

        addRecipe(st, "Es Teh", "Teh", 0.02);
        addRecipe(st, "Es Teh", "Gula", 0.03);

        addRecipe(st, "Es Jeruk", "Jeruk", 1);
        addRecipe(st, "Es Jeruk", "Gula", 0.03);

        addRecipe(st, "Kopi Hitam", "Kopi", 0.02);
        addRecipe(st, "Kopi Hitam", "Gula", 0.02);

        addRecipe(st, "Kentang Goreng", "Kentang", 0.20);
        addRecipe(st, "Kentang Goreng", "Minyak", 0.05);

        addRecipe(st, "Telur Ceplok", "Telur", 1);
        addRecipe(st, "Telur Ceplok", "Minyak", 0.01);
    }

    private static void addRecipe(Statement st, String menuName, String ingredientName, double amount) throws SQLException {
        st.executeUpdate("INSERT INTO menu_bahan_baku(menu_id, bahan_baku_id, jumlah_dibutuhkan) " +
                "SELECT m.id_menu, i.id_bahan, " + amount + " " +
                "FROM menu_items m, ingredients i " +
                "WHERE m.nama_menu='" + menuName + "' AND i.nama_bahan='" + ingredientName + "' " +
                "AND NOT EXISTS (SELECT 1 FROM menu_bahan_baku mbb WHERE mbb.menu_id=m.id_menu AND mbb.bahan_baku_id=i.id_bahan)");
    }
}
