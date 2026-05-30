-- Migrasi fitur QRIS, komposisi bahan baku, soft delete menu, dan sinkronisasi stok.
-- Catatan: SQLite tidak mendukung ALTER TABLE ADD COLUMN IF NOT EXISTS.
-- Pada aplikasi ini pengecekan kolom is_deleted sudah dibuat aman di DatabaseInitializer lewat PRAGMA table_info(menu_items).

CREATE TABLE IF NOT EXISTS menu_bahan_baku (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id INTEGER NOT NULL,
    bahan_baku_id INTEGER NOT NULL,
    jumlah_dibutuhkan REAL NOT NULL,
    FOREIGN KEY (menu_id) REFERENCES menu_items(id_menu),
    FOREIGN KEY (bahan_baku_id) REFERENCES ingredients(id_bahan)
);

-- Jalankan hanya jika kolom belum ada:
-- ALTER TABLE menu_items ADD COLUMN is_deleted INTEGER DEFAULT 0;

UPDATE menu_items SET is_deleted = 0 WHERE is_deleted IS NULL;

-- Refresh stok menu berbasis bahan baku untuk menu yang sudah punya komposisi.
UPDATE menu_items
SET stok = COALESCE((
    SELECT CAST(MIN(i.stok / mbb.jumlah_dibutuhkan) AS INTEGER)
    FROM menu_bahan_baku mbb
    JOIN ingredients i ON i.id_bahan = mbb.bahan_baku_id
    WHERE mbb.menu_id = menu_items.id_menu
      AND mbb.jumlah_dibutuhkan > 0
), stok),
updated_at = CURRENT_TIMESTAMP
WHERE id_menu IN (SELECT DISTINCT menu_id FROM menu_bahan_baku);
