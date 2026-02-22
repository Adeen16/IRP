package banking.ui.panels;

import banking.model.User;
import banking.service.AuthService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * User security settings panel for password updates.
 */
public class UserSecurityPanel extends JPanel implements Refreshable {
    private final User currentUser;
    private final AuthService authService;

    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    public UserSecurityPanel(User user, AuthService service) {
        this.currentUser = user;
        this.authService = service;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Security Settings");
        title.setFont(UIStyle.TITLE_FONT);
        title.setForeground(UIStyle.TEXT_COLOR);
        add(title, BorderLayout.NORTH);

        ModernUIComponents.RoundedPanel card = new ModernUIComponents.RoundedPanel(20, Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        card.add(new JLabel("Change Password"), gbc);
        ((JLabel)card.getComponent(0)).setFont(UIStyle.HEADER_FONT);

        gbc.gridy++;
        card.add(new JLabel("Current Password"), gbc);
        gbc.gridy++;
        txtOldPassword = new JPasswordField();
        UIStyle.stylePasswordField(txtOldPassword);
        card.add(txtOldPassword, gbc);

        gbc.gridy++;
        card.add(new JLabel("New Password (min 6 chars)"), gbc);
        gbc.gridy++;
        txtNewPassword = new JPasswordField();
        UIStyle.stylePasswordField(txtNewPassword);
        card.add(txtNewPassword, gbc);

        gbc.gridy++;
        card.add(new JLabel("Confirm New Password"), gbc);
        gbc.gridy++;
        txtConfirmPassword = new JPasswordField();
        UIStyle.stylePasswordField(txtConfirmPassword);
        card.add(txtConfirmPassword, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 0, 0);
        JButton btnUpdate = new JButton("Update Password");
        UIStyle.stylePrimaryButton(btnUpdate);
        btnUpdate.addActionListener(e -> handlePasswordChange());
        card.add(btnUpdate, gbc);

        // Center the card
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        centerPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        card.setPreferredSize(new Dimension(500, 450));
        centerPanel.add(card);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void handlePasswordChange() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirm = new String(txtConfirmPassword.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            UIStyle.showError(this, "All fields are required.");
            return;
        }

        if (!newPass.equals(confirm)) {
            UIStyle.showError(this, "New passwords do not match.");
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.changePassword(currentUser.getUserId(), oldPass, newPass);
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        UIStyle.showSuccess(UserSecurityPanel.this, "Password updated successfully!");
                        txtOldPassword.setText("");
                        txtNewPassword.setText("");
                        txtConfirmPassword.setText("");
                    }
                } catch (Exception e) {
                    UIStyle.showError(UserSecurityPanel.this, e.getCause().getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void onActivated() {
        // Clear fields when tab is switched
        txtOldPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }
}
