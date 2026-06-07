package controller;

import app.MainApp;
import dao.SettingDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import model.AppSetting;
import model.User;
import utils.SessionManager;

import java.io.File;
import java.net.URL;

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

            String role = user.getRole() == null ? "" : user.getRole().trim();
            boolean isAdmin = "Admin".equalsIgnoreCase(role);
            setAdminOnly(menuButton, !isAdmin);
            setAdminOnly(inventoryButton, !isAdmin);
            setAdminOnly(supplierButton, !isAdmin);
            setAdminOnly(reportButton, !isAdmin);
            setAdminOnly(promoButton, !isAdmin);
            setAdminOnly(settingButton, !isAdmin);
        }
        loadSetting();
        openDashboard();
    }

    private void setAdminOnly(Button button, boolean disabled) {
        if (button != null) {
            button.setDisable(disabled);
        }
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
        } catch (Exception ignored) {
            // Aplikasi tetap jalan walaupun logo gagal dimuat.
        }
    }

    private void loadPage(String fxml) {
        try {
            URL resource = getClass().getResource("/view/" + fxml);
            if (resource == null) {
                showNavigationError("File tampilan tidak ditemukan: " + fxml);
                return;
            }
            Parent page = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(page);
        } catch (Exception e) {
            e.printStackTrace();
            showNavigationError("Gagal membuka halaman " + fxml + ": " + rootMessage(e));
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }

    private void showNavigationError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    @FXML private void openDashboard() { loadPage("dashboard.fxml"); }
    @FXML private void openMenu() { loadPage("menu.fxml"); }
    @FXML private void openCashier() { loadPage("cashier.fxml"); }

    // Inventory dan pembelian bahan sudah digabung dalam satu FXML agar stok otomatis sinkron.
    @FXML private void openInventory() { loadPage("inventory.fxml"); }

    @FXML private void openSupplier() { loadPage("supplier.fxml"); }
    @FXML private void openHistory() { loadPage("history.fxml"); }
    @FXML private void openReport() { loadPage("report.fxml"); }
    @FXML private void openPromo() { loadPage("promo.fxml"); }
    @FXML private void openSetting() { loadPage("setting.fxml"); }

    @FXML private void logout() {
        SessionManager.clear();
        MainApp.showLogin();
    }
}
