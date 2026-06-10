package com.banking.frontend.controller;

import com.banking.frontend.MainApp;
import com.banking.frontend.model.CreditRequest;
import com.banking.frontend.model.Transaction;
import com.banking.frontend.service.ApiService;
import com.banking.frontend.service.SessionManager;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class UserDashboardController {

    // ── Topbar ─────────────────────────────────────────────────────────────
    @FXML private Label  welcomeLabel;
    @FXML private Button logoutButton;

    // ── Stat cards ─────────────────────────────────────────────────────────
    @FXML private Label balanceLabel;
    @FXML private Label totalCreditLabel;
    @FXML private Label totalDebitLabel;

    // ── Physical card ──────────────────────────────────────────────────────
    @FXML private Label cardHolderLabel;
    @FXML private Label cardNumberLabel;
    @FXML private Label accountChip;
    @FXML private Label heroAcctLabel;
    @FXML private Label txCountLabel;

    // ── Quick actions form ─────────────────────────────────────────────────
    @FXML private TextField destinationAccountField;
    @FXML private TextField amountField;
    @FXML private TextField creditAmountField;
    @FXML private Button    transferButton;
    @FXML private Button    requestCreditButton;
    @FXML private Label     transferStatusLabel;

    // ── Filter bar ─────────────────────────────────────────────────────────
    @FXML private TextField accountFilterField;
    @FXML private Button    filterTransactionsButton;
    @FXML private Button    clearFilterButton;

    // ── Transaction table ──────────────────────────────────────────────────
    @FXML private TableView<Transaction>   transactionTable;
    @FXML private TableColumn<Transaction, String> dateColumn;
    @FXML private TableColumn<Transaction, String> typeColumn;
    @FXML private TableColumn<Transaction, String> amountColumn;
    @FXML private TableColumn<Transaction, String> descriptionColumn;

    // ── Credit requests table ──────────────────────────────────────────────
    @FXML private TableView<CreditRequest>  creditRequestsTable;
    @FXML private TableColumn<CreditRequest, String> requestIdColumn;
    @FXML private TableColumn<CreditRequest, String> requestAccountIdColumn;
    @FXML private TableColumn<CreditRequest, String> requestAmountColumn;
    @FXML private TableColumn<CreditRequest, String> requestStatusColumn;

    // ── Charts ─────────────────────────────────────────────────────────────
    @FXML private AreaChart<String, Number> balanceTrendChart;
    @FXML private PieChart                  transactionPieChart;

    // ── Animation roots ────────────────────────────────────────────────────
    @FXML private StackPane heroBanner;
    @FXML private HBox statCardsRow;
    @FXML private HBox cardRow;
    @FXML private HBox chartsRow;
    @FXML private VBox txCard;
    @FXML private VBox creditReqCard;
    @FXML private VBox mainCard;

    // ── Hero floating art ──────────────────────────────────────────────────
    @FXML private VBox  heroCardBack;
    @FXML private VBox  heroCardFront;
    @FXML private Label coin1;
    @FXML private Label coin2;
    @FXML private Label spark1;
    @FXML private Label spark2;
    @FXML private Label spark3;

    private final SessionManager sessionManager = new SessionManager();
    private final ApiService     apiService     = new ApiService();

    // ═══════════════════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    public void initialize() {
        String username = sessionManager.getSavedUsername();
        welcomeLabel.setText(username != null ? username : "User Dashboard");
        if (cardHolderLabel != null && username != null) {
            cardHolderLabel.setText(username.toUpperCase());
        }
        configureTable();
        configureCreditRequestTable();
        loadUserData(null);
        loadUserCreditRequests();
        runEntryAnimations();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ENTRY ANIMATIONS  — fade + slide-up, staggered
    // ═══════════════════════════════════════════════════════════════════════
    private void runEntryAnimations() {
        animateIn(heroBanner,    0);
        animateIn(statCardsRow,  110);
        animateIn(cardRow,       220);
        animateIn(chartsRow,     330);
        animateIn(txCard,        440);
        animateIn(creditReqCard, 550);
        startHeroAnimations();
        startCardFloat();
    }

    private void animateIn(Node node, double delayMs) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateY(28);
        FadeTransition fade = new FadeTransition(Duration.millis(520), node);
        fade.setFromValue(0); fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(520), node);
        slide.setFromY(28); slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);
        ParallelTransition pt = new ParallelTransition(node, fade, slide);
        pt.setDelay(Duration.millis(delayMs));
        pt.play();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HERO — infinite floating cards, drifting coins, twinkling sparkles
    // ═══════════════════════════════════════════════════════════════════════
    private void startHeroAnimations() {
        floatLoop(heroCardFront, 11, 2000, 0);
        floatLoop(heroCardBack,   8, 2400, 150);
        floatLoop(coin1,         14, 2200, 0);
        floatLoop(coin2,         16, 2600, 300);
        twinkle(spark1, 1600, 0);
        twinkle(spark2, 1300, 250);
        twinkle(spark3, 1900, 500);
    }

    /** Gentle bob on the main teal card. */
    private void startCardFloat() {
        floatLoop(mainCard, 6, 3200, 0);
    }

    /** Infinite up/down float that does not fight FXML rotate/layout. */
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

    /** Infinite twinkle — fade + scale pulse for sparkles. */
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

    // ═══════════════════════════════════════════════════════════════════════
    //  COUNT-UP ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /** Animate an integer count label from 0 → target over ~800 ms */
    private void animateCount(Label label, int target) {
        if (label == null) return;
        if (target == 0) { label.setText("0"); return; }
        long startNs   = System.nanoTime();
        long durNs     = 800_000_000L;
        new AnimationTimer() {
            @Override public void handle(long now) {
                double p = Math.min(1.0, (double)(now - startNs) / durNs);
                double e = 1.0 - Math.pow(1.0 - p, 3);          // ease-out cubic
                label.setText(String.valueOf((int)(target * e)));
                if (p >= 1.0) { label.setText(String.valueOf(target)); stop(); }
            }
        }.start();
    }

    /** Animate the balance label from $0.00 → target over ~1 s */
    private void animateBalance(Label label, double target) {
        if (label == null) return;
        long startNs = System.nanoTime();
        long durNs   = 1_000_000_000L;
        new AnimationTimer() {
            @Override public void handle(long now) {
                double p = Math.min(1.0, (double)(now - startNs) / durNs);
                double e = 1.0 - Math.pow(1.0 - p, 3);
                label.setText(String.format("$%.2f", target * e));
                if (p >= 1.0) { label.setText(String.format("$%.2f", target)); stop(); }
            }
        }.start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CHART POPULATION
    // ═══════════════════════════════════════════════════════════════════════
    private void populateCharts(List<Transaction> transactions, double currentBalance) {
        String myAccount = sessionManager.getSavedAccountId();

        // Classify every transaction relative to THIS account (real data).
        int    creditCount = 0, debitCount = 0;
        double creditSum   = 0, debitSum   = 0;
        for (Transaction tx : transactions) {
            int dir = direction(tx, myAccount);   // +1 incoming, -1 outgoing, 0 unknown
            double amt = parseAmount(tx.getAmount());
            if (dir > 0)      { creditCount++; creditSum += amt; }
            else if (dir < 0) { debitCount++;  debitSum  += amt; }
        }

        // ── Stat cards: real money + real count ──
        animateBalance(totalCreditLabel, creditSum);
        animateBalance(totalDebitLabel, debitSum);
        if (txCountLabel != null) animateCount(txCountLabel, transactions.size());

        // ── Pie: real credit vs transfer counts (no fake fallback) ──
        if (transactionPieChart != null) {
            ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
            if (creditCount == 0 && debitCount == 0) {
                pie.add(new PieChart.Data("No transactions yet", 1));
            } else {
                pie.add(new PieChart.Data("Credits (" + creditCount + ")", creditCount));
                pie.add(new PieChart.Data("Transfers (" + debitCount + ")", debitCount));
            }
            transactionPieChart.setData(pie);
        }

        // ── Area: real running balance reconstructed from the transactions ──
        if (balanceTrendChart != null) {
            balanceTrendChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();

            List<Transaction> chrono = new ArrayList<>(transactions);
            Collections.reverse(chrono);   // backend returns newest-first

            if (chrono.isEmpty()) {
                series.getData().add(new XYChart.Data<>("Start", currentBalance));
                series.getData().add(new XYChart.Data<>("Now",   currentBalance));
            } else {
                // current balance = balance after the newest tx; walk back to the start.
                double net = 0;
                for (Transaction tx : chrono) net += direction(tx, myAccount) * parseAmount(tx.getAmount());
                double running = currentBalance - net;   // balance before the first tx

                List<XYChart.Data<String, Number>> points = new ArrayList<>();
                for (Transaction tx : chrono) {
                    running += direction(tx, myAccount) * parseAmount(tx.getAmount());
                    points.add(new XYChart.Data<>(shortDate(tx), Math.round(running * 100.0) / 100.0));
                }
                // show at most the last 12 points to keep the axis readable
                int from = Math.max(0, points.size() - 12);
                for (int i = from; i < points.size(); i++) series.getData().add(points.get(i));
            }
            balanceTrendChart.getData().add(series);
        }
    }

    /** +1 if money came INTO this account, -1 if it left, 0 if unrelated/unknown. */
    private int direction(Transaction tx, String myAccount) {
        if (myAccount == null || myAccount.isBlank()) return 0;
        if (myAccount.equals(tx.getDestinationAccountId())) return 1;
        if (myAccount.equals(tx.getSourceAccountId()))      return -1;
        return 0;
    }

    private String classifyType(Transaction tx) {
        int dir = direction(tx, sessionManager.getSavedAccountId());
        if (dir > 0) return "Credit";
        if (dir < 0) return "Debit";
        return "—";
    }

    private String signedAmount(Transaction tx) {
        double amt = parseAmount(tx.getAmount());
        int dir = direction(tx, sessionManager.getSavedAccountId());
        String sign = dir > 0 ? "+ $" : dir < 0 ? "- $" : "$";
        return sign + String.format("%,.2f", amt);
    }

    /** Compact label like "15 Jan" from the formatted transaction date. */
    private String shortDate(Transaction tx) {
        String d = tx.getDate();
        if (d == null || d.isBlank()) return "";
        String[] p = d.trim().split("\\s+");
        if (p.length >= 2) {
            String mon = p[1].length() >= 3 ? p[1].substring(0, 3) : p[1];
            mon = Character.toUpperCase(mon.charAt(0)) + mon.substring(1);
            return p[0] + " " + mon;
        }
        return p[0];
    }

    /** Last 4 alphanumerics of the account id, for the masked display card. */
    private String last4(String account) {
        if (account == null) return "0000";
        String clean = account.replaceAll("[^A-Za-z0-9]", "");
        if (clean.isEmpty()) return "0000";
        String tail = clean.length() >= 4 ? clean.substring(clean.length() - 4) : clean;
        return tail.toUpperCase();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TABLE CONFIGURATION
    // ═══════════════════════════════════════════════════════════════════════
    private void configureTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(cd -> new SimpleStringProperty(classifyType(cd.getValue())));
        amountColumn.setCellValueFactory(cd -> new SimpleStringProperty(signedAmount(cd.getValue())));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
    }

    private void configureCreditRequestTable() {
        requestIdColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        requestAccountIdColumn.setCellValueFactory(new PropertyValueFactory<>("accountId"));
        requestAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DATA LOADING
    // ═══════════════════════════════════════════════════════════════════════
    private void loadUserData(String accountFilter) {
        String userId = sessionManager.getSavedUserId();
        if (userId == null) return;

        try {
            double balanceVal = apiService.getUserBalanceValue(userId);
            animateBalance(balanceLabel, balanceVal);

            // Tie the display card + chip to the real account.
            String account = sessionManager.getSavedAccountId();
            if (cardNumberLabel != null) {
                cardNumberLabel.setText("••••  ••••  ••••  " + last4(account));
            }
            if (heroAcctLabel != null) {
                heroAcctLabel.setText("••••  ••••  ••••  " + last4(account));
            }
            if (accountChip != null) {
                accountChip.setText(account != null ? "Acct " + account : "No account");
            }

            String filterAccountId = accountFilter != null && !accountFilter.isBlank()
                    ? accountFilter : sessionManager.getSavedAccountId();
            List<Transaction> transactions = apiService.getUserTransactions(userId, filterAccountId);
            transactionTable.setItems(FXCollections.observableArrayList(transactions));
            populateCharts(transactions, balanceVal);

        } catch (Exception ex) {
            ex.printStackTrace();
            balanceLabel.setText("unavailable");
            showDialog(Alert.AlertType.ERROR, "Load Error", "Unable to load data: " + ex.getMessage());
        }
    }

    private void loadUserCreditRequests() {
        String userId = sessionManager.getSavedUserId();
        if (userId == null) return;
        try {
            List<CreditRequest> requests = apiService.getUserCreditRequests(userId);
            creditRequestsTable.setItems(FXCollections.observableArrayList(requests));
        } catch (Exception ex) {
            ex.printStackTrace();
            showDialog(Alert.AlertType.ERROR, "Load Error", "Unable to load credit requests: " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BUTTON HANDLERS
    // ═══════════════════════════════════════════════════════════════════════
    @FXML
    private void handleFilterTransactions(ActionEvent event) {
        String filter = accountFilterField.getText();
        loadUserData(filter != null && !filter.isBlank() ? filter.trim() : null);
    }

    @FXML
    private void handleClearFilter(ActionEvent event) {
        accountFilterField.clear();
        loadUserData(null);
    }

    @FXML
    private void handleTransfer(ActionEvent event) {
        if (transferStatusLabel != null) transferStatusLabel.setText("");
        String destination = destinationAccountField.getText();
        String amount      = amountField.getText();
        String userId      = sessionManager.getSavedUserId();
        String accountId   = sessionManager.getSavedAccountId();

        if (userId == null || userId.isBlank()) {
            showDialog(Alert.AlertType.ERROR, "Transfer Error", "No user session. Please log in again.");
            return;
        }
        if (destination == null || destination.isBlank() || amount == null || amount.isBlank()) {
            showDialog(Alert.AlertType.ERROR, "Transfer Error", "Destination account and amount are required.");
            return;
        }
        if (!isValidAmount(amount)) {
            showDialog(Alert.AlertType.ERROR, "Transfer Error", "Enter a valid transfer amount.");
            return;
        }
        try {
            double balanceVal   = apiService.getUserBalanceValue(userId);
            double transferVal  = parseAmount(amount.trim());
            if (transferVal > balanceVal) {
                showDialog(Alert.AlertType.ERROR, "Transfer Error", "Insufficient balance.");
                return;
            }
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Transfer Error", "Unable to verify balance: " + ex.getMessage());
            return;
        }

        setLoading(true);
        try {
            Map<String, String> payload = Map.of(
                "userId",               userId,
                "destinationAccountId", destination.trim(),
                "amount",               amount.trim(),
                "sourceAccountId",      accountId != null ? accountId : ""
            );
            String response = apiService.transferMoney(payload);
            showDialog(Alert.AlertType.INFORMATION, "Transfer Completed", "Transfer submitted: " + response);
            destinationAccountField.clear();
            amountField.clear();
            loadUserData(null);
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Transfer Failed", "Transfer failed: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void handleRequestCredit(ActionEvent event) {
        if (transferStatusLabel != null) transferStatusLabel.setText("");
        String amount    = creditAmountField.getText();
        String username  = sessionManager.getSavedUsername();
        String accountId = sessionManager.getSavedAccountId();

        if (username == null || username.isBlank()) {
            showDialog(Alert.AlertType.ERROR, "Credit Request", "No user session. Please log in again.");
            return;
        }
        if (amount == null || amount.isBlank() || !isValidAmount(amount)) {
            showDialog(Alert.AlertType.ERROR, "Credit Request", "Enter a valid credit amount.");
            return;
        }

        setLoading(true);
        try {
            Map<String, String> payload = Map.of(
                "username",  username.trim(),
                "accountId", accountId != null ? accountId : "",
                "amount",    amount.trim()
            );
            String response = apiService.requestCredit(payload);
            showDialog(Alert.AlertType.INFORMATION, "Credit Request", "Request submitted: " + response);
            creditAmountField.clear();
            loadUserData(null);
            loadUserCreditRequests();
        } catch (Exception ex) {
            showDialog(Alert.AlertType.ERROR, "Credit Request", "Request failed: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            setLoading(false);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        sessionManager.clearSession();
        try { MainApp.showLogin(); } catch (Exception ex) { ex.printStackTrace(); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private void setLoading(boolean loading) {
        if (transferButton       != null) transferButton.setDisable(loading);
        if (requestCreditButton  != null) requestCreditButton.setDisable(loading);
        if (filterTransactionsButton != null) filterTransactionsButton.setDisable(loading);
        if (clearFilterButton    != null) clearFilterButton.setDisable(loading);
        if (logoutButton         != null) logoutButton.setDisable(loading);
    }

    private boolean isValidAmount(String value) {
        if (value == null || value.isBlank()) return false;
        try { return Double.parseDouble(value.trim()) > 0; }   // must be a positive amount
        catch (NumberFormatException ex) { return false; }
    }

    private double parseAmount(String value) {
        if (value == null) return 0.0;
        String n = value.replaceAll("[^0-9.\\-]", "");
        if (n.isBlank()) return 0.0;
        try { return Double.parseDouble(n); } catch (NumberFormatException ex) { return 0.0; }
    }

    private void showDialog(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
