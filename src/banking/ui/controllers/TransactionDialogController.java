package banking.ui.controllers;

import banking.model.User;
import banking.service.BankingService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;

public class TransactionDialogController {
    @FXML private Label lblDialogTitle;
    @FXML private Label lblAccountNumber;
    @FXML private VBox targetAccountBox;
    @FXML private TextField txtTargetAccount;
    @FXML private TextField txtAmount;
    @FXML private VBox pinBox;
    @FXML private PasswordField txtPin;
    @FXML private Label lblMessage;
    @FXML private Button btnSubmit;

    private final BankingService bankingService = new BankingService();
    private Stage dialogStage;
    private User currentUser;
    private String sourceAccount;
    private String type;
    private Runnable successCallback;

    public void configure(Stage stage, User user, String accountNumber, String transactionType, Runnable callback) {
        this.dialogStage = stage;
        this.currentUser = user;
        this.sourceAccount = accountNumber;
        this.type = transactionType;
        this.successCallback = callback;

        boolean needsPin = "WITHDRAW".equals(transactionType) || "TRANSFER".equals(transactionType);
        targetAccountBox.setManaged("TRANSFER".equals(transactionType));
        targetAccountBox.setVisible("TRANSFER".equals(transactionType));
        pinBox.setManaged(needsPin);
        pinBox.setVisible(needsPin);

        lblDialogTitle.setText(transactionType + " Funds");
        lblAccountNumber.setText(accountNumber);
        btnSubmit.setText(transactionType);
    }

    @FXML
    private void handleSubmit() {
        BigDecimal amount;
        try {
            amount = new BigDecimal(txtAmount.getText().trim());
        } catch (Exception exception) {
            setMessage("Enter a valid numeric amount.", true);
            return;
        }

        String targetAccount = txtTargetAccount.getText().trim();
        String pin = txtPin.getText().trim();

        if ("TRANSFER".equals(type) && targetAccount.isEmpty()) {
            setMessage("Recipient account is required.", true);
            return;
        }
        if (("WITHDRAW".equals(type) || "TRANSFER".equals(type)) && pin.isEmpty()) {
            setMessage("Transaction PIN is required.", true);
            return;
        }

        btnSubmit.setDisable(true);
        setMessage("Processing " + type.toLowerCase() + "...", false);

        UiSupport.runAsync("transaction-dialog", () -> {
            int userId = currentUser == null ? 0 : currentUser.getUserId();
            if ("DEPOSIT".equals(type)) {
                bankingService.deposit(sourceAccount, amount, userId);
            } else if ("WITHDRAW".equals(type)) {
                bankingService.withdraw(sourceAccount, amount, pin, userId);
            } else {
                bankingService.transfer(sourceAccount, targetAccount, amount, pin, userId);
            }
            return null;
        }, ignored -> {
            if (successCallback != null) {
                successCallback.run();
            }
            dialogStage.close();
            UiSupport.showInfo("Transaction Complete", type + " completed successfully.");
        }, error -> {
            btnSubmit.setDisable(false);
            setMessage(error.getMessage(), true);
        });
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void setMessage(String message, boolean error) {
        lblMessage.setText(message == null ? "" : message);
        lblMessage.getStyleClass().removeAll("error-text", "success-text");
        lblMessage.getStyleClass().add(error ? "error-text" : "success-text");
    }
}
