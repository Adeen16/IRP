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

-- loan table
CREATE TABLE IF NOT EXISTS loan (
    loan_id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    loan_amount REAL NOT NULL,
    interest_rate REAL NOT NULL DEFAULT 0,
    loan_duration INTEGER NOT NULL,
    emi REAL NOT NULL DEFAULT 0,
    loan_type TEXT NOT NULL DEFAULT 'PERSONAL',
    status TEXT CHECK(status IN ('APPROVED', 'REJECTED', 'PENDING')) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

-- account table
CREATE TABLE IF NOT EXISTS account (
    account_number TEXT PRIMARY KEY,
    customer_id INTEGER NOT NULL,
    balance REAL NOT NULL DEFAULT 0.00,
    account_type TEXT NOT NULL DEFAULT 'SAVINGS',
    transaction_password TEXT,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE
);

-- transaction table
CREATE TABLE IF NOT EXISTS "transaction" (
    transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_number TEXT NOT NULL,
    type TEXT CHECK(type IN ('DEPOSIT', 'WITHDRAW', 'TRANSFER')) NOT NULL,
    amount REAL NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    performed_by INTEGER,
    FOREIGN KEY (account_number) REFERENCES account(account_number) ON DELETE CASCADE
);

-- audit_log table
CREATE TABLE IF NOT EXISTS audit_log (
    log_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    action TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user (password: admin123)
INSERT OR IGNORE INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$Qcuod2BwzFFg2Lsb/1cOxeFrLRcO/IwaE/Ob6jZHlfKey2R/0ls8y', 'ADMIN');

-- Insert default user user (password: user123)
INSERT OR IGNORE INTO users (username, password_hash, role) VALUES
('user', '$2a$10$x2XiVwZmDt2E67eLUm70z.gMKhy7gQFimZob8QGFL3vt36no7yXQC', 'USER');
