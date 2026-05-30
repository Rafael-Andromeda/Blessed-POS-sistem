package model;

public class MenuIngredient {
    private int id;
    private int menuId;
    private int bahanBakuId;
    private String namaBahan;
    private String satuan;
    private double jumlahDibutuhkan;

    public MenuIngredient() {}

    public MenuIngredient(int id, int menuId, int bahanBakuId, String namaBahan, String satuan, double jumlahDibutuhkan) {
        this.id = id;
        this.menuId = menuId;
        this.bahanBakuId = bahanBakuId;
        this.namaBahan = namaBahan;
        this.satuan = satuan;
        this.jumlahDibutuhkan = jumlahDibutuhkan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }
    public int getBahanBakuId() { return bahanBakuId; }
    public void setBahanBakuId(int bahanBakuId) { this.bahanBakuId = bahanBakuId; }
    public String getNamaBahan() { return namaBahan; }
    public void setNamaBahan(String namaBahan) { this.namaBahan = namaBahan; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    public double getJumlahDibutuhkan() { return jumlahDibutuhkan; }
    public void setJumlahDibutuhkan(double jumlahDibutuhkan) { this.jumlahDibutuhkan = jumlahDibutuhkan; }
}
