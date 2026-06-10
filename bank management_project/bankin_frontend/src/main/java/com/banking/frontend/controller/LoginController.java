package com.banking.frontend.controller;

import com.banking.frontend.MainApp;
import com.banking.frontend.model.LoginResponse;
import com.banking.frontend.service.ApiService;
import com.banking.frontend.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginController {
    @FXML
    private StackPane rootPane;

    @FXML
    private VBox formBox;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label statusLabel;

    private final ApiService apiService = new ApiService();
    private final SessionManager sessionManager = new SessionManager();

    @FXML
    private void initialize() {
        // Layout/sizing is now driven entirely by FXML + app.css.
        // Submit on Enter from either field for a smoother sign-in flow.
        usernameField.setOnAction(this::handleLogin);
        passwordField.setOnAction(this::handleLogin);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        statusLabel.setText("");
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showDialog(Alert.AlertType.ERROR, "Login Error", "Username and password are required.");
            return;
        }

        loginButton.setDisable(true);
        try {
            LoginResponse response = apiService.login(username.trim(), password.trim());
            if (response == null || response.getRole() == null) {
                showDialog(Alert.AlertType.ERROR, "Login Error", "Invalid login response from server.");
                return;
            }

            // Store the auth token first so the dashboard's initial API calls are authorized.
            sessionManager.setToken(response.getToken());
            sessionManager.saveSession(
                    response.getUserId(),
                    response.getUsername(),
                    response.getRole(),
                    response.getAccountId()
            );
            if ("ADMIN".equalsIgnoreCase(response.getRole())) {
                MainApp.showAdminDashboard();
            } else {
                MainApp.showUserDashboard();
            }
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Login Failed", "Login failed: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void showDialog(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
