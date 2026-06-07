package controller;

import app.MainApp;
import dao.SettingDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import utils.DatabaseBackupUtil;
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

    @FXML private void backupDatabase() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Pilih Folder Backup Database");
        File dir = dc.showDialog(MainApp.getPrimaryStage());
        if (dir == null) return;
        try {
            File backup = DatabaseBackupUtil.backupTo(dir);
            new Alert(Alert.AlertType.INFORMATION, "Backup berhasil dibuat: " + backup.getAbsolutePath()).showAndWait();
        } catch (Exception e) { new Alert(Alert.AlertType.ERROR, "Backup gagal: " + e.getMessage()).showAndWait(); }
    }

    @FXML private void restoreDatabase() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih File Backup Database");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        File f = fc.showOpenDialog(MainApp.getPrimaryStage());
        if (f == null) return;
        if (new Alert(Alert.AlertType.CONFIRMATION, "Restore akan menimpa database aktif. Lanjutkan?").showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL) != javafx.scene.control.ButtonType.OK) return;
        try {
            DatabaseBackupUtil.restoreFrom(f);
            new Alert(Alert.AlertType.INFORMATION, "Restore berhasil. Tutup dan buka ulang aplikasi agar data refresh.").showAndWait();
        } catch (Exception e) { new Alert(Alert.AlertType.ERROR, "Restore gagal: " + e.getMessage()).showAndWait(); }
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
