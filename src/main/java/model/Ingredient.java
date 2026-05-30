package model;

public class Ingredient {
    private int idBahan;
    private String namaBahan;
    private double stok;
    private String satuan;
    private double batasMinimum;
    private String status;
    private String createdAt;
    private String updatedAt;

    public Ingredient() {}
    public Ingredient(int idBahan, String namaBahan, double stok, String satuan, double batasMinimum, String status, String createdAt, String updatedAt) {
        this.idBahan = idBahan;
        this.namaBahan = namaBahan;
        this.stok = stok;
        this.satuan = satuan;
        this.batasMinimum = batasMinimum;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getIdBahan() { return idBahan; }
    public void setIdBahan(int idBahan) { this.idBahan = idBahan; }
    public String getNamaBahan() { return namaBahan; }
    public void setNamaBahan(String namaBahan) { this.namaBahan = namaBahan; }
    public double getStok() { return stok; }
    public void setStok(double stok) { this.stok = stok; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    public double getBatasMinimum() { return batasMinimum; }
    public void setBatasMinimum(double batasMinimum) { this.batasMinimum = batasMinimum; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return namaBahan + " (" + satuan + ")";
    }
}
