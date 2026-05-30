package model;

import utils.CurrencyUtil;

public class TransactionDetail {
    private int idDetail;
    private int idTransaksi;
    private int idMenu;
    private String namaMenu;
    private String kategori;
    private int harga;
    private int qty;
    private int total;

    public TransactionDetail() {}
    public TransactionDetail(int idDetail, int idTransaksi, int idMenu, String namaMenu, int harga, int qty, int total) {
        this(idDetail, idTransaksi, idMenu, namaMenu, "-", harga, qty, total);
    }
    public TransactionDetail(int idDetail, int idTransaksi, int idMenu, String namaMenu, String kategori, int harga, int qty, int total) {
        this.idDetail = idDetail;
        this.idTransaksi = idTransaksi;
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.kategori = kategori;
        this.harga = harga;
        this.qty = qty;
        this.total = total;
    }
    public int getIdDetail() { return idDetail; }
    public void setIdDetail(int idDetail) { this.idDetail = idDetail; }
    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }
    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public int getHarga() { return harga; }
    public void setHarga(int harga) { this.harga = harga; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getHargaFormatted() { return CurrencyUtil.formatRupiah(harga); }
    public String getTotalFormatted() { return CurrencyUtil.formatRupiah(total); }
}
