package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class untuk mengelola koneksi ke database SQLite.
 *
 * Pola Singleton digunakan agar hanya ada satu titik akses koneksi
 * database di seluruh aplikasi, mencegah konflik resource dan memudahkan
 * pengelolaan konfigurasi koneksi secara terpusat.
 */
public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:nasigoreng71.db";

    // Satu-satunya instance (Singleton)
    private static DatabaseConnection instance;

    // Private constructor — mencegah instansiasi dari luar class
    private DatabaseConnection() {}

    /**
     * Mengembalikan satu-satunya instance DatabaseConnection (Singleton pattern).
     * Thread-safe menggunakan synchronized.
     *
     * @return instance tunggal DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Membuat dan mengembalikan koneksi baru ke database.
     * Bisa dipanggil langsung secara static (backward-compatible).
     *
     * @return Connection ke SQLite
     * @throws SQLException jika koneksi gagal dibuka
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
        return connection;
    }

    /**
     * Mengembalikan URL database yang digunakan.
     *
     * @return string URL database
     */
    public String getDbUrl() {
        return DB_URL;
    }
}
