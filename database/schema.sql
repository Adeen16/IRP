-- Banking Management System Database Schema
-- Converted for SQLite Persistence Layer

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    role TEXT CHECK(role IN ('ADMIN', 'USER')) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- customer table
CREATE TABLE IF NOT EXISTS customer (
    customer_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER UNIQUE,
    name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    cibil_score INTEGER DEFAULT 700,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- loan_requests table
CREATE TABLE IF NOT EXISTS loan_requests (
    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER,
    amount REAL,
    status TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

-- account table
CREATE TABLE IF NOT EXISTS account (
    account_number TEXT PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    balance REAL NOT NULL DEFAULT 0.00,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

-- transaction table
CREATE TABLE IF NOT EXISTS "transaction" (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_number TEXT NOT NULL,
    type TEXT CHECK(type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')) NOT NULL,
    amount REAL NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES account(account_number) ON DELETE CASCADE
);

-- Insert default admin user (password: admin123)
-- Password is hashed using SHA-256
INSERT OR IGNORE INTO users (username, password_hash, role) VALUES
('admin', '240be518fabd2724ddb6f04eebf74c720a948d7e831c08c8fa822809f', 'ADMIN');

-- Insert default user user (password: user123)
-- SHA-256 hash for user123: e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446
INSERT OR IGNORE INTO users (username, password_hash, role) VALUES
('user', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', 'USER');
