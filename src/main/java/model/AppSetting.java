package model;

public class AppSetting {
    private int idSetting;
    private String namaAplikasi;
    private String namaWarung;
    private String alamat;
    private String telepon;
    private String logoPath;
    private String updatedAt;

    public AppSetting() {}
    public AppSetting(int idSetting, String namaAplikasi, String namaWarung, String alamat, String telepon, String logoPath, String updatedAt) {
        this.idSetting = idSetting;
        this.namaAplikasi = namaAplikasi;
        this.namaWarung = namaWarung;
        this.alamat = alamat;
        this.telepon = telepon;
        this.logoPath = logoPath;
        this.updatedAt = updatedAt;
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
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
