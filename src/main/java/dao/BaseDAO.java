package dao;

import java.util.List;

/**
 * Interface generik untuk operasi CRUD dasar.
 * Diterapkan sebagai abstraction layer pada semua DAO class.
 *
 * @param <T>  Tipe model entity
 * @param <ID> Tipe primary key
 */
public interface BaseDAO<T, ID> {

    /**
     * Menyimpan entity baru ke database.
     * @param entity objek yang akan disimpan
     * @return true jika berhasil, false jika gagal
     */
    boolean insert(T entity);

    /**
     * Memperbarui data entity di database.
     * @param entity objek yang akan diperbarui
     * @return true jika berhasil, false jika gagal
     */
    boolean update(T entity);

    /**
     * Menghapus entity dari database berdasarkan ID.
     * @param id primary key entity yang akan dihapus
     * @return true jika berhasil, false jika gagal
     */
    boolean delete(ID id);

    /**
     * Mengambil semua data entity dari database.
     * @return List berisi semua entity
     */
    List<T> findAll();
}
