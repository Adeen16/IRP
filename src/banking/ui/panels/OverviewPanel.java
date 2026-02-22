package banking.ui.panels;

import banking.service.AnalyticsService;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Dashboard overview panel showing stats and analytics chart.
 * Extracted from AdminDashboard for CardLayout integration.
 */
public class OverviewPanel extends JPanel implements Refreshable {
    private final AnalyticsService analyticsService;
    private JLabel lblTotalBalance;
    private JLabel lblTotalAccounts;
    private JLabel lblDailyTransactions;

    public OverviewPanel(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(30, 30));
        setBackground(UIStyle.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(30, 30, 30, 30));

        // --- Stats Panel ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 25, 0));
        statsPanel.setBackground(UIStyle.BACKGROUND_COLOR);

        JPanel card1 = UIStyle.createStatCard("TOTAL ASSETS", "$0.00", UIStyle.ACCENT_COLOR);
        JPanel card2 = UIStyle.createStatCard("ACTIVE ACCOUNTS", "0", UIStyle.SUCCESS_COLOR);
        JPanel card3 = UIStyle.createStatCard("TODAY'S TRANSACTIONS", "0", UIStyle.WARNING_COLOR);

        // Grab the value labels so we can update them later
        lblTotalBalance = findValueLabel(card1);
        lblTotalAccounts = findValueLabel(card2);
        lblDailyTransactions = findValueLabel(card3);

        statsPanel.add(card1);
        statsPanel.add(card2);
        statsPanel.add(card3);

        add(statsPanel, BorderLayout.NORTH);

        // --- Analytics Chart ---
        ModernUIComponents.RoundedPanel chartCard =
                new ModernUIComponents.RoundedPanel(20, Color.WHITE);
        chartCard.setLayout(new BorderLayout(20, 20));
        chartCard.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblChartTitle = new JLabel("Transaction Trends");
        lblChartTitle.setFont(UIStyle.HEADER_FONT);
        chartCard.add(lblChartTitle, BorderLayout.NORTH);

        try {
            org.jfree.data.category.DefaultCategoryDataset dataset =
                    new org.jfree.data.category.DefaultCategoryDataset();
            dataset.addValue(200, "Transactions", "Mon");
            dataset.addValue(350, "Transactions", "Tue");
            dataset.addValue(420, "Transactions", "Wed");
            dataset.addValue(380, "Transactions", "Thu");
            dataset.addValue(510, "Transactions", "Fri");
            dataset.addValue(480, "Transactions", "Sat");
            dataset.addValue(550, "Transactions", "Sun");

            org.jfree.chart.JFreeChart lineChart = org.jfree.chart.ChartFactory.createLineChart(
                    null, null, null, dataset,
                    org.jfree.chart.plot.PlotOrientation.VERTICAL,
                    false, true, false
            );

            lineChart.setBackgroundPaint(Color.WHITE);
            org.jfree.chart.plot.CategoryPlot plot = lineChart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            plot.setRangeGridlinePaint(new Color(226, 232, 240));

            org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(lineChart);
            chartPanel.setBackground(Color.WHITE);
            chartCard.add(chartPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            chartCard.add(new JLabel("Chart unavailable"), BorderLayout.CENTER);
        }

        add(chartCard, BorderLayout.CENTER);
    }

    /**
     * Utility to find the second JLabel (the value label) inside a stat card.
     */
    private JLabel findValueLabel(JPanel card) {
        for (Component c : card.getComponents()) {
            if (c instanceof JPanel inner) {
                for (Component ic : inner.getComponents()) {
                    if (ic instanceof JLabel lbl && lbl.getFont().getSize() >= 24) {
                        return lbl;
                    }
                }
            }
        }
        return new JLabel(); // fallback
    }

    @Override
    public void onActivated() {
        refreshStats();
    }

    private void refreshStats() {
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return analyticsService.getDashboardSummary();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    BigDecimal balance = stats.get("totalBalance") instanceof BigDecimal b ? b : BigDecimal.ZERO;
                    int accounts = stats.get("totalAccounts") != null
                            ? ((Number) stats.get("totalAccounts")).intValue() : 0;
                    int txns = stats.get("todayTransactions") != null
                            ? ((Number) stats.get("todayTransactions")).intValue() : 0;

                    lblTotalBalance.setText(banking.util.Validator.formatCurrency(balance));
                    lblTotalAccounts.setText(String.valueOf(accounts));
                    lblDailyTransactions.setText(String.valueOf(txns));
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }
}
