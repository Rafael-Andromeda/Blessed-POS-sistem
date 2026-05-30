package model;

/**
 * Model untuk relasi menu dan bahan baku (menu_bahan_baku).
 */
public class MenuIngredient {
    private int id;
    private int menuId;
    private int bahanBakuId;
    private double jumlahDibutuhkan;

    // Untuk tampilan UI
    private String namaBahan;
    private String satuan;

    public MenuIngredient() {}

    public MenuIngredient(int id, int menuId, int bahanBakuId, double jumlahDibutuhkan, String namaBahan, String satuan) {
        this.id = id;
        this.menuId = menuId;
        this.bahanBakuId = bahanBakuId;
        this.jumlahDibutuhkan = jumlahDibutuhkan;
        this.namaBahan = namaBahan;
        this.satuan = satuan;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }
    public int getBahanBakuId() { return bahanBakuId; }
    public void setBahanBakuId(int bahanBakuId) { this.bahanBakuId = bahanBakuId; }
    public double getJumlahDibutuhkan() { return jumlahDibutuhkan; }
    public void setJumlahDibutuhkan(double jumlahDibutuhkan) { this.jumlahDibutuhkan = jumlahDibutuhkan; }
    public String getNamaBahan() { return namaBahan; }
    public void setNamaBahan(String namaBahan) { this.namaBahan = namaBahan; }
    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    @Override
    public String toString() {
        return namaBahan + " (" + jumlahDibutuhkan + " " + satuan + ")";
    }
}
