package model;

import utils.CurrencyUtil;

/**
 * Model untuk entitas menu makanan/minuman.
 * Extends BaseModel untuk mewarisi field audit createdAt dan updatedAt (inheritance).
 */
public class MenuItem extends BaseModel {
    private int idMenu;
    private String namaMenu;
    private String kategori;
    private int harga;
    private int stok;
    private String gambar;
    private String status;
    private int isDeleted;

    public MenuItem() {}

    public MenuItem(int idMenu, String namaMenu, String kategori, int harga, int stok, String gambar, String status, String createdAt, String updatedAt) {
        this(idMenu, namaMenu, kategori, harga, stok, gambar, status, 0, createdAt, updatedAt);
    }

    public MenuItem(int idMenu, String namaMenu, String kategori, int harga, int stok, String gambar, String status, int isDeleted, String createdAt, String updatedAt) {
        super(createdAt, updatedAt);
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.kategori = kategori;
        this.harga = harga;
        this.stok = stok;
        this.gambar = gambar;
        this.status = status;
        this.isDeleted = isDeleted;
    }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public int getHarga() { return harga; }
    public void setHarga(int harga) { this.harga = harga; }
    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }
    public String getGambar() { return gambar; }
    public void setGambar(String gambar) { this.gambar = gambar; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }
    public String getHargaFormatted() { return CurrencyUtil.formatRupiah(harga); }

    @Override
    public String toString() {
        return namaMenu + " (" + kategori + ") - " + CurrencyUtil.formatRupiah(harga);
    }
}
