package com.banking.frontend.controller;

import com.banking.frontend.MainApp;
import com.banking.frontend.model.CreditRequest;
import com.banking.frontend.model.Transaction;
import com.banking.frontend.model.User;
import com.banking.frontend.service.ApiService;
import com.banking.frontend.service.SessionManager;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.prefs.Preferences;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Label statUsersLabel;

    @FXML
    private Label statTransactionsLabel;

    @FXML
    private Label statRequestsLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> userIdColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> accountIdColumn;

    @FXML
    private TableColumn<User, String> balanceColumn;

    @FXML
    private TableColumn<User, Boolean> blockedColumn;

    @FXML
    private Button refreshUsersButton;

    @FXML
    private Button createUserButton;

    @FXML
    private Button editUserButton;

    @FXML
    private Button deleteUserButton;

    @FXML
    private Button toggleBlockButton;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField accountIdField;

    @FXML
    private TextField initialBalanceField;

    @FXML
    private Label blockedStatusLabel;

    @FXML
    private TextArea adminLogArea;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, String> txDateColumn;

    @FXML
    private TableColumn<Transaction, String> txSourceColumn;

    @FXML
    private TableColumn<Transaction, String> txDestColumn;

    @FXML
    private TableColumn<Transaction, String> txAmountColumn;

    @FXML
    private TableColumn<Transaction, String> txDescriptionColumn;

    @FXML
    private TableColumn<Transaction, String> txStatusColumn;

    @FXML
    private Button refreshTransactionsButton;


    @FXML
    private TableView<CreditRequest> creditRequestsTable;

    @FXML
    private TableColumn<CreditRequest, String> requestIdColumn;

    @FXML
    private TableColumn<CreditRequest, String> requestUserIdColumn;

    @FXML
    private TableColumn<CreditRequest, String> requestAmountColumn;

    @FXML
    private TableColumn<CreditRequest, String> requestStatusColumn;

    @FXML
    private Button approveRequestButton;

    @FXML
    private Button rejectRequestButton;

    @FXML
    private TextField creditAccountIdField;

    @FXML
    private TextField creditAmountField;

    @FXML
    private Button creditAccountButton;

    // ── Charts ────────────────────────────────────────────────────────────
    @FXML private BarChart<String, Number> txBarChart;

    // ── Overview labels ───────────────────────────────────────────────────
    @FXML private Label overviewUsersLabel;
    @FXML private Label overviewTxLabel;
    @FXML private Label overviewRequestsLabel;

    // ── Animation root ────────────────────────────────────────────────────
    @FXML private HBox analyticsRow;

    // ── Hero floating art ─────────────────────────────────────────────────
    @FXML private StackPane sectionDashboard;
    @FXML private VBox  heroCardBack;
    @FXML private VBox  heroCardFront;
    @FXML private Label coin1;
    @FXML private Label coin2;
    @FXML private Label spark1;
    @FXML private Label spark2;
    @FXML private Label spark3;

    // ── Nav sections ──────────────────────────────────────────────────────
    @FXML private HBox navDashboard;
    @FXML private HBox navUsers;
    @FXML private HBox navTransactions;
    @FXML private HBox navSettings;

    @FXML private HBox statCardsRow;
    @FXML private VBox sectionUsers;
    @FXML private HBox sectionUsersForm;
    @FXML private HBox sectionTransactionsAndApprovals;
    @FXML private VBox sectionSettings;

    // ── Settings controls (persisted locally) ────────────────────────────
    @FXML private TextField settingsName;
    @FXML private TextField settingsEmail;
    @FXML private PasswordField settingsCurrentPw;
    @FXML private PasswordField settingsNewPw;
    @FXML private CheckBox chk2fa;
    @FXML private CheckBox chkIdle;
    @FXML private CheckBox chkEmailNotif;
    @FXML private CheckBox chkTxAlerts;
    @FXML private CheckBox chkCreditAlerts;
    @FXML private CheckBox chkWeekly;

    private final Preferences settingsPrefs = Preferences.userRoot().node("amabank/admin-settings");

    private final SessionManager sessionManager = new SessionManager();
    private final ApiService apiService = new ApiService();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Admin Dashboard");
        configureTable();
        configureTransactionTable();
        configureCreditRequestTable();
        attachSelectionListener();
        attachCreditRequestSelectionListener();
        loadUsers();
        loadTransactions();
        loadCreditRequests();
        showDashboard();          // start on the Dashboard tab (hide the other sections)
        loadSettings();           // restore saved Settings values
        runEntryAnimations();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ENTRY ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════
    private void runEntryAnimations() {
        animateIn(sectionDashboard,   0);
        animateIn(analyticsRow, 160);
        startHeroAnimations();
    }

    // ── Nav item handler ──────────────────────────────────────────────────
    @FXML
    private void handleNavClick(javafx.scene.input.MouseEvent event) {
        HBox clicked = (HBox) event.getSource();
        setActiveNav(clicked);

        if (clicked == navDashboard)    showDashboard();
        else if (clicked == navUsers)   showUsers();
        else if (clicked == navTransactions) showTransactions();
        else if (clicked == navSettings)    showSettings();
    }

    private void setActiveNav(HBox active) {
        navDashboard.getStyleClass().remove("nav-item-active");
        navUsers.getStyleClass().remove("nav-item-active");
        navTransactions.getStyleClass().remove("nav-item-active");
        navSettings.getStyleClass().remove("nav-item-active");

        active.getStyleClass().add("nav-item-active");
    }

    /** Show or hide a section AND collapse its layout space when hidden. */
    private void toggle(javafx.scene.Node node, boolean show) {
        if (node == null) return;
        node.setVisible(show);
        node.setManaged(show);
    }

    private void showDashboard() {
        toggle(sectionDashboard, true);
        toggle(statCardsRow, true);
        toggle(analyticsRow, true);
        toggle(sectionUsers, false);
        toggle(sectionUsersForm, false);
        toggle(sectionTransactionsAndApprovals, false);
        toggle(sectionSettings, false);
    }

    private void showUsers() {
        toggle(sectionDashboard, false);
        toggle(statCardsRow, false);
        toggle(analyticsRow, false);
        toggle(sectionUsers, true);
        toggle(sectionUsersForm, true);
        toggle(sectionTransactionsAndApprovals, false);
        toggle(sectionSettings, false);
    }

    private void showTransactions() {
        toggle(sectionDashboard, false);
        toggle(statCardsRow, false);
        toggle(analyticsRow, false);
        toggle(sectionUsers, false);
        toggle(sectionUsersForm, false);
        toggle(sectionTransactionsAndApprovals, true);
        toggle(sectionSettings, false);
    }

    private void showSettings() {
        toggle(sectionDashboard, false);
        toggle(statCardsRow, false);
        toggle(analyticsRow, false);
        toggle(sectionUsers, false);
        toggle(sectionUsersForm, false);
        toggle(sectionTransactionsAndApprovals, false);
        toggle(sectionSettings, true);
    }

    // ── HERO: infinite floating cards, drifting coins, twinkling sparkles ──
    private void startHeroAnimations() {
        floatLoop(heroCardFront, 11, 2000, 0);
        floatLoop(heroCardBack,   8, 2400, 150);
        floatLoop(coin1,         14, 2200, 0);
        floatLoop(coin2,         16, 2600, 300);
        twinkle(spark1, 1600, 0);
        twinkle(spark2, 1300, 250);
        twinkle(spark3, 1900, 500);
    }

    private void floatLoop(Node node, double amplitude, double durMs, double delayMs) {
        if (node == null) return;
        TranslateTransition t = new TranslateTransition(Duration.millis(durMs), node);
        t.setFromY(0); t.setToY(-amplitude);
        t.setAutoReverse(true);
        t.setCycleCount(Animation.INDEFINITE);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.setDelay(Duration.millis(delayMs));
        t.play();
    }

    private void twinkle(Node node, double durMs, double delayMs) {
        if (node == null) return;
        FadeTransition f = new FadeTransition(Duration.millis(durMs), node);
        f.setFromValue(1.0); f.setToValue(0.25);
        f.setAutoReverse(true);
        f.setCycleCount(Animation.INDEFINITE);
        f.setDelay(Duration.millis(delayMs));
        f.play();
        ScaleTransition s = new ScaleTransition(Duration.millis(durMs), node);
        s.setFromX(0.7); s.setFromY(0.7);
        s.setToX(1.25);  s.setToY(1.25);
        s.setAutoReverse(true);
        s.setCycleCount(Animation.INDEFINITE);
        s.setDelay(Duration.millis(delayMs));
        s.play();
    }

    private void animateIn(Node node, double delayMs) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateY(24);
        FadeTransition fade  = new FadeTransition(Duration.millis(520), node);
        fade.setFromValue(0); fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(520), node);
        slide.setFromY(24); slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(node, fade, slide);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  COUNT-UP ANIMATION
    // ═══════════════════════════════════════════════════════════════════════
    private void animateCount(Label label, int target) {
        if (label == null) { return; }
        if (target == 0)   { label.setText("0"); return; }
        long startNs = System.nanoTime();
        long durNs   = 900_000_000L;
        new AnimationTimer() {
            @Override public void handle(long now) {
                double p = Math.min(1.0, (double)(now - startNs) / durNs);
                double e = 1.0 - Math.pow(1.0 - p, 3);
                label.setText(String.valueOf((int)(target * e)));
                if (p >= 1.0) { label.setText(String.valueOf(target)); stop(); }
            }
        }.start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BAR CHART — monthly transaction volume from real data
    // ═══════════════════════════════════════════════════════════════════════
    private void populateTxBarChart(List<Transaction> transactions) {
        if (txBarChart == null) return;
        String[] names  = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        Map<String,Integer> counts = new LinkedHashMap<>();
        for (String m : names) counts.put(m, 0);

        for (Transaction tx : transactions) {
            String month = extractMonth(tx.getDate());
            if (month != null && counts.containsKey(month)) {
                counts.put(month, counts.get(month) + 1);
            }
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transactions");
        for (Map.Entry<String,Integer> e : counts.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        txBarChart.getData().clear();
        txBarChart.getData().add(series);
    }

    /** Extract short month name from formatted date like "15 january 2025 2:30pm" */
    private String extractMonth(String dateStr) {
        if (dateStr == null) return null;
        String lower = dateStr.toLowerCase();
        String[][] map = {
            {"january","Jan"},{"february","Feb"},{"march","Mar"},{"april","Apr"},
            {"may","May"},{"june","Jun"},{"july","Jul"},{"august","Aug"},
            {"september","Sep"},{"october","Oct"},{"november","Nov"},{"december","Dec"}
        };
        for (String[] pair : map) {
            if (lower.contains(pair[0])) return pair[1];
        }
        // Fallback: yyyy-MM-dd prefix
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
            try {
                int m = Integer.parseInt(dateStr.substring(5, 7));
                if (m >= 1 && m <= 12) return map[m-1][1];
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void configureTable() {
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        accountIdColumn.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        // Display the real balance as currency; the raw value stays in the model for the edit form.
        balanceColumn.setCellValueFactory(cd -> new SimpleStringProperty(formatMoney(cd.getValue().getInitialBalance())));
        blockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
    }

    private void configureTransactionTable() {
        txDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        txSourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceDisplay"));
        txDestColumn.setCellValueFactory(new PropertyValueFactory<>("destinationAccountId"));
        txAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        txDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        // Every persisted transaction succeeded (failed ones throw and are never saved),
        // so each row is shown as a green "Completed" badge.
        txStatusColumn.setCellValueFactory(cd -> new SimpleStringProperty("Completed"));
        txStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge-success");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
    }

    private void configureCreditRequestTable() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        requestUserIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        requestAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void attachSelectionListener() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldUser, newUser) -> {
            if (newUser != null) {
                userIdField.setText(newUser.getUserId());
                usernameField.setText(newUser.getUsername());
                passwordField.setText("");
                accountIdField.setText(newUser.getAccountId());
                initialBalanceField.setText(newUser.getInitialBalance());
                blockedStatusLabel.setText(String.valueOf(newUser.isBlocked()));
            }
        });
    }

    private void attachCreditRequestSelectionListener() {
        creditRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldRequest, newRequest) -> {
            updateRequestActionButtons(newRequest);
        });
    }

    private void updateRequestActionButtons(CreditRequest request) {
        boolean pending = isRequestPending(request);
        approveRequestButton.setDisable(!pending);
        rejectRequestButton.setDisable(!pending);
    }

    private boolean isRequestPending(CreditRequest request) {
        return request != null && "pending".equalsIgnoreCase(request.getStatus() == null ? "" : request.getStatus().trim());
    }

    private void loadUsers() {
        try {
            List<User> users = apiService.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
            setStat(statUsersLabel, users.size());
            if (overviewUsersLabel != null) overviewUsersLabel.setText(users.size() + " active accounts");
            adminLogArea.setText("Loaded " + users.size() + " users.");
        } catch (Exception ex) {
            adminLogArea.setText("Failed to load users: " + ex.getMessage());
            showDialog(Alert.AlertType.ERROR, "Load Error", "Failed to load users: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void setLoading(boolean loading) {
        refreshUsersButton.setDisable(loading);
        refreshTransactionsButton.setDisable(loading);
        approveRequestButton.setDisable(loading);
        rejectRequestButton.setDisable(loading);
        creditAccountButton.setDisable(loading);
        createUserButton.setDisable(loading);
        editUserButton.setDisable(loading);
        deleteUserButton.setDisable(loading);
        toggleBlockButton.setDisable(loading);
        logoutButton.setDisable(loading);
    }

    private boolean isValidAmount(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed >= 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private void loadTransactions() {
        try {
            List<Transaction> transactions = apiService.getAllTransactions();
            transactionsTable.setItems(FXCollections.observableArrayList(transactions));
            setStat(statTransactionsLabel, transactions.size());
            if (overviewTxLabel != null) overviewTxLabel.setText(transactions.size() + " transactions");
            populateTxBarChart(transactions);
            adminLogArea.setText("Loaded " + transactions.size() + " transactions.");
        } catch (Exception ex) {
            adminLogArea.setText("Failed to load transactions: " + ex.getMessage());
            showDialog(Alert.AlertType.ERROR, "Load Error", "Failed to load transactions: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadCreditRequests() {
        try {
            List<CreditRequest> requests = apiService.getCreditRequests();
            creditRequestsTable.setItems(FXCollections.observableArrayList(requests));
            setStat(statRequestsLabel, requests.size());
            long pending = requests.stream()
                .filter(r -> "pending".equalsIgnoreCase(r.getStatus() == null ? "" : r.getStatus().trim()))
                .count();
            if (overviewRequestsLabel != null) overviewRequestsLabel.setText(pending + " pending");
            updateRequestActionButtons(creditRequestsTable.getSelectionModel().getSelectedItem());
            adminLogArea.setText("Loaded " + requests.size() + " credit requests.");
        } catch (Exception ex) {
            adminLogArea.setText("Failed to load credit requests: " + ex.getMessage());
            showDialog(Alert.AlertType.ERROR, "Load Error", "Failed to load credit requests: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleRefreshUsers(ActionEvent event) {
        setLoading(true);
        try {
            loadUsers();
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void handleRefreshTransactions(ActionEvent event) {
        setLoading(true);
        try {
            loadTransactions();
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void handleApproveRequest(ActionEvent event) {
        CreditRequest selectedRequest = creditRequestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showDialog(Alert.AlertType.WARNING, "Approve Request", "Select a credit request to approve.");
            return;
        }
        if (!isRequestPending(selectedRequest)) {
            String status = selectedRequest.getStatus() == null ? "processed" : selectedRequest.getStatus().trim();
            showDialog(Alert.AlertType.INFORMATION, "Approve Request", "This request has already been " + status + ". Only pending requests can be approved.");
            return;
        }
        try {
            String response = apiService.approveCreditRequest(selectedRequest.getRequestId());
            showDialog(Alert.AlertType.INFORMATION, "Approve Request", "Approved credit request successfully.");
            loadCreditRequests();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Approve Failed", "Approve failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleRejectRequest(ActionEvent event) {
        CreditRequest selectedRequest = creditRequestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showDialog(Alert.AlertType.WARNING, "Reject Request", "Select a credit request to reject.");
            return;
        }
        if (!isRequestPending(selectedRequest)) {
            String status = selectedRequest.getStatus() == null ? "processed" : selectedRequest.getStatus().trim();
            showDialog(Alert.AlertType.INFORMATION, "Reject Request", "This request has already been " + status + ". Only pending requests can be rejected.");
            return;
        }
        try {
            String response = apiService.rejectCreditRequest(selectedRequest.getRequestId());
            showDialog(Alert.AlertType.INFORMATION, "Reject Request", "Rejected credit request successfully.");
            loadCreditRequests();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Reject Failed", "Reject failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleCreditAccount(ActionEvent event) {
        String accountId = creditAccountIdField.getText().trim();
        String amount = creditAmountField.getText().trim();

        if (accountId.isBlank() || amount.isBlank()) {
            showDialog(Alert.AlertType.WARNING, "Credit Account", "Account ID and amount are required to credit an account.");
            return;
        }
        if (!isValidAmount(amount)) {
            showDialog(Alert.AlertType.WARNING, "Credit Account", "Enter a valid credit amount.");
            return;
        }

        setLoading(true);
        try {
            String response = apiService.creditAccount(Map.of("accountId", accountId, "amount", amount));
            showDialog(Alert.AlertType.INFORMATION, "Credit Account", "Credited account " + accountId + ": " + response);
            creditAccountIdField.clear();
            creditAmountField.clear();
            loadUsers();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Credit Failed", "Credit failed: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void handleCreateUser(ActionEvent event) {
        try {
            if (userIdField.getText().isBlank() || usernameField.getText().isBlank() || passwordField.getText().isBlank()
                    || accountIdField.getText().isBlank() || initialBalanceField.getText().isBlank()) {
                showDialog(Alert.AlertType.WARNING, "Create User", "All fields except blocked must be filled to create a user.");
                return;
            }
            User user = new User();
            user.setUserId(userIdField.getText().trim());
            user.setUsername(usernameField.getText().trim());
            user.setPassword(passwordField.getText().trim());
            user.setAccountId(accountIdField.getText().trim());
            user.setInitialBalance(initialBalanceField.getText().trim());
            apiService.createUser(user);
            showDialog(Alert.AlertType.INFORMATION, "Create User", "Created user successfully.");
            loadUsers();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Create Failed", "Create failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateUser(ActionEvent event) {
        try {
            String selectedId = userIdField.getText().trim();
            if (selectedId.isBlank()) {
                showDialog(Alert.AlertType.WARNING, "Update User", "Select a user or enter a valid User ID to update.");
                return;
            }
            User user = new User();
            user.setUsername(usernameField.getText().trim());
            user.setPassword(passwordField.getText().trim());
            user.setAccountId(accountIdField.getText().trim());
            user.setInitialBalance(initialBalanceField.getText().trim());
            apiService.updateUser(selectedId, user);
            showDialog(Alert.AlertType.INFORMATION, "Update User", "Updated user successfully.");
            loadUsers();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Update Failed", "Update failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        try {
            String selectedId = userIdField.getText().trim();
            if (selectedId.isBlank()) {
                showDialog(Alert.AlertType.WARNING, "Delete User", "Select a user or enter a valid User ID to delete.");
                return;
            }
            apiService.deleteUser(selectedId);
            showDialog(Alert.AlertType.INFORMATION, "Delete User", "Deleted user: " + selectedId);
            clearFields();
            loadUsers();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Delete Failed", "Delete failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleToggleBlock(ActionEvent event) {
        try {
            String selectedId = userIdField.getText().trim();
            if (selectedId.isBlank()) {
                showDialog(Alert.AlertType.WARNING, "Block User", "Select a user or enter a valid User ID to block/unblock.");
                return;
            }
            boolean currentlyBlocked = Boolean.parseBoolean(blockedStatusLabel.getText());
            apiService.blockUser(selectedId, !currentlyBlocked);
            blockedStatusLabel.setText(String.valueOf(!currentlyBlocked));
            showDialog(Alert.AlertType.INFORMATION, "Block User", !currentlyBlocked ? "User blocked successfully." : "User unblocked successfully.");
            loadUsers();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Block/Unblock Failed", "Block/unblock failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void clearFields() {
        userIdField.clear();
        usernameField.clear();
        passwordField.clear();
        accountIdField.clear();
        initialBalanceField.clear();
        blockedStatusLabel.setText("false");
    }

    private void setStat(Label label, int value) {
        animateCount(label, value);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SETTINGS — persisted locally via java.util.prefs (survives restarts)
    // ═══════════════════════════════════════════════════════════════════════
    private void loadSettings() {
        if (settingsName != null)  settingsName.setText(settingsPrefs.get("name", "AmaBank Admin"));
        if (settingsEmail != null) settingsEmail.setText(settingsPrefs.get("email", "admin@amabank.com"));
        setCheck(chk2fa,         settingsPrefs.getBoolean("twoFactor", false));
        setCheck(chkIdle,        settingsPrefs.getBoolean("idleSignout", true));
        setCheck(chkEmailNotif,  settingsPrefs.getBoolean("emailNotif", true));
        setCheck(chkTxAlerts,    settingsPrefs.getBoolean("txAlerts", true));
        setCheck(chkCreditAlerts,settingsPrefs.getBoolean("creditAlerts", true));
        setCheck(chkWeekly,      settingsPrefs.getBoolean("weekly", false));
    }

    private void setCheck(CheckBox box, boolean value) {
        if (box != null) box.setSelected(value);
    }

    private boolean isChecked(CheckBox box) {
        return box != null && box.isSelected();
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        if (settingsName != null)  settingsPrefs.put("name", settingsName.getText().trim());
        if (settingsEmail != null) settingsPrefs.put("email", settingsEmail.getText().trim());
        showDialog(Alert.AlertType.INFORMATION, "Settings", "Profile saved.");
    }

    @FXML
    private void handleUpdateSecurity(ActionEvent event) {
        settingsPrefs.putBoolean("twoFactor", isChecked(chk2fa));
        settingsPrefs.putBoolean("idleSignout", isChecked(chkIdle));
        if (settingsCurrentPw != null) settingsCurrentPw.clear();
        if (settingsNewPw != null) settingsNewPw.clear();
        showDialog(Alert.AlertType.INFORMATION, "Settings",
                "Security preferences saved.\n\nNote: the built-in administrator password is fixed in this build, so password changes here aren't applied.");
    }

    @FXML
    private void handleSavePreferences(ActionEvent event) {
        settingsPrefs.putBoolean("emailNotif", isChecked(chkEmailNotif));
        settingsPrefs.putBoolean("txAlerts", isChecked(chkTxAlerts));
        settingsPrefs.putBoolean("creditAlerts", isChecked(chkCreditAlerts));
        settingsPrefs.putBoolean("weekly", isChecked(chkWeekly));
        showDialog(Alert.AlertType.INFORMATION, "Settings", "Preferences saved.");
    }

    /** Formats a raw balance string (e.g. "500.00") as currency for display. */
    private String formatMoney(String raw) {
        if (raw == null || raw.isBlank()) {
            return "$0.00";
        }
        try {
            double v = Double.parseDouble(raw.trim().replaceAll("[^0-9.\\-]", ""));
            return String.format("$%,.2f", v);
        } catch (NumberFormatException ex) {
            return raw;
        }
    }

    private void showDialog(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        sessionManager.clearSession();
        try {
            MainApp.showLogin();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
