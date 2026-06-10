package model;

/**
 * Model untuk pengaturan aplikasi.
 * Extends BaseModel untuk mewarisi field audit updatedAt (inheritance).
 */
public class AppSetting extends BaseModel {
    private int idSetting;
    private String namaAplikasi;
    private String namaWarung;
    private String alamat;
    private String telepon;
    private String logoPath;

    public AppSetting() {}

    public AppSetting(int idSetting, String namaAplikasi, String namaWarung, String alamat, String telepon, String logoPath, String updatedAt) {
        super(null, updatedAt);
        this.idSetting = idSetting;
        this.namaAplikasi = namaAplikasi;
        this.namaWarung = namaWarung;
        this.alamat = alamat;
        this.telepon = telepon;
        this.logoPath = logoPath;
    }

    public int getIdSetting() { return idSetting; }
    public void setIdSetting(int idSetting) { this.idSetting = idSetting; }
    public String getNamaAplikasi() { return namaAplikasi; }
    public void setNamaAplikasi(String namaAplikasi) { this.namaAplikasi = namaAplikasi; }
    public String getNamaWarung() { return namaWarung; }
    public void setNamaWarung(String namaWarung) { this.namaWarung = namaWarung; }
    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }
    public String getTelepon() { return telepon; }
    public void setTelepon(String telepon) { this.telepon = telepon; }
    public String getLogoPath() { return logoPath; }
    public void setLogoPath(String logoPath) { this.logoPath = logoPath; }

    @Override
    public String toString() {
        return namaAplikasi + " - " + namaWarung;
    }
}
