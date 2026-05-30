package model;

import utils.CurrencyUtil;

public class CartItem {
    private MenuItem menuItem;
    private int qty;

    public CartItem(MenuItem menuItem, int qty) {
        this.menuItem = menuItem;
        this.qty = qty;
    }
    public MenuItem getMenuItem() { return menuItem; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }
    public int getIdMenu() { return menuItem.getIdMenu(); }
    public String getNamaMenu() { return menuItem.getNamaMenu(); }
    public int getHarga() { return menuItem.getHarga(); }
    public int getTotal() { return menuItem.getHarga() * qty; }
    public String getHargaFormatted() { return CurrencyUtil.formatRupiah(getHarga()); }
    public String getTotalFormatted() { return CurrencyUtil.formatRupiah(getTotal()); }
}
