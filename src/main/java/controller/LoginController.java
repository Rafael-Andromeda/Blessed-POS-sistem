package controller;

import app.MainApp;
import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.User;
import utils.SessionManager;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();
        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Username dan password wajib diisi.");
            return;
        }
        User user = userDAO.authenticate(username, password);
        if (user == null) {
            errorLabel.setText("Login gagal. Username/password salah.");
            return;
        }
        SessionManager.setCurrentUser(user);
        MainApp.showMain();
    }

    @FXML
    private void showDemoInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Akun Demo");
        alert.setHeaderText("Gunakan akun dummy berikut");
        alert.setContentText("Admin: admin / admin123\nKasir: kasir / kasir123");
        alert.showAndWait();
    }
}
