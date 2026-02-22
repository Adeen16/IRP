-- Banking Management System Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS banking_system;
USE banking_system;

-- Users table for authentication
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- Customers table
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    date_of_birth DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Accounts table
CREATE TABLE accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT', 'FIXED_DEPOSIT') DEFAULT 'SAVINGS',
    balance DECIMAL(15, 2) DEFAULT 0.00,
    status ENUM('ACTIVE', 'INACTIVE', 'CLOSED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- Transactions table
CREATE TABLE transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    balance_after DECIMAL(15, 2) NOT NULL,
    reference_account VARCHAR(20),
    description VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number),
    INDEX idx_account_date (account_number, transaction_date),
    INDEX idx_transaction_date (transaction_date)
);

-- Account audit log
CREATE TABLE audit_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(50) NOT NULL,
    table_name VARCHAR(50),
    record_id VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45)
);

-- Insert default admin user (password: admin123)
-- Password is hashed using SHA-256
INSERT INTO users (username, password_hash, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb9f7c7b21c4e5e1b8f8e3d5a0c1b2d3e4f5a6b7', 'ADMIN');

-- Create views for reporting
CREATE VIEW v_account_summary AS
SELECT 
    c.customer_id,
    c.name AS customer_name,
    a.account_number,
    a.account_type,
    a.balance,
    a.status,
    COUNT(t.transaction_id) AS transaction_count
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
LEFT JOIN transactions t ON a.account_number = t.account_number
GROUP BY c.customer_id, c.name, a.account_number, a.account_type, a.balance, a.status;

CREATE VIEW v_daily_transactions AS
SELECT 
    DATE(transaction_date) AS transaction_date,
    transaction_type,
    COUNT(*) AS transaction_count,
    SUM(amount) AS total_amount
FROM transactions
GROUP BY DATE(transaction_date), transaction_type;

-- Stored procedure for money transfer
DELIMITER //
CREATE PROCEDURE transfer_money(
    IN from_account VARCHAR(20),
    IN to_account VARCHAR(20),
    IN amount DECIMAL(15, 2),
    OUT success BOOLEAN,
    OUT message VARCHAR(255)
)
BEGIN
    DECLARE from_balance DECIMAL(15, 2);
    DECLARE to_balance DECIMAL(15, 2);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET success = FALSE;
        SET message = 'Transfer failed due to database error';
    END;
    
    START TRANSACTION;
    
    SELECT balance INTO from_balance FROM accounts WHERE account_number = from_account FOR UPDATE;
    
    IF from_balance IS NULL THEN
        SET success = FALSE;
        SET message = 'Source account not found';
        ROLLBACK;
    ELSEIF from_balance < amount THEN
        SET success = FALSE;
        SET message = 'Insufficient balance';
        ROLLBACK;
    ELSE
        UPDATE accounts SET balance = balance - amount WHERE account_number = from_account;
        UPDATE accounts SET balance = balance + amount WHERE account_number = to_account;
        
        SELECT balance INTO from_balance FROM accounts WHERE account_number = from_account;
        SELECT balance INTO to_balance FROM accounts WHERE account_number = to_account;
        
        INSERT INTO transactions (account_number, transaction_type, amount, balance_after, reference_account, description)
        VALUES (from_account, 'TRANSFER_OUT', amount, from_balance, to_account, CONCAT('Transfer to ', to_account));
        
        INSERT INTO transactions (account_number, transaction_type, amount, balance_after, reference_account, description)
        VALUES (to_account, 'TRANSFER_IN', amount, to_balance, from_account, CONCAT('Transfer from ', from_account));
        
        SET success = TRUE;
        SET message = 'Transfer successful';
        COMMIT;
    END IF;
END //
DELIMITER ;
