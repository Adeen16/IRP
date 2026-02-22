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
        setTitle("Secure Banking System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 550);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Container
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        setContentPane(mainPanel);

        // Header (Slate background)
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(UIStyle.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(450, 180));
        
        JLabel lblTitle = new JLabel("SECURE BANK");
        lblTitle.setFont(UIStyle.TITLE_FONT);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Login Card (Rounded)
        banking.ui.components.ModernUIComponents.RoundedPanel cardPanel = 
            new banking.ui.components.ModernUIComponents.RoundedPanel(20, Color.WHITE);
        cardPanel.setLayout(new GridBagLayout());
        cardPanel.setPreferredSize(new Dimension(380, 320));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 20, 8, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Welcome Text
        JLabel lblWelcome = new JLabel("Sign In", SwingConstants.CENTER);
        lblWelcome.setFont(UIStyle.HEADER_FONT);
        lblWelcome.setForeground(UIStyle.TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 0, 20, 0);
        cardPanel.add(lblWelcome, gbc);

        // Username
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 2, 5);
        gbc.gridy = 1;
        JLabel lblUser = new JLabel("Username");
        UIStyle.styleLabel(lblUser);
        cardPanel.add(lblUser, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 10, 5);
        txtUsername = new JTextField(20);
        UIStyle.styleTextField(txtUsername);
        cardPanel.add(txtUsername, gbc);

        // Password
        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 2, 5);
        JLabel lblPass = new JLabel("Password");
        UIStyle.styleLabel(lblPass);
        cardPanel.add(lblPass, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 5, 10, 5);
        txtPassword = new JPasswordField(20);
        UIStyle.stylePasswordField(txtPassword);
        cardPanel.add(txtPassword, gbc);

        // Login Button
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 5, 10, 5);
        btnLogin = new JButton("LOGIN");
        UIStyle.stylePrimaryButton(btnLogin);
        cardPanel.add(btnLogin, gbc);

        // Overlay the card on the header/body boundary
        JPanel centerWrapper = new JPanel(null); // Absolute layout for custom positioning
        centerWrapper.setBackground(UIStyle.BACKGROUND_COLOR);
        
        cardPanel.setBounds(35, -50, 380, 320); // Position card to overlap the header
        centerWrapper.add(cardPanel);
        
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        // Footer
        JLabel lblFooter = new JLabel("© 2026 Secure Banking Corp", SwingConstants.CENTER);
        lblFooter.setFont(UIStyle.SMALL_FONT);
        lblFooter.setForeground(UIStyle.TEXT_LIGHT);
        lblFooter.setBorder(new EmptyBorder(10, 10, 20, 10));
        mainPanel.add(lblFooter, BorderLayout.SOUTH);

        // Action Listener
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Enter key support
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
        btnLogin.setText("AUTHENTICATING...");

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
                    btnLogin.setText("LOGIN");
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
