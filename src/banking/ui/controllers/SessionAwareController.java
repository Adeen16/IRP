package banking.ui.controllers;

import banking.model.User;

public interface SessionAwareController {
    void setUser(User user);
}
