package model;

import utils.CurrencyUtil;

/**
 * Model untuk entitas promosi.
 * Extends BaseModel untuk mewarisi field audit createdAt dan updatedAt (inheritance).
 */
public class Promo extends BaseModel {
    private int idPromo;
    private String namaPromo;
    private String jenisPromo;
    private int nilaiPromo;
    private int minimalPembelian;
    private String tanggalMulai;
    private String tanggalSelesai;
    private String status;

    public Promo() {}

    public Promo(int idPromo, String namaPromo, String jenisPromo, int nilaiPromo, int minimalPembelian, String tanggalMulai, String tanggalSelesai, String status, String createdAt, String updatedAt) {
        super(createdAt, updatedAt);
        this.idPromo = idPromo;
        this.namaPromo = namaPromo;
        this.jenisPromo = jenisPromo;
        this.nilaiPromo = nilaiPromo;
        this.minimalPembelian = minimalPembelian;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
        this.status = status;
    }

    public int getIdPromo() { return idPromo; }
    public void setIdPromo(int idPromo) { this.idPromo = idPromo; }
    public String getNamaPromo() { return namaPromo; }
    public void setNamaPromo(String namaPromo) { this.namaPromo = namaPromo; }
    public String getJenisPromo() { return jenisPromo; }
    public void setJenisPromo(String jenisPromo) { this.jenisPromo = jenisPromo; }
    public int getNilaiPromo() { return nilaiPromo; }
    public void setNilaiPromo(int nilaiPromo) { this.nilaiPromo = nilaiPromo; }
    public int getMinimalPembelian() { return minimalPembelian; }
    public void setMinimalPembelian(int minimalPembelian) { this.minimalPembelian = minimalPembelian; }
    public String getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(String tanggalMulai) { this.tanggalMulai = tanggalMulai; }
    public String getTanggalSelesai() { return tanggalSelesai; }
    public void setTanggalSelesai(String tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLabel() {
        if (idPromo == 0) return "Tanpa Promo";
        String nilai = "Persen".equals(jenisPromo) ? nilaiPromo + "%" : CurrencyUtil.formatRupiah(nilaiPromo);
        return namaPromo + " (" + nilai + ")";
    }

    @Override
    public String toString() { return getLabel(); }
}
