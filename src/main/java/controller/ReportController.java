package controller;

import dao.TransactionDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import utils.CurrencyUtil;

import java.util.Map;

public class ReportController {
    @FXML private Label monthlyRevenueLabel;
    @FXML private Label totalTransactionLabel;
    @FXML private Label averageTransactionLabel;
    @FXML private LineChart<String, Number> dailyChart;
    @FXML private BarChart<String, Number> categoryChart;
    @FXML private PieChart bestMenuChart;

    private final TransactionDAO dao = new TransactionDAO();

    @FXML public void initialize() { reload(); }

    @FXML private void reload() {
        monthlyRevenueLabel.setText(CurrencyUtil.formatRupiah(dao.getMonthlyRevenue()));
        totalTransactionLabel.setText(String.valueOf(dao.getTotalTransactions()));
        averageTransactionLabel.setText(CurrencyUtil.formatRupiah(dao.getAverageTransaction()));
        loadDaily(); loadCategory(); loadBestMenu();
    }

    private void loadDaily() {
        dailyChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>(); s.setName("Penjualan Harian");
        for (Map.Entry<String,Integer> e: dao.salesDaily().entrySet()) s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        dailyChart.getData().add(s);
    }

    private void loadCategory() {
        categoryChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>(); s.setName("Kategori");
        for (Map.Entry<String,Integer> e: dao.salesByCategory().entrySet()) s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        categoryChart.getData().add(s);
    }

    private void loadBestMenu() {
        bestMenuChart.getData().clear();
        for (Map.Entry<String,Integer> e: dao.bestSellingMenu().entrySet()) bestMenuChart.getData().add(new PieChart.Data(e.getKey(), e.getValue()));
    }
}
