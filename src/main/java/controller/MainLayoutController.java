package controller;

import app.MainApp;
import dao.SettingDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import model.AppSetting;
import model.User;
import utils.SessionManager;

import java.io.File;
import java.io.IOException;

public class MainLayoutController {
    @FXML private StackPane contentPane;
    @FXML private Label appNameLabel;
    @FXML private Label shopLabel;
    @FXML private Label userInitialLabel;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView logoImage;
    @FXML private Button dashboardButton;
    @FXML private Button menuButton;
    @FXML private Button cashierButton;
    @FXML private Button inventoryButton;
    @FXML private Button supplierButton;
    @FXML private Button purchaseButton;
    @FXML private Button historyButton;
    @FXML private Button reportButton;
    @FXML private Button promoButton;
    @FXML private Button settingButton;

    private final SettingDAO settingDAO = new SettingDAO();

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            userInitialLabel.setText(user.getInitials());
            userNameLabel.setText(user.getNama());
            userRoleLabel.setText(user.getRole());
            boolean isAdmin = "Admin".equalsIgnoreCase(user.getRole());
            menuButton.setDisable(!isAdmin);
            inventoryButton.setDisable(!isAdmin);
            if (supplierButton != null) supplierButton.setDisable(!isAdmin);
            if (purchaseButton != null) purchaseButton.setDisable(!isAdmin);
            reportButton.setDisable(!isAdmin);
            promoButton.setDisable(!isAdmin);
            settingButton.setDisable(!isAdmin);
        }
        loadSetting();
        openDashboard();
    }

    public void loadSetting() {
        AppSetting setting = settingDAO.getSetting();
        appNameLabel.setText(setting.getNamaAplikasi());
        shopLabel.setText("Point of Sale");
        String logoPath = setting.getLogoPath();
        try {
            if (logoPath != null && !logoPath.isBlank() && new File(logoPath).exists()) {
                logoImage.setImage(new Image(new File(logoPath).toURI().toString()));
            } else {
                logoImage.setImage(new Image(getClass().getResourceAsStream("/images/default-logo.png")));
            }
        } catch (Exception ignored) { }
    }

    private void loadPage(String fxml) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/view/" + fxml));
            contentPane.getChildren().setAll(page);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void openDashboard() { loadPage("dashboard.fxml"); }
    @FXML private void openMenu() { loadPage("menu.fxml"); }
    @FXML private void openCashier() { loadPage("cashier.fxml"); }
    @FXML private void openInventory() { loadPage("inventory.fxml"); }
    @FXML private void openSupplier() { loadPage("supplier.fxml"); }
    @FXML private void openPurchase() { loadPage("purchase.fxml"); }
    @FXML private void openHistory() { loadPage("history.fxml"); }
    @FXML private void openReport() { loadPage("report.fxml"); }
    @FXML private void openPromo() { loadPage("promo.fxml"); }
    @FXML private void openSetting() { loadPage("setting.fxml"); }

    @FXML private void logout() {
        SessionManager.clear();
        MainApp.showLogin();
    }
}
