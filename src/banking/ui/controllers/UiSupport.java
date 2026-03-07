package banking.ui.controllers;

import banking.util.Validator;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class UiSupport {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private UiSupport() {
    }

    public static <T> void runAsync(String threadName, Callable<T> action,
                                    Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };

        task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
        task.setOnFailed(event -> onError.accept(task.getException()));

        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    public static String formatCurrency(BigDecimal amount) {
        return Validator.formatCurrency(amount == null ? BigDecimal.ZERO : amount);
    }

    public static String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return timestamp.toLocalDateTime().format(DATE_TIME_FORMAT);
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) {
            return "-";
        }
        return timestamp.toLocalDateTime().toLocalDate().format(DATE_FORMAT);
    }

    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }

    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }

    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            styleDialog(alert);
            alert.showAndWait();
        });
    }

    private static void styleDialog(Alert alert) {
        DialogStyler.apply(alert);
    }

    public static void markActive(Button activeButton, Button... buttons) {
        for (Button button : buttons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    public static HBox metricCard(String title, String value, String accentStyleClass, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");

        valueLabel.setText(value);
        valueLabel.getStyleClass().addAll("metric-value", accentStyleClass);

        VBoxLike box = new VBoxLike(titleLabel, valueLabel);
        box.getStyleClass().add("metric-card");
        HBox wrapper = new HBox(box);
        wrapper.setFillHeight(true);
        HBox.setHgrow(box, Priority.ALWAYS);
        return wrapper;
    }

    public static HBox sectionHeader(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("muted-text");

        VBoxLike left = new VBoxLike(titleLabel, subtitleLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(left, spacer);
        header.getStyleClass().add("section-header");
        return header;
    }

    private static final class DialogStyler {
        private static void apply(Alert alert) {
            alert.getDialogPane().getStylesheets().add(
                UiSupport.class.getResource("/banking/resources/styles/theme.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("dialog-root");
        }
    }

    private static final class VBoxLike extends javafx.scene.layout.VBox {
        private VBoxLike(javafx.scene.Node... children) {
            super(6, children);
        }
    }
}
