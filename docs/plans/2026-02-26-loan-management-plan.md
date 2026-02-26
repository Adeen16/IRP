# Loan Management Feature Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Allow users to request loans and administrators to approve them via a new database table and Swing UI panels.

**Architecture:** We will create the `loans` table, build the Java Model/DAO/Service layer, and finally wire up the Swing UI panels to these services.

**Tech Stack:** Java SE, Swing, FlatLaf, MySQL, JDBC.

---

### Task 1: Create the Database Table

**Files:**
- Modify: `c:/Users/ADEEN/workspace/IRP/database/schema.sql`

**Step 1: Write the schema addition**

```sql
-- Loans table
CREATE TABLE loans (
    loan_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    term_months INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'PAID') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);
```

**Step 2: Commit**

```bash
git add database/schema.sql
git commit -m "feat: add loans table to database schema"
```

---

### Task 2: Create the Loan Model

**Files:**
- Create: `c:/Users/ADEEN/workspace/IRP/src/banking/model/Loan.java`

**Step 1: Write the implementation**

```java
package banking.model;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Loan {
    private int loanId;
    private int customerId;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private int termMonths;
    private String status;
    private Timestamp createdAt;

    // Getters and Setters...
}
```

**Step 2: Commit**

```bash
git add src/banking/model/Loan.java
git commit -m "feat: add Loan entity model"
```

---

### Task 3: Create the LoanDAO Interface

**Files:**
- Create: `c:/Users/ADEEN/workspace/IRP/src/banking/dao/LoanDAO.java`

**Step 1: Write the implementation**

```java
package banking.dao;
import banking.model.Loan;
import java.util.List;

public interface LoanDAO {
    boolean insertLoan(Loan loan);
    List<Loan> getLoansByCustomer(int customerId);
    List<Loan> getPendingLoans();
    boolean updateLoanStatus(int loanId, String status);
}
```

**Step 2: Commit**

```bash
git add src/banking/dao/LoanDAO.java
git commit -m "feat: add LoanDAO interface"
```
