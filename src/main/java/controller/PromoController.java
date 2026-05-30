package controller;

import dao.PromoDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Promo;

public class PromoController {
    @FXML private TableView<Promo> table;
    @FXML private TableColumn<Promo, String> nameColumn;
    @FXML private TableColumn<Promo, String> typeColumn;
    @FXML private TableColumn<Promo, Integer> valueColumn;
    @FXML private TableColumn<Promo, Integer> minColumn;
    @FXML private TableColumn<Promo, String> statusColumn;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField valueField;
    @FXML private TextField minPurchaseField;
    @FXML private DatePicker startPicker;
    @FXML private DatePicker endPicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label formTitleLabel;

    private final PromoDAO dao = new PromoDAO();
    private Promo selected;

    @FXML public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("namaPromo"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("jenisPromo"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("nilaiPromo"));
        minColumn.setCellValueFactory(new PropertyValueFactory<>("minimalPembelian"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        typeCombo.getItems().setAll("Persen", "Nominal"); typeCombo.setValue("Persen");
        statusCombo.getItems().setAll("Aktif", "Nonaktif"); statusCombo.setValue("Aktif");
        table.getSelectionModel().selectedItemProperty().addListener((obs,o,n)->select(n));
        load();
    }

    private void load() { table.setItems(FXCollections.observableArrayList(dao.findAll(false))); }

    private void select(Promo p) {
        if (p == null) return;
        selected=p; formTitleLabel.setText("Edit Promo");
        nameField.setText(p.getNamaPromo()); typeCombo.setValue(p.getJenisPromo());
        valueField.setText(String.valueOf(p.getNilaiPromo())); minPurchaseField.setText(String.valueOf(p.getMinimalPembelian()));
        statusCombo.setValue(p.getStatus());
        if (p.getTanggalMulai()!=null && !p.getTanggalMulai().isBlank()) startPicker.setValue(java.time.LocalDate.parse(p.getTanggalMulai())); else startPicker.setValue(null);
        if (p.getTanggalSelesai()!=null && !p.getTanggalSelesai().isBlank()) endPicker.setValue(java.time.LocalDate.parse(p.getTanggalSelesai())); else endPicker.setValue(null);
    }

    @FXML private void save() {
        try {
            Promo p = selected == null ? new Promo() : selected;
            p.setNamaPromo(nameField.getText().trim());
            p.setJenisPromo(typeCombo.getValue());
            p.setNilaiPromo(Integer.parseInt(valueField.getText().trim()));
            p.setMinimalPembelian(Integer.parseInt(minPurchaseField.getText().trim()));
            p.setTanggalMulai(startPicker.getValue()==null?null:startPicker.getValue().toString());
            p.setTanggalSelesai(endPicker.getValue()==null?null:endPicker.getValue().toString());
            p.setStatus(statusCombo.getValue());
            if (p.getNamaPromo().isBlank()) { show("Nama promo wajib diisi."); return; }
            boolean ok = selected==null ? dao.insert(p) : dao.update(p);
            if (ok) { clear(); load(); }
        } catch (NumberFormatException e) { show("Nilai promo dan minimal pembelian wajib angka."); }
    }

    @FXML private void delete() {
        if (selected == null) return;
        dao.delete(selected.getIdPromo()); clear(); load();
    }

    @FXML private void clear() {
        selected=null; table.getSelectionModel().clearSelection(); formTitleLabel.setText("Tambah Promo");
        nameField.clear(); valueField.clear(); minPurchaseField.setText("0"); startPicker.setValue(null); endPicker.setValue(null); typeCombo.setValue("Persen"); statusCombo.setValue("Aktif");
    }

    private void show(String msg) { new Alert(Alert.AlertType.WARNING, msg).showAndWait(); }
}
