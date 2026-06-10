package com.banking.frontend;

import com.banking.frontend.service.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    private static Stage primaryStage;
    private final SessionManager sessionManager = new SessionManager();

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        if (sessionManager.isLoggedIn()) {
            String role = sessionManager.getSavedRole();
            if ("ADMIN".equalsIgnoreCase(role)) {
                showAdminDashboard();
            } else {
                showUserDashboard();
            }
        } else {
            showLogin();
        }
        primaryStage.show();
    }

    public static void showLogin() throws IOException {
        primaryStage.setTitle("AmaBank — Sign In");
        Parent root = FXMLLoader.load(MainApp.class.getResource("/login.fxml"));
        primaryStage.setScene(new Scene(root, 1040, 660));
        primaryStage.centerOnScreen();
    }

    public static void showAdminDashboard() throws IOException {
        primaryStage.setTitle("AmaBank — Admin Console");
        Parent root = FXMLLoader.load(MainApp.class.getResource("/admin_dashboard.fxml"));
        primaryStage.setScene(new Scene(root, 1400, 900));
        primaryStage.centerOnScreen();
    }

    public static void showUserDashboard() throws IOException {
        primaryStage.setTitle("AmaBank — User Portal");
        Parent root = FXMLLoader.load(MainApp.class.getResource("/user_dashboard.fxml"));
        primaryStage.setScene(new Scene(root, 1280, 880));
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
