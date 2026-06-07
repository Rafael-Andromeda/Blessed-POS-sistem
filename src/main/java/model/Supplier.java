package model;

public class Supplier {
    private int idSupplier;
    private String namaSupplier;
    private String kontak;
    private String alamat;
    private String status;

    public Supplier() {}
    public Supplier(int idSupplier, String namaSupplier, String kontak, String alamat, String status) {
        this.idSupplier = idSupplier; this.namaSupplier = namaSupplier; this.kontak = kontak; this.alamat = alamat; this.status = status;
    }
    public int getIdSupplier() { return idSupplier; }
    public void setIdSupplier(int idSupplier) { this.idSupplier = idSupplier; }
    public String getNamaSupplier() { return namaSupplier; }
    public void setNamaSupplier(String namaSupplier) { this.namaSupplier = namaSupplier; }
    public String getKontak() { return kontak; }
    public void setKontak(String kontak) { this.kontak = kontak; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    @Override public String toString() { return namaSupplier; }
}
