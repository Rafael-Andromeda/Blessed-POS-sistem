package model;

/**
 * Model untuk entitas bahan baku (Ingredient).
 * Extends BaseModel untuk mewarisi field audit createdAt dan updatedAt (inheritance).
 */
public class Ingredient extends BaseModel {
    private int idBahan;
    private String namaBahan;
    private double stok;
    private String satuan;
    private double batasMinimum;
    private String status;

    public Ingredient() {}

    public Ingredient(int idBahan, String namaBahan, double stok, String satuan, double batasMinimum, String status, String createdAt, String updatedAt) {
        super(createdAt, updatedAt);
        this.idBahan = idBahan;
        this.namaBahan = namaBahan;
        this.stok = stok;
        this.satuan = satuan;
        this.batasMinimum = batasMinimum;
        this.status = status;
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

    @Override
    public String toString() {
        return namaBahan + " (" + satuan + ")";
    }
}
