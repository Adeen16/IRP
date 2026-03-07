package banking.ui;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public final class ThemeManager {
    private static final String THEME = "banking/resources/css/luxury-theme.css";
    private static final String FALLBACK_THEME = "banking/resources/css/global.css";

    private ThemeManager() {
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }

        URL themeUrl = ThemeManager.class.getClassLoader().getResource(THEME);
        if (themeUrl == null) {
            themeUrl = ThemeManager.class.getClassLoader().getResource(FALLBACK_THEME);
        }
        if (themeUrl == null) {
            System.out.println("[WARN] Theme CSS not found");
            return;
        }

        String stylesheet = themeUrl.toExternalForm();
        if (!scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
        }
    }

    public static void apply(Stage stage) {
        if (stage != null) {
            apply(stage.getScene());
        }
    }
}
