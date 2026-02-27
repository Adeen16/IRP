package banking.ui;

import banking.model.User;
import banking.service.AuthService;
import banking.ui.admin.AdminDashboard;
import banking.ui.user.UserDashboard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private AuthService authService;

    public LoginForm() {
        this.authService = new AuthService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Secure Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600); // Expanded size for the gorgeous split-pane
        setLocationRelativeTo(null);
        setResizable(false);

        // Split panel layout (50/50)
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
        setContentPane(mainPanel);

        // --- Left Panel (Branding) ---
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(15, 23, 42)); // Deep Navy Blue (Slate 900)
        
        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.insets = new Insets(10, 10, 5, 10);
        
        // Logo / Icon (Text based modern emoji for 0 dependency)
        JLabel lblLogo = new JLabel("🏦");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        lblLogo.setForeground(Color.WHITE);
        leftPanel.add(lblLogo, gbcLeft);

        gbcLeft.gridy++;
        JLabel lblTitle = new JLabel("SECURE BANK");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 38));
        lblTitle.setForeground(Color.WHITE);
        leftPanel.add(lblTitle, gbcLeft);
        
        gbcLeft.gridy++;
        gbcLeft.insets = new Insets(5, 10, 10, 10);
        JLabel lblTagline = new JLabel("Enterprise Grade Financial Security");
        lblTagline.setFont(new Font("Inter", Font.PLAIN, 16));
        lblTagline.setForeground(new Color(148, 163, 184)); // Slate 400
        leftPanel.add(lblTagline, gbcLeft);

        mainPanel.add(leftPanel);

        // --- Right Panel (Form) ---
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 0;
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.insets = new Insets(10, 50, 10, 50);
        gbcRight.weightx = 1.0;
        
        // Welcome Text
        gbcRight.gridy = 0;
        gbcRight.insets = new Insets(10, 50, 40, 50);
        JLabel lblWelcome = new JLabel("Welcome Back", SwingConstants.LEFT);
        lblWelcome.setFont(new Font("Inter", Font.BOLD, 32));
        lblWelcome.setForeground(new Color(30, 41, 59)); // Slate 800
        rightPanel.add(lblWelcome, gbcRight);

        // Username Label
        gbcRight.gridy++;
        gbcRight.insets = new Insets(5, 50, 5, 50);
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Inter", Font.BOLD, 13));
        lblUser.setForeground(new Color(100, 116, 139)); // Slate 500
        rightPanel.add(lblUser, gbcRight);

        // Username Input
        gbcRight.gridy++;
        gbcRight.insets = new Insets(0, 50, 20, 50);
        txtUsername = new JTextField(20);
        txtUsername.setPreferredSize(new Dimension(300, 45));
        txtUsername.setFont(new Font("Inter", Font.PLAIN, 16));
        // Soft rounded gray border
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        rightPanel.add(txtUsername, gbcRight);

        // Password Label
        gbcRight.gridy++;
        gbcRight.insets = new Insets(5, 50, 5, 50);
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Inter", Font.BOLD, 13));
        lblPass.setForeground(new Color(100, 116, 139));
        rightPanel.add(lblPass, gbcRight);

        // Password Input
        gbcRight.gridy++;
        gbcRight.insets = new Insets(0, 50, 35, 50);
        txtPassword = new JPasswordField(20);
        txtPassword.setPreferredSize(new Dimension(300, 45));
        txtPassword.setFont(new Font("Inter", Font.PLAIN, 16));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        rightPanel.add(txtPassword, gbcRight);

        // Login Button
        gbcRight.gridy++;
        gbcRight.insets = new Insets(10, 50, 10, 50);
        btnLogin = new JButton("Sign In");
        btnLogin.setPreferredSize(new Dimension(300, 50));
        btnLogin.setFont(new Font("Inter", Font.BOLD, 16));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBackground(new Color(37, 99, 235)); // Sleek Blue 600
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(new EmptyBorder(10, 10, 10, 10));
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(btnLogin, gbcRight);

        mainPanel.add(rightPanel);

        // Action Listeners
        btnLogin.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());
        txtUsername.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIStyle.showError(this, "Please enter both username and password.");
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Authenticating...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    if (user != null) {
                        dispose();
                        if (user.isAdmin()) {
                            new AdminDashboard(user).setVisible(true);
                        } else {
                            new UserDashboard(user).setVisible(true);
                        }
                    }
                } catch (Exception ex) {
                    UIStyle.showError(LoginForm.this, "Authentication failed: " + ex.getCause().getMessage());
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                }
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
