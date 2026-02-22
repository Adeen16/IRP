package banking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UIStyle {
    public static final Color PRIMARY_COLOR = new Color(30, 41, 59);    // Slate 800
    public static final Color SECONDARY_COLOR = new Color(51, 65, 85);  // Slate 700
    public static final Color ACCENT_COLOR = new Color(59, 130, 246);   // Blue 500
    public static final Color DANGER_COLOR = new Color(239, 68, 68);    // Red 500
    public static final Color WARNING_COLOR = new Color(245, 158, 11);  // Amber 500
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);  // Emerald 500
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252); // Slate 50
    public static final Color CARD_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(15, 23, 42);       // Slate 900
    public static final Color TEXT_LIGHT = new Color(100, 116, 139);    // Slate 500
    
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    
    public static void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(10, 25, 10, 25));
    }
    
    public static void stylePrimaryButton(JButton button) {
        styleButton(button, PRIMARY_COLOR);
    }
    
    public static void styleSuccessButton(JButton button) {
        styleButton(button, ACCENT_COLOR);
    }
    
    public static void styleDangerButton(JButton button) {
        styleButton(button, DANGER_COLOR);
    }
    
    public static void styleTextField(JTextField field) {
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
    }
    
    public static void stylePasswordField(JPasswordField field) {
        field.setFont(LABEL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(Color.WHITE);
    }
    
    public static void styleLabel(JLabel label) {
        label.setFont(LABEL_FONT);
        label.setForeground(TEXT_COLOR);
    }
    
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(LABEL_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
    }
    
    public static void styleTable(JTable table) {
        table.setFont(SMALL_FONT);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(SECONDARY_COLOR);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(PRIMARY_COLOR.brighter());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(new Color(189, 195, 199));
    }
    
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        return card;
    }
    
    public static JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(10, 10));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SMALL_FONT);
        titleLabel.setForeground(TEXT_LIGHT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
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
}
