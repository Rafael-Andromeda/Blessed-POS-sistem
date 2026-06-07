package controller;

import dao.IngredientDAO;
import dao.PurchaseDAO;
import dao.SupplierDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Ingredient;
import model.Purchase;
import model.Supplier;

public class PurchaseController {
    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private ComboBox<Ingredient> ingredientCombo;
    @FXML private TextField amountField;
    @FXML private TextField priceField;
    @FXML private TextField noteField;
    @FXML private TableView<Purchase> table;
    @FXML private TableColumn<Purchase, String> dateColumn;
    @FXML private TableColumn<Purchase, String> supplierColumn;
    @FXML private TableColumn<Purchase, String> ingredientColumn;
    @FXML private TableColumn<Purchase, Double> amountColumn;
    @FXML private TableColumn<Purchase, String> unitColumn;
    @FXML private TableColumn<Purchase, String> totalColumn;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();

    @FXML public void initialize() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("namaSupplier"));
        ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        reloadMasterData(); load();
    }
    private void reloadMasterData() {
        supplierCombo.setItems(FXCollections.observableArrayList(supplierDAO.findAll()));
        ingredientCombo.setItems(FXCollections.observableArrayList(ingredientDAO.findAll()));
    }
    private void load() { table.setItems(FXCollections.observableArrayList(purchaseDAO.findAll())); }
    @FXML private void save() {
        Ingredient ing = ingredientCombo.getValue();
        if (ing == null) { alert("Pilih bahan baku."); return; }
        try {
            double amount = Double.parseDouble(amountField.getText());
            int price = priceField.getText().isBlank() ? 0 : Integer.parseInt(priceField.getText());
            if (amount <= 0) { alert("Jumlah harus lebih dari 0."); return; }
            Supplier s = supplierCombo.getValue();
            Purchase p = new Purchase();
            p.setIdSupplier(s == null ? 0 : s.getIdSupplier()); p.setIdBahan(ing.getIdBahan()); p.setJumlah(amount); p.setHargaSatuan(price); p.setCatatan(noteField.getText());
            purchaseDAO.save(p);
            clear(); reloadMasterData(); load();
            new Alert(Alert.AlertType.INFORMATION, "Pembelian tersimpan dan stok bahan baku sudah bertambah.").showAndWait();
        } catch (NumberFormatException e) { alert("Jumlah dan harga harus berupa angka."); }
        catch (Exception e) { alert("Gagal menyimpan pembelian: " + e.getMessage()); }
    }
    @FXML private void clear() { supplierCombo.setValue(null); ingredientCombo.setValue(null); amountField.clear(); priceField.clear(); noteField.clear(); }
    private void alert(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
}
