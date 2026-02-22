package banking.ui.user;

import banking.model.User;
import banking.service.AuthService;
import banking.service.BankingService;
import banking.ui.LoginForm;
import banking.ui.UIStyle;
import banking.ui.panels.UserOverviewPanel;
import banking.ui.panels.UserSecurityPanel;
import banking.ui.panels.UserStatementPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized customer portal with CardLayout-based navigation.
 */
public class UserDashboard extends JFrame {
    private final User currentUser;
    private final BankingService bankingService;
    private final AuthService authService;
    
    private CardLayout cardLayout;
    private JPanel contentArea;
    private List<JButton> navButtons = new ArrayList<>();

    // Panels
    private UserOverviewPanel overviewPanel;
    private UserStatementPanel statementPanel;
    private UserSecurityPanel securityPanel;

    public UserDashboard(User user) {
        this.currentUser = user;
        this.bankingService = new BankingService();
        this.authService = new AuthService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Secure Bank - Customer Portal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIStyle.PRIMARY_COLOR);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel lblBrand = new JLabel("SECURE BANK");
        lblBrand.setFont(UIStyle.TITLE_FONT);
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblBrand);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel lblRole = new JLabel("Customer Portal");
        lblRole.setFont(UIStyle.SMALL_FONT);
        lblRole.setForeground(new Color(148, 163, 184));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblRole);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Nav Buttons
        JButton btnDash = addNavButton(sidebar, "\u2302  Dashboard Overview", "dashboard");
        JButton btnStmt = addNavButton(sidebar, "\u2630  Account Statement",  "statement");
        JButton btnSec  = addNavButton(sidebar, "\u2699  Security Settings",  "security");

        sidebar.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("Logout");
        UIStyle.styleDangerButton(btnLogout);
        btnLogout.setMaximumSize(new Dimension(240, 45));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        sidebar.add(btnLogout);

        // --- Header ---
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setPreferredSize(new Dimension(0, 80));
        topHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel lblHeader = new JLabel("Logged in as: " + currentUser.getUsername());
        lblHeader.setFont(UIStyle.HEADER_FONT);
        lblHeader.setForeground(UIStyle.TEXT_COLOR);
        lblHeader.setBorder(new EmptyBorder(0, 30, 0, 0));
        topHeader.add(lblHeader, BorderLayout.WEST);

        // --- Content Area ---
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIStyle.BACKGROUND_COLOR);

        overviewPanel  = new UserOverviewPanel(currentUser, bankingService, this);
        statementPanel = new UserStatementPanel(currentUser, bankingService);
        securityPanel  = new UserSecurityPanel(currentUser, authService);

        contentArea.add(overviewPanel,  "dashboard");
        contentArea.add(statementPanel, "statement");
        contentArea.add(securityPanel,  "security");

        // Assembly
        JPanel mainLayout = new JPanel(new BorderLayout());
        mainLayout.add(topHeader, BorderLayout.NORTH);
        mainLayout.add(contentArea, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainLayout, BorderLayout.CENTER);

        // Initial state
        setActiveButton(btnDash);
        overviewPanel.onActivated();
    }

    private JButton addNavButton(JPanel sidebar, String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.BUTTON_FONT);
        btn.setForeground(new Color(148, 163, 184));
        btn.setBackground(UIStyle.PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(260, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));

        btn.addActionListener(e -> {
            setActiveButton(btn);
            switchPanel(panelName);
        });

        navButtons.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        return btn;
    }

    private void setActiveButton(JButton selected) {
        for (JButton btn : navButtons) {
            btn.setBackground(UIStyle.PRIMARY_COLOR);
            btn.setForeground(new Color(148, 163, 184));
        }
        selected.setBackground(new Color(51, 65, 85));
        selected.setForeground(Color.WHITE);
    }

    private void switchPanel(String name) {
        cardLayout.show(contentArea, name);
        switch (name) {
            case "dashboard" -> overviewPanel.onActivated();
            case "statement" -> statementPanel.onActivated();
            case "security"  -> securityPanel.onActivated();
        }
    }
}
