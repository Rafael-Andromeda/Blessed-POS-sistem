package controller;

import dao.IngredientDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Ingredient;

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

    private final IngredientDAO dao = new IngredientDAO();
    private Ingredient selected;

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("namaBahan"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stok"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("satuan"));
        minColumn.setCellValueFactory(new PropertyValueFactory<>("batasMinimum"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        unitCombo.getItems().setAll("kg","gram","liter","ml","pcs");
        unitCombo.setValue("kg");
        table.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->select(n));
        load();
    }

    private void load() {
        table.setItems(FXCollections.observableArrayList(dao.findAll()));
        alertLabel.setVisible(dao.hasLowStock());
        alertLabel.setManaged(dao.hasLowStock());
    }

    private void select(Ingredient i) {
        if (i == null) return;
        selected = i;
        formTitleLabel.setText("Edit Bahan");
        nameField.setText(i.getNamaBahan());
        stockField.setText(String.valueOf(i.getStok()));
        unitCombo.setValue(i.getSatuan());
        minField.setText(String.valueOf(i.getBatasMinimum()));
    }

    @FXML private void save() {
        try {
            Ingredient i = selected == null ? new Ingredient() : selected;
            i.setNamaBahan(nameField.getText().trim());
            i.setStok(Double.parseDouble(stockField.getText().trim()));
            i.setSatuan(unitCombo.getValue());
            i.setBatasMinimum(Double.parseDouble(minField.getText().trim()));
            if (i.getNamaBahan().isBlank()) { show("Nama bahan wajib diisi."); return; }
            boolean ok = selected == null ? dao.insert(i) : dao.update(i);
            if (ok) { clear(); load(); }
        } catch (NumberFormatException e) { show("Stok dan batas minimum wajib angka."); }
    }

    @FXML private void delete() {
        if (selected == null) return;
        boolean ok = dao.delete(selected.getIdBahan());
        if (!ok) { show("Bahan baku tidak bisa dihapus karena masih digunakan pada komposisi menu. Hapus/ubah komposisi menu terlebih dahulu."); return; }
        clear(); load();
    }

    @FXML private void clear() {
        selected=null; table.getSelectionModel().clearSelection(); formTitleLabel.setText("Tambah Bahan");
        nameField.clear(); stockField.clear(); minField.clear(); unitCombo.setValue("kg");
    }

    private void show(String msg){ new Alert(Alert.AlertType.WARNING,msg).showAndWait(); }
}
