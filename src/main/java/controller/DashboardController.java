package controller;

import dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Transaction;
import utils.CurrencyUtil;

import java.util.List;
import java.util.Map;

public class DashboardController {
    @FXML private Label todayRevenueLabel;
    @FXML private Label todayTransactionLabel;
    @FXML private Label averageLabel;
    @FXML private Label customerLabel;
    @FXML private LineChart<String, Number> weeklyChart;
    @FXML private PieChart bestMenuChart;
    @FXML private TableView<Transaction> recentTable;
    @FXML private TableColumn<Transaction, String> codeColumn;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> itemsColumn;
    @FXML private TableColumn<Transaction, String> paymentColumn;
    @FXML private TableColumn<Transaction, String> totalColumn;

    private final TransactionDAO transactionDAO = new TransactionDAO();

    @FXML
    public void initialize() {
        todayRevenueLabel.setText(CurrencyUtil.formatRupiah(transactionDAO.getTodayRevenue()));
        todayTransactionLabel.setText(String.valueOf(transactionDAO.getTodayCount()));
        averageLabel.setText(CurrencyUtil.formatRupiah(transactionDAO.getTodayAverage()));
        customerLabel.setText(String.valueOf(transactionDAO.getTodayCount()));
        setupTable();
        loadWeeklyChart();
        loadBestMenuChart();
    }

    private void setupTable() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("kodeTransaksi"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        itemsColumn.setCellValueFactory(new PropertyValueFactory<>("items"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("metodePembayaran"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalFormatted"));
        List<Transaction> recent = transactionDAO.findAll("", "Semua");
        if (recent.size() > 8) recent = recent.subList(0, 8);
        recentTable.setItems(FXCollections.observableArrayList(recent));
    }

    private void loadWeeklyChart() {
        weeklyChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Penjualan Mingguan");
        for (Map.Entry<String, Integer> e : transactionDAO.salesWeekly().entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        weeklyChart.getData().add(series);
    }

    private void loadBestMenuChart() {
        bestMenuChart.getData().clear();
        for (Map.Entry<String, Integer> e : transactionDAO.bestSellingMenu().entrySet()) {
            bestMenuChart.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
        }
    }
}
