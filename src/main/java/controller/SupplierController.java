package controller;

import dao.SupplierDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Supplier;

public class SupplierController {
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextArea addressArea;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TableView<Supplier> table;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, String> statusColumn;

    private final SupplierDAO dao = new SupplierDAO();
    private Supplier selected;

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("namaSupplier"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("kontak"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("alamat"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCombo.getItems().setAll("Aktif", "Nonaktif");
        statusCombo.setValue("Aktif");
        table.getSelectionModel().selectedItemProperty().addListener((o,a,b)->select(b));
        load();
    }
    private void load() { table.setItems(FXCollections.observableArrayList(dao.findAll())); }
    private void select(Supplier s) {
        selected = s;
        if (s == null) return;
        nameField.setText(s.getNamaSupplier()); contactField.setText(s.getKontak()); addressArea.setText(s.getAlamat()); statusCombo.setValue(s.getStatus());
    }
    @FXML private void save() {
        if (nameField.getText().isBlank()) { alert("Nama supplier wajib diisi."); return; }
        Supplier s = new Supplier(selected == null ? 0 : selected.getIdSupplier(), nameField.getText(), contactField.getText(), addressArea.getText(), statusCombo.getValue());
        boolean ok = selected == null ? dao.insert(s) : dao.update(s);
        if (ok) { clear(); load(); }
        else alert("Gagal menyimpan supplier.");
    }
    @FXML private void delete() {
        if (selected == null) { alert("Pilih supplier dahulu."); return; }
        if (new Alert(Alert.AlertType.CONFIRMATION, "Hapus supplier ini?").showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (dao.delete(selected.getIdSupplier())) { clear(); load(); } else alert("Gagal menghapus. Supplier mungkin sudah dipakai pada pembelian.");
        }
    }
    @FXML private void clear() { selected = null; table.getSelectionModel().clearSelection(); nameField.clear(); contactField.clear(); addressArea.clear(); statusCombo.setValue("Aktif"); }
    private void alert(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
}
