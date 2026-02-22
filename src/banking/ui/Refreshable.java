package banking.ui;

/**
 * Interface for panels that need to reload data when they become visible.
 * Implemented by each module panel (Customers, Accounts, etc.)
 */
public interface Refreshable {
    /** Called every time this panel becomes the active view via CardLayout. */
    void onActivated();
}
