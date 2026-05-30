package controller;

import dao.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import utils.CurrencyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportController {
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label totalTransactionLabel;
    @FXML private Label averageTransactionLabel;
    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private LineChart<String, Number> dailyChart;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private PieChart bestMenuChart;

    private final TransactionDAO dao = new TransactionDAO();

    @FXML public void initialize() {
        monthCombo.setItems(FXCollections.observableArrayList("Semua","01","02","03","04","05","06","07","08","09","10","11","12"));
        monthCombo.setValue("Semua");
        List<String> years = new ArrayList<>();
        years.add("Semua");
        years.addAll(dao.findAvailableYears());
        yearCombo.setItems(FXCollections.observableArrayList(years));
        yearCombo.setValue("Semua");
        reload();
    }

    @FXML private void reload() {
        String month = selectedMonth();
        String year = selectedYear();
        monthlyRevenueLabel.setText(CurrencyUtil.formatRupiah(dao.getRevenue(month, year)));
        totalTransactionLabel.setText(String.valueOf(dao.getTotalTransactions(month, year)));
        averageTransactionLabel.setText(CurrencyUtil.formatRupiah(dao.getAverageTransaction(month, year)));
        loadDaily(month, year); loadCategory(month, year); loadBestMenu(month, year);
    }

    @FXML private void resetFilter() {
        monthCombo.setValue("Semua");
        yearCombo.setValue("Semua");
        reload();
    }

    private String selectedMonth() { return monthCombo == null ? "Semua" : monthCombo.getValue(); }
    private String selectedYear() { return yearCombo == null ? "Semua" : yearCombo.getValue(); }

    private void loadDaily(String month, String year) {
        dailyChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>(); s.setName("Penjualan Harian");
        for (Map.Entry<String,Integer> e: dao.salesDaily(month, year).entrySet()) s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        dailyChart.getData().add(s);
    }

    private void loadCategory(String month, String year) {
        categoryChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>(); s.setName("Kategori");
        for (Map.Entry<String,Integer> e: dao.salesByCategory(month, year).entrySet()) s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        categoryChart.getData().add(s);
    }

    private void loadBestMenu(String month, String year) {
        bestMenuChart.getData().clear();
        for (Map.Entry<String,Integer> e: dao.bestSellingMenu(month, year).entrySet()) bestMenuChart.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
    }
}
