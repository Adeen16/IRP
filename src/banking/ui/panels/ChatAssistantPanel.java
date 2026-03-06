package banking.ui.panels;

import banking.model.User;
import banking.service.BankingService;
import banking.service.ChatController;
import banking.ui.Refreshable;
import banking.ui.UIStyle;
import banking.ui.components.ModernUIComponents;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

/**
 * Chat panel that provides a local AI banking assistant.
 * Uses Ollama (local LLM) via ChatController -> LLMService -> BankingService.
 * The LLM has NO direct database access.
 */
public class ChatAssistantPanel extends JPanel implements Refreshable {

    private final User currentUser;
    private final ChatController chatController;

    private JTextPane chatHistory;
    private JTextField userInput;
    private JButton sendButton;
    private JLabel statusLabel;
    private StyledDocument chatDoc;

    // Styles for chat messages
    private Style userStyle;
    private Style assistantStyle;
    private Style systemStyle;
    private Style labelStyle;

    public ChatAssistantPanel(User user, BankingService bankingService) {
        this.currentUser = user;
        this.chatController = new ChatController(user, bankingService);
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIStyle.BACKGROUND_COLOR);

        // ===== Header =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
            new EmptyBorder(15, 25, 15, 25)
        ));

        JPanel headerLeft = new JPanel(new GridLayout(2, 1, 0, 2));
        headerLeft.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("AI Banking Assistant");
        titleLabel.setFont(UIStyle.HEADER_FONT);
        titleLabel.setForeground(UIStyle.TEXT_COLOR);
        headerLeft.add(titleLabel);

        statusLabel = new JLabel("Checking Ollama connection...");
        statusLabel.setFont(UIStyle.SMALL_FONT);
        statusLabel.setForeground(UIStyle.TEXT_LIGHT);
        headerLeft.add(statusLabel);

        JButton clearBtn = new JButton("Clear Chat");
        UIStyle.styleButton(clearBtn, UIStyle.SECONDARY_COLOR);
        clearBtn.addActionListener(e -> clearChat());

        JComboBox<String> modelSelector = new JComboBox<>(new String[]{"phi3", "llama3:8b"});
        modelSelector.setFont(UIStyle.SMALL_FONT);
        modelSelector.setPreferredSize(new Dimension(100, 25));
        modelSelector.setToolTipText("Switch AI model");
        modelSelector.setSelectedItem(chatController.getModelName());
        modelSelector.addActionListener(e -> {
            String selected = (String) modelSelector.getSelectedItem();
            chatController.setModel(selected);
            statusLabel.setText("Switched to " + selected + " - Reconnecting...");
            checkConnection();
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setBackground(Color.WHITE);
        headerRight.add(modelSelector);
        headerRight.add(clearBtn);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== Chat History =====
        chatHistory = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        chatHistory.setEditable(false);
        chatHistory.setBackground(new Color(248, 250, 252));
        chatHistory.setMargin(new Insets(15, 15, 15, 15));
        chatDoc = chatHistory.getStyledDocument();

        // Define text styles
        userStyle = chatHistory.addStyle("user", null);
        StyleConstants.setForeground(userStyle, new Color(30, 41, 59));
        StyleConstants.setFontFamily(userStyle, "Segoe UI");
        StyleConstants.setFontSize(userStyle, 14);

        assistantStyle = chatHistory.addStyle("assistant", null);
        StyleConstants.setForeground(assistantStyle, new Color(30, 80, 160));
        StyleConstants.setFontFamily(assistantStyle, "Segoe UI");
        StyleConstants.setFontSize(assistantStyle, 14);

        systemStyle = chatHistory.addStyle("system", null);
        StyleConstants.setForeground(systemStyle, UIStyle.TEXT_LIGHT);
        StyleConstants.setFontFamily(systemStyle, "Segoe UI");
        StyleConstants.setFontSize(systemStyle, 12);
        StyleConstants.setItalic(systemStyle, true);

        labelStyle = chatHistory.addStyle("label", null);
        StyleConstants.setFontFamily(labelStyle, "Segoe UI");
        StyleConstants.setFontSize(labelStyle, 13);
        StyleConstants.setBold(labelStyle, true);

        JScrollPane scrollPane = new JScrollPane(chatHistory);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        ModernUIComponents.RoundedPanel chatCard = new ModernUIComponents.RoundedPanel(15, Color.WHITE);
        chatCard.setLayout(new BorderLayout());
        chatCard.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatCard.add(scrollPane, BorderLayout.CENTER);

        JPanel chatWrapper = new JPanel(new BorderLayout());
        chatWrapper.setBackground(UIStyle.BACKGROUND_COLOR);
        chatWrapper.setBorder(new EmptyBorder(15, 25, 10, 25));
        chatWrapper.add(chatCard, BorderLayout.CENTER);

        add(chatWrapper, BorderLayout.CENTER);

        // ===== Input Area =====
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(UIStyle.BACKGROUND_COLOR);
        inputPanel.setBorder(new EmptyBorder(5, 25, 20, 25));

        userInput = new JTextField();
        userInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(12, 15, 12, 15)
        ));
        userInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        sendButton = new JButton("Send");
        UIStyle.styleButton(sendButton, UIStyle.ACCENT_COLOR);
        sendButton.setPreferredSize(new Dimension(100, 45));
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(userInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        add(inputPanel, BorderLayout.SOUTH);

        // Show welcome message
        appendSystemMessage("Welcome to the AI Banking Assistant! Ask me about your transactions, " +
            "spending patterns, account details, or any banking help.\n" +
            "Model: " + chatController.getModelName() + "\n");
    }

    /**
     * Sends the user's message and retrieves the LLM response on a background thread.
     */
    private void sendMessage() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;

        // Display user message
        appendUserMessage(message);
        userInput.setText("");

        // Disable input while processing
        setInputEnabled(false);
        statusLabel.setText("Thinking...");
        statusLabel.setForeground(UIStyle.ACCENT_COLOR);

        // Process on background thread
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return chatController.processMessage(message);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    appendAssistantMessage(response);
                } catch (Exception e) {
                    appendAssistantMessage("[Error] " + e.getMessage());
                }
                setInputEnabled(true);
                statusLabel.setText("Model: " + chatController.getModelName() + " | Ready");
                statusLabel.setForeground(UIStyle.TEXT_LIGHT);
            }
        }.execute();
    }

    private void setInputEnabled(boolean enabled) {
        userInput.setEnabled(enabled);
        sendButton.setEnabled(enabled);
        if (enabled) {
            userInput.requestFocusInWindow();
        }
    }

    // ===== Chat Display Methods =====

    private void appendUserMessage(String message) {
        try {
            Style userLabel = chatHistory.addStyle("userLabel", labelStyle);
            StyleConstants.setForeground(userLabel, new Color(30, 41, 59));
            chatDoc.insertString(chatDoc.getLength(), "You:\n", userLabel);
            chatDoc.insertString(chatDoc.getLength(), message + "\n\n", userStyle);
            scrollToBottom();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendAssistantMessage(String message) {
        try {
            Style aiLabel = chatHistory.addStyle("aiLabel", labelStyle);
            StyleConstants.setForeground(aiLabel, new Color(30, 80, 160));
            chatDoc.insertString(chatDoc.getLength(), "Assistant:\n", aiLabel);
            chatDoc.insertString(chatDoc.getLength(), message + "\n\n", assistantStyle);
            scrollToBottom();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendSystemMessage(String message) {
        try {
            chatDoc.insertString(chatDoc.getLength(), message + "\n", systemStyle);
            scrollToBottom();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            chatHistory.setCaretPosition(chatDoc.getLength());
        });
    }

    private void clearChat() {
        try {
            chatDoc.remove(0, chatDoc.getLength());
            appendSystemMessage("Chat cleared. Ask me anything about your banking activity.\n");
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivated() {
        // Check Ollama status on activation
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return chatController.isLLMAvailable();
            }

            @Override
            protected void done() {
                try {
                    boolean available = get();
                    if (available) {
                        statusLabel.setText("Model: " + chatController.getModelName() + " | Connected");
                        statusLabel.setForeground(new Color(16, 185, 129)); // green
                    } else {
                        statusLabel.setText("Ollama offline — run 'ollama serve' in terminal");
                        statusLabel.setForeground(UIStyle.DANGER_COLOR);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Connection check failed");
                    statusLabel.setForeground(UIStyle.DANGER_COLOR);
                }
            }
        }.execute();

        userInput.requestFocusInWindow();
    }

    private void checkConnection() {
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                return chatController.isLLMAvailable();
            }

            @Override
            protected void done() {
                try {
                    boolean available = get();
                    if (available) {
                        statusLabel.setText("Model: " + chatController.getModelName() + " | Connected");
                        statusLabel.setForeground(new Color(16, 185, 129));
                    } else {
                        statusLabel.setText("Ollama offline — run 'ollama serve' in terminal");
                        statusLabel.setForeground(UIStyle.DANGER_COLOR);
                    }
                } catch (Exception e) {
                    statusLabel.setText("Connection check failed");
                    statusLabel.setForeground(UIStyle.DANGER_COLOR);
                }
            }
        }.execute();
    }
}
