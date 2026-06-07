package model;

import utils.CurrencyUtil;

public class Purchase {
    private int idPembelian;
    private int idSupplier;
    private String namaSupplier;
    private int idBahan;
    private String namaBahan;
    private double jumlah;
    private String satuan;
    private int hargaSatuan;
    private int total;
    private String tanggal;
    private String catatan;

    public Purchase() {}
    public Purchase(int idPembelian, int idSupplier, String namaSupplier, int idBahan, String namaBahan, double jumlah, String satuan, int hargaSatuan, int total, String tanggal, String catatan) {
        this.idPembelian = idPembelian; this.idSupplier = idSupplier; this.namaSupplier = namaSupplier; this.idBahan = idBahan; this.namaBahan = namaBahan; this.jumlah = jumlah; this.satuan = satuan; this.hargaSatuan = hargaSatuan; this.total = total; this.tanggal = tanggal; this.catatan = catatan;
    }
    public int getIdPembelian() { return idPembelian; }
    public int getIdSupplier() { return idSupplier; }
    public void setIdSupplier(int idSupplier) { this.idSupplier = idSupplier; }
    public String getNamaSupplier() { return namaSupplier; }
    public int getIdBahan() { return idBahan; }
    public void setIdBahan(int idBahan) { this.idBahan = idBahan; }
    public String getNamaBahan() { return namaBahan; }
    public double getJumlah() { return jumlah; }
    public void setJumlah(double jumlah) { this.jumlah = jumlah; }
    public String getSatuan() { return satuan; }
    public int getHargaSatuan() { return hargaSatuan; }
    public void setHargaSatuan(int hargaSatuan) { this.hargaSatuan = hargaSatuan; }
    public int getTotal() { return total; }
    public String getTotalFormatted() { return CurrencyUtil.formatRupiah(total); }
    public String getTanggal() { return tanggal; }
    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
}
