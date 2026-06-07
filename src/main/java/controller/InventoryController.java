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

public class InventoryController {
    @FXML private Label alertLabel;
    @FXML private TableView<Ingredient> table;
    @FXML private TableColumn<Ingredient, String> nameColumn;
    @FXML private TableColumn<Ingredient, Double> stockColumn;
    @FXML private TableColumn<Ingredient, String> unitColumn;
    @FXML private TableColumn<Ingredient, Double> minColumn;
    @FXML private TableColumn<Ingredient, String> statusColumn;
    @FXML private TextField nameField;
    @FXML private TextField stockField;
    @FXML private ComboBox<String> unitCombo;
    @FXML private TextField minField;
    @FXML private Label formTitleLabel;

    @FXML private ComboBox<Supplier> supplierCombo;
    @FXML private ComboBox<Ingredient> purchaseIngredientCombo;
    @FXML private TextField amountField;
    @FXML private TextField priceField;
    @FXML private TextField noteField;
    @FXML private TableView<Purchase> purchaseTable;
    @FXML private TableColumn<Purchase, String> purchaseDateColumn;
    @FXML private TableColumn<Purchase, String> purchaseSupplierColumn;
    @FXML private TableColumn<Purchase, String> purchaseIngredientColumn;
    @FXML private TableColumn<Purchase, Double> purchaseAmountColumn;
    @FXML private TableColumn<Purchase, String> purchaseUnitColumn;
    @FXML private TableColumn<Purchase, String> purchaseTotalColumn;

    private final IngredientDAO ingredientDAO = new IngredientDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private Ingredient selected;

    @FXML
    public void initialize() {
        setupIngredientTable();
        setupPurchaseTable();

        unitCombo.getItems().setAll("kg", "gram", "liter", "ml", "pcs", "botol", "bungkus");
        unitCombo.setValue("kg");

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> select(newValue));
        refreshAll();
    }

    private void setupIngredientTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stok"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        minColumn.setCellValueFactory(new PropertyValueFactory<>("batasMinimum"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void setupPurchaseTable() {
        purchaseDateColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        purchaseSupplierColumn.setCellValueFactory(new PropertyValueFactory<>("namaSupplier"));
        purchaseIngredientColumn.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        purchaseAmountColumn.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        purchaseUnitColumn.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        purchaseTotalColumn.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
    }

    @FXML
    private void refreshAll() {
        loadIngredients();
        loadPurchases();
        reloadPurchaseMasterData();
        updateLowStockAlert();
    }

    private void loadIngredients() {
        table.setItems(FXCollections.observableArrayList(ingredientDAO.findAll()));
    }

    private void loadPurchases() {
        purchaseTable.setItems(FXCollections.observableArrayList(purchaseDAO.findAll()));
    }

    private void reloadPurchaseMasterData() {
        supplierCombo.setItems(FXCollections.observableArrayList(supplierDAO.findAll()));
        purchaseIngredientCombo.setItems(FXCollections.observableArrayList(ingredientDAO.findAll()));
    }

    private void updateLowStockAlert() {
        boolean hasLowStock = ingredientDAO.hasLowStock();
        alertLabel.setVisible(hasLowStock);
        alertLabel.setManaged(hasLowStock);
        if (hasLowStock) {
            alertLabel.setText("Low Stock Alert: " + ingredientDAO.getLowStockSummary());
        }
    }

    private void select(Ingredient ingredient) {
        if (ingredient == null) return;
        selected = ingredient;
        formTitleLabel.setText("Edit / Koreksi Bahan");
        nameField.setText(ingredient.getNamaBahan());
        stockField.setText(String.valueOf(ingredient.getStok()));
        unitCombo.setValue(ingredient.getSatuan());
        minField.setText(String.valueOf(ingredient.getBatasMinimum()));
    }

    @FXML
    private void save() {
        try {
            String name = nameField.getText() == null ? "" : nameField.getText().trim();
            String stockText = stockField.getText() == null ? "" : stockField.getText().trim();
            String minText = minField.getText() == null ? "" : minField.getText().trim();
            String unit = unitCombo.getValue();

            if (name.isBlank()) { showWarning("Nama bahan wajib diisi."); return; }
            if (unit == null || unit.isBlank()) { showWarning("Satuan wajib dipilih."); return; }
            if (stockText.isBlank() || minText.isBlank()) { showWarning("Stok dan batas minimum wajib diisi."); return; }

            double stock = Double.parseDouble(stockText);
            double minimum = Double.parseDouble(minText);
            if (stock < 0) { showWarning("Stok tidak boleh negatif."); return; }
            if (minimum < 0) { showWarning("Batas minimum tidak boleh negatif."); return; }

            Ingredient ingredient = selected == null ? new Ingredient() : selected;
            ingredient.setNamaBahan(name);
            ingredient.setStok(stock);
            ingredient.setSatuan(unit);
            ingredient.setBatasMinimum(minimum);

            boolean ok = selected == null ? ingredientDAO.insert(ingredient) : ingredientDAO.update(ingredient);
            if (ok) {
                clear();
                refreshAll();
                showInfo("Data bahan berhasil disimpan.");
            } else {
                showWarning("Data bahan gagal disimpan. Cek apakah nama bahan sudah benar dan database tidak sedang terkunci.");
            }
        } catch (NumberFormatException e) {
            showWarning("Stok dan batas minimum wajib berupa angka. Contoh: 10 atau 2.5");
        }
    }

    @FXML
    private void delete() {
        if (selected == null) {
            showWarning("Pilih bahan yang ingin dihapus dari tabel terlebih dahulu.");
            return;
        }
        boolean ok = ingredientDAO.delete(selected.getIdBahan());
        if (!ok) {
            showWarning("Bahan baku tidak bisa dihapus karena masih digunakan pada komposisi menu atau riwayat transaksi. Gunakan edit/koreksi stok jika masih diperlukan.");
            return;
        }
        clear();
        refreshAll();
        showInfo("Data bahan berhasil dihapus.");
    }

    @FXML
    private void clear() {
        selected = null;
        table.getSelectionModel().clearSelection();
        formTitleLabel.setText("Tambah Data Bahan");
        nameField.clear();
        stockField.clear();
        minField.clear();
        unitCombo.setValue("kg");
    }

    @FXML
    private void savePurchase() {
        Ingredient ingredient = purchaseIngredientCombo.getValue();
        if (ingredient == null) {
            showWarning("Pilih bahan baku yang dibeli. Jika bahan belum ada, tambahkan dulu melalui form Data Bahan di kanan.");
            return;
        }

        try {
            String amountText = amountField.getText() == null ? "" : amountField.getText().trim();
            String priceText = priceField.getText() == null ? "" : priceField.getText().trim();
            if (amountText.isBlank()) { showWarning("Jumlah pembelian wajib diisi."); return; }

            double amount = Double.parseDouble(amountText);
            int price = priceText.isBlank() ? 0 : Integer.parseInt(priceText);
            if (amount <= 0) { showWarning("Jumlah pembelian harus lebih dari 0."); return; }
            if (price < 0) { showWarning("Harga satuan tidak boleh negatif."); return; }

            Supplier supplier = supplierCombo.getValue();
            Purchase purchase = new Purchase();
            purchase.setIdSupplier(supplier == null ? 0 : supplier.getIdSupplier());
            purchase.setIdBahan(ingredient.getIdBahan());
            purchase.setJumlah(amount);
            purchase.setHargaSatuan(price);
            purchase.setCatatan(noteField.getText());

            purchaseDAO.save(purchase);
            clearPurchase();
            refreshAll();
            showInfo("Pembelian tersimpan dan stok bahan otomatis bertambah.");
        } catch (NumberFormatException e) {
            showWarning("Jumlah dan harga satuan wajib berupa angka. Contoh jumlah: 5, harga: 12000");
        } catch (Exception e) {
            showWarning("Gagal menyimpan pembelian: " + e.getMessage());
        }
    }

    @FXML
    private void clearPurchase() {
        supplierCombo.setValue(null);
        purchaseIngredientCombo.setValue(null);
        amountField.clear();
        priceField.clear();
        noteField.clear();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}
