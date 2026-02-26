# Loan Management Module - Design Document

**Date:** 2026-02-26
**Topic:** Loan Management Core Feature

## Database Architecture
We will create a new `loans` table in the database to track loan applications and their status.
- **Fields:** `loan_id`, `customer_id`, `amount`, `interest_rate`, `term_months`, `status` (PENDING, APPROVED, REJECTED, PAID), `created_at`.
- **Relationship:** Linked to the `customers` table.

## Java Components (Backend)
- **Model:** `banking.model.Loan` class to represent the database entity.
- **DAO Level:** `banking.dao.LoanDAO` and `LoanDAOImpl` to handle CRUD operations on the `loans` table.
- **Service Level:** `banking.service.LoanService` to handle business logic.

## User Interface (Frontend - Swing)
- **User View (`UserDashboard`):** A new "Apply for Loan" panel.
- **Admin View (`AdminDashboard`):** A new "Manage Loans" panel where administrators can see all `PENDING` loans. 

## Data Flow
1. User enters data ($5,000 for 24 months) in UI.
2. UI passes data to `LoanService`.
3. `LoanService` validates and passes a `Loan` to `LoanDAO`.
4. `LoanDAO` executes `INSERT INTO loans` with status `PENDING`.
5. Admin logs in, clicks "Approve", status updates to `APPROVED`.
