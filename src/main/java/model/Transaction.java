package model;

import utils.CurrencyUtil;

public class Transaction {
    private int idTransaksi;
    private String kodeTransaksi;
    private int idUser;
    private String kasir;
    private Integer idPromo;
    private String tipeOrder;
    private String metodePembayaran;
    private int subtotal;
    private int diskon;
    private int total;
    private String tanggal;
    private String items;

    public Transaction() {}

    public Transaction(int idTransaksi, String kodeTransaksi, int idUser, String kasir, Integer idPromo, String tipeOrder, String metodePembayaran, int subtotal, int diskon, int total, String tanggal, String items) {
        this.idTransaksi = idTransaksi;
        this.kodeTransaksi = kodeTransaksi;
        this.idUser = idUser;
        this.kasir = kasir;
        this.idPromo = idPromo;
        this.tipeOrder = tipeOrder;
        this.metodePembayaran = metodePembayaran;
        this.subtotal = subtotal;
        this.diskon = diskon;
        this.total = total;
        this.tanggal = tanggal;
        this.items = items;
    }

    public int getIdTransaksi() { return idTransaksi; }
    public void setIdTransaksi(int idTransaksi) { this.idTransaksi = idTransaksi; }
    public String getKodeTransaksi() { return kodeTransaksi; }
    public void setKodeTransaksi(String kodeTransaksi) { this.kodeTransaksi = kodeTransaksi; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getKasir() { return kasir; }
    public void setKasir(String kasir) { this.kasir = kasir; }
    public Integer getIdPromo() { return idPromo; }
    public void setIdPromo(Integer idPromo) { this.idPromo = idPromo; }
    public String getTipeOrder() { return tipeOrder; }
    public void setTipeOrder(String tipeOrder) { this.tipeOrder = tipeOrder; }
    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }
    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public int getDiskon() { return diskon; }
    public void setDiskon(int diskon) { this.diskon = diskon; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }
    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }
    public String getSubtotalFormatted() { return CurrencyUtil.formatRupiah(subtotal); }
    public String getDiskonFormatted() { return CurrencyUtil.formatRupiah(diskon); }
    public String getTotalFormatted() { return CurrencyUtil.formatRupiah(total); }
}
