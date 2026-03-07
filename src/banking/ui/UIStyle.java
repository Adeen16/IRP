package banking.ui;

import banking.ui.components.ModernUIComponents;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

public class UIStyle {
    public static final Color BACKGROUND_COLOR = new Color(11, 11, 15);
    public static final Color CARD_COLOR = new Color(20, 20, 24);
    public static final Color CARD_GRADIENT_TOP = new Color(17, 17, 17);
    public static final Color CARD_GRADIENT_BOTTOM = new Color(10, 10, 10);
    public static final Color PRIMARY_COLOR = new Color(9, 9, 11);
    public static final Color SECONDARY_COLOR = new Color(24, 24, 27);
    public static final Color ACCENT_COLOR = new Color(201, 162, 39);
    public static final Color ACCENT_HOVER = new Color(229, 199, 107);
    public static final Color GOLD_GLOW_COLOR = new Color(201, 162, 39);
    public static final Color CARD_GOLD_BORDER = new Color(201, 162, 39, 64);
    public static final Color CARD_INNER_SHADOW = new Color(0, 0, 0, 153);
    public static final Color TEXT_COLOR = new Color(229, 231, 235);
    public static final Color TEXT_LIGHT = new Color(156, 163, 175);
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);
    public static final Color DANGER_COLOR = new Color(239, 68, 68);
    public static final Color WARNING_COLOR = new Color(229, 199, 107);
    public static final Color BORDER_COLOR = new Color(255, 255, 255, 13);
    public static final Color ROW_HOVER_COLOR = new Color(28, 28, 34);

    public static final Font TITLE_FONT = new Font("Georgia", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("Georgia", Font.BOLD, 20);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    static {
        initializeThemeDefaults();
    }

    private static void initializeThemeDefaults() {
        UIManager.put("Panel.background", new ColorUIResource(BACKGROUND_COLOR));
        UIManager.put("Viewport.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("ScrollPane.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("Label.foreground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("OptionPane.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("OptionPane.messageFont", new FontUIResource(LABEL_FONT));
        UIManager.put("Button.background", new ColorUIResource(ACCENT_COLOR));
        UIManager.put("Button.foreground", new ColorUIResource(BACKGROUND_COLOR));
        UIManager.put("Button.font", new FontUIResource(BUTTON_FONT));
        UIManager.put("TextField.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("TextField.foreground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("TextField.caretForeground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("PasswordField.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("PasswordField.foreground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("PasswordField.caretForeground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("ComboBox.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("ComboBox.foreground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("Table.background", new ColorUIResource(CARD_COLOR));
        UIManager.put("Table.foreground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("Table.selectionBackground", new ColorUIResource(new Color(201, 162, 39, 56)));
        UIManager.put("Table.selectionForeground", new ColorUIResource(TEXT_COLOR));
        UIManager.put("Table.gridColor", new ColorUIResource(BORDER_COLOR));
        UIManager.put("TableHeader.background", new ColorUIResource(SECONDARY_COLOR));
        UIManager.put("TableHeader.foreground", new ColorUIResource(TEXT_COLOR));
    }

    public static void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(isGoldTone(bgColor) ? BACKGROUND_COLOR : TEXT_COLOR);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bgColor, 1, true),
            new EmptyBorder(10, 25, 10, 25)
        ));
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button, ACCENT_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(229, 199, 107, 90), 1, true),
            new EmptyBorder(10, 25, 10, 25)
        ));
    }

    public static void styleSuccessButton(JButton button) {
        styleButton(button, SUCCESS_COLOR);
        button.setForeground(Color.WHITE);
    }

    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER_COLOR);
        button.setForeground(Color.WHITE);
    }

    public static void styleSecondaryButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setForeground(ACCENT_COLOR);
        button.setBackground(CARD_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ACCENT_COLOR, 1, true),
            new EmptyBorder(10, 25, 10, 25)
        ));
    }

    public static void styleTextField(JTextField field) {
        field.setFont(LABEL_FONT);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(CARD_COLOR);
    }

    public static void stylePasswordField(JPasswordField field) {
        field.setFont(LABEL_FONT);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(CARD_COLOR);
    }

    public static void styleLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
    }

    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setBackground(CARD_COLOR);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
    }

    public static void styleTable(JTable table) {
        table.setFont(SMALL_FONT);
        table.setRowHeight(34);
        table.setBackground(CARD_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setSelectionBackground(new Color(201, 162, 39, 56));
        table.setSelectionForeground(TEXT_COLOR);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(SECONDARY_COLOR);
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_COLOR));
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.getViewport().setBackground(CARD_COLOR);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 6, 0, new Color(0, 0, 0, 70)),
            BorderFactory.createLineBorder(CARD_GOLD_BORDER)
        ));
    }

    public static void styleTextArea(JTextArea area) {
        area.setFont(LABEL_FONT);
        area.setForeground(TEXT_COLOR);
        area.setCaretColor(TEXT_COLOR);
        area.setBackground(CARD_COLOR);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(12, 12, 12, 12)
        ));
    }

    public static void styleDialog(JDialog dialog) {
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
    }

    public static JPanel createCard() {
        JPanel card = new ModernUIComponents.RoundedPanel(20, CARD_COLOR);
        card.setBackground(CARD_COLOR);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setOpaque(false);
        return card;
    }

    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SMALL_FONT);
        titleLabel.setForeground(TEXT_LIGHT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Georgia", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);

        JPanel leftBar = new JPanel();
        leftBar.setPreferredSize(new Dimension(4, 60));
        leftBar.setBackground(accentColor);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CARD_COLOR);
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);

        card.add(leftBar, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    public static void showMessage(Component parent, String message, String title, int type) {
        JOptionPane.showMessageDialog(parent, message, title, type);
    }

    public static void showSuccess(Component parent, String message) {
        showMessage(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        showMessage(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        showMessage(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static boolean isGoldTone(Color color) {
        return color.equals(ACCENT_COLOR) || color.equals(ACCENT_HOVER) || color.equals(WARNING_COLOR);
    }
}
