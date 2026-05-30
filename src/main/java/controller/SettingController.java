package controller;

import app.MainApp;
import dao.SettingDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import model.AppSetting;

import java.io.File;

public class SettingController {
    @FXML private TextField appNameField;
    @FXML private TextField shopNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField logoField;

    private final SettingDAO dao = new SettingDAO();
    private AppSetting setting;

    @FXML public void initialize() { load(); }

    private void load() {
        setting = dao.getSetting();
        appNameField.setText(setting.getNamaAplikasi());
        shopNameField.setText(setting.getNamaWarung());
        addressField.setText(setting.getAlamat());
        phoneField.setText(setting.getTelepon());
        logoField.setText(setting.getLogoPath());
    }

    @FXML private void browseLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Logo Aplikasi");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(MainApp.getPrimaryStage());
        if (f != null) logoField.setText(f.getAbsolutePath());
    }

    @FXML private void save() {
        setting.setNamaAplikasi(appNameField.getText());
        setting.setNamaWarung(shopNameField.getText());
        setting.setAlamat(addressField.getText());
        setting.setTelepon(phoneField.getText());
        setting.setLogoPath(logoField.getText());
        if (dao.update(setting)) new Alert(Alert.AlertType.INFORMATION, "Setting berhasil disimpan. Login ulang untuk refresh logo/sidebar.").showAndWait();
    }
}
