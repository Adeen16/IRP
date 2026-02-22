package banking.ui.admin;

import banking.model.User;
import banking.service.AnalyticsService;
import banking.service.AuthService;
import banking.service.BankingService;
import banking.ui.LoginForm;
import banking.ui.UIStyle;
import banking.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main admin dashboard frame with CardLayout-based panel switching.
 * Each sidebar button loads a different module panel.
 */
public class AdminDashboard extends JFrame {
    private User adminUser;
    private CardLayout cardLayout;
    private JPanel contentArea;
    private JButton activeButton;
    private List<JButton> navButtons = new ArrayList<>();

    // Services (shared across panels)
    private BankingService bankingService;
    private AuthService authService;
    private AnalyticsService analyticsService;

    // Panels
    private OverviewPanel overviewPanel;
    private CustomerPanel customerPanel;
    private AccountPanel accountPanel;
    private TransactionPanel transactionPanel;
    private UserPanel userPanel;

    public AdminDashboard(User user) {
        this.adminUser = user;
        this.bankingService = new BankingService();
        this.authService = new AuthService();
        this.analyticsService = new AnalyticsService();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Secure Bank - Management Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 800));

        // ─── Sidebar ─────────────────────────────────────────────
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

        JLabel lblRole = new JLabel("Admin Dashboard");
        lblRole.setFont(UIStyle.SMALL_FONT);
        lblRole.setForeground(new Color(148, 163, 184));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblRole);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Navigation buttons
        JButton btnOverview     = addNavButton(sidebar, "[D] Dashboard",      "overview");
        JButton btnCustomers    = addNavButton(sidebar, "[C] Customers",        "customers");
        JButton btnAccounts     = addNavButton(sidebar, "[A] Accounts",         "accounts");
        JButton btnTransactions = addNavButton(sidebar, "[T] Transactions",     "transactions");
        JButton btnUsers        = addNavButton(sidebar, "[S] System Users",     "users");

        sidebar.add(Box.createVerticalGlue());

        // Logout button
        JButton btnLogout = new JButton("Logout");
        UIStyle.styleDangerButton(btnLogout);
        btnLogout.setMaximumSize(new Dimension(240, 45));
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        sidebar.add(btnLogout);

        // ─── Top Header ──────────────────────────────────────────
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setPreferredSize(new Dimension(0, 80));
        topHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel lblHeader = new JLabel("Welcome back, " + adminUser.getUsername());
        lblHeader.setFont(UIStyle.HEADER_FONT);
        lblHeader.setForeground(UIStyle.TEXT_COLOR);
        lblHeader.setBorder(new EmptyBorder(0, 30, 0, 0));
        topHeader.add(lblHeader, BorderLayout.WEST);

        // ─── Content Area (CardLayout) ───────────────────────────
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIStyle.BACKGROUND_COLOR);

        overviewPanel    = new OverviewPanel(analyticsService);
        customerPanel    = new CustomerPanel(bankingService);
        accountPanel     = new AccountPanel(bankingService);
        transactionPanel = new TransactionPanel(bankingService);
        userPanel        = new UserPanel(authService, adminUser);

        contentArea.add(overviewPanel,    "overview");
        contentArea.add(customerPanel,    "customers");
        contentArea.add(accountPanel,     "accounts");
        contentArea.add(transactionPanel, "transactions");
        contentArea.add(userPanel,        "users");

        // ─── Main assembly ───────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        mainPanel.add(topHeader, BorderLayout.NORTH);
        mainPanel.add(contentArea, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        // Set initial active button and load overview data
        setActiveButton(btnOverview);
        overviewPanel.onActivated();
    }

    /**
     * Creates a sidebar navigation button and registers its click handler.
     */
    private JButton addNavButton(JPanel sidebar, String text, String panelName) {
        JButton btn = new JButton(text);
        btn.setFont(UIStyle.BUTTON_FONT);
        btn.setForeground(new Color(148, 163, 184)); // Slate 400
        btn.setBackground(UIStyle.PRIMARY_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(260, 48));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 10));

        btn.addActionListener(e -> {
            System.out.println("Nav Button Clicked: " + text + " -> " + panelName);
            setActiveButton(btn);
            switchPanel(panelName);
        });

        navButtons.add(btn);
        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        return btn;
    }

    /**
     * Highlight the active sidebar button and dim all others.
     */
    private void setActiveButton(JButton selected) {
        for (JButton btn : navButtons) {
            btn.setBackground(UIStyle.PRIMARY_COLOR);
            btn.setForeground(new Color(148, 163, 184)); // Slate 400
        }
        selected.setBackground(new Color(51, 65, 85));   // Slate 700
        selected.setForeground(Color.WHITE);
        activeButton = selected;
    }

    /**
     * Switch to the named panel + trigger its onActivated() callback.
     */
    private void switchPanel(String name) {
        System.out.println("Switching panel to: " + name);
        cardLayout.show(contentArea, name);
        
        switch (name) {
            case "overview":
                if (overviewPanel != null) overviewPanel.onActivated();
                break;
            case "customers":
                System.out.println("Activating Customer Panel...");
                if (customerPanel != null) customerPanel.onActivated();
                break;
            case "accounts":
                if (accountPanel != null) accountPanel.onActivated();
                break;
            case "transactions":
                if (transactionPanel != null) transactionPanel.onActivated();
                break;
            case "users":
                if (userPanel != null) userPanel.onActivated();
                break;
            default:
                System.err.println("Unknown panel: " + name);
        }
    }
}
