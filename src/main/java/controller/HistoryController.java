package controller;

import app.MainApp;
import dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import model.Transaction;
import model.TransactionDetail;
import utils.CSVExporter;

import java.io.File;
import java.util.List;

public class HistoryController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> monthCombo;
    @FXML private TableView<Transaction> table;
    @FXML private TableColumn<Transaction, String> codeColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> itemsColumn;
    @FXML private TableColumn<Transaction, String> totalColumn;
    @FXML private TableColumn<Transaction, String> paymentColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;

    private final TransactionDAO dao = new TransactionDAO();

    @FXML public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("kodeTransaksi"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        itemsColumn.setCellValueFactory(new PropertyValueFactory<>("items"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("metodePembayaran"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("tipeOrder"));
        monthCombo.getItems().setAll("Semua","01","02","03","04","05","06","07","08","09","10","11","12");
        monthCombo.setValue("Semua");
        searchField.textProperty().addListener((o,a,b)->load());
        monthCombo.setOnAction(e->load());
        table.setRowFactory(tv -> { TableRow<Transaction> r = new TableRow<>(); r.setOnMouseClicked(e -> { if (e.getClickCount()==2 && !r.isEmpty()) showDetail(r.getItem()); }); return r; });
        load();
    }

    private void load() { table.setItems(FXCollections.observableArrayList(dao.findAll(searchField.getText(), monthCombo.getValue()))); }

    @FXML private void showSelectedDetail() {
        Transaction t = table.getSelectionModel().getSelectedItem();
        if (t != null) showDetail(t);
    }

    private void showDetail(Transaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tanggal: ").append(t.getTanggal()).append("\nKasir: ").append(t.getKasir()).append("\n\n");
        for (TransactionDetail d : dao.findDetails(t.getIdTransaksi())) sb.append(d.getNamaMenu()).append(" x").append(d.getQty()).append(" = ").append(d.getTotalFormatted()).append("\n");
        sb.append("\nSubtotal: ").append(t.getSubtotalFormatted()).append("\nDiskon: ").append(t.getDiskonFormatted()).append("\nTotal: ").append(t.getTotalFormatted());
        Alert a = new Alert(Alert.AlertType.INFORMATION, sb.toString());
        a.setTitle("Detail Transaksi"); a.setHeaderText(t.getKodeTransaksi()); a.showAndWait();
    }

    @FXML private void exportCSV() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Riwayat Transaksi");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("history_transaksi.csv");
        File f = fc.showSaveDialog(MainApp.getPrimaryStage());
        if (f == null) return;
        try {
            CSVExporter.exportTransactions(f, table.getItems());
            new Alert(Alert.AlertType.INFORMATION, "Export berhasil: " + f.getAbsolutePath()).showAndWait();
        } catch (Exception e) { new Alert(Alert.AlertType.ERROR, "Export gagal: " + e.getMessage()).showAndWait(); }
    }
}
