package com.library.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a library patron (member).
 * Tracks contact information, active loans, and borrowing history.
 */
public class Patron {

    private final String patronId;       // Immutable system-assigned ID
    private String name;
    private String email;
    private String phoneNumber;

    // Active ISBNs currently checked out
    private final List<String> activeLoanIsbns;

    // Full historical ISBNs (including returned books) — used for recommendations
    private final List<String> borrowingHistory;

    private Patron(Builder builder) {
        this.patronId        = builder.patronId;
        this.name            = builder.name;
        this.email           = builder.email;
        this.phoneNumber     = builder.phoneNumber;
        this.activeLoanIsbns = new ArrayList<>();
        this.borrowingHistory = new ArrayList<>();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getPatronId()   { return patronId; }
    public String getName()       { return name; }
    public String getEmail()      { return email; }
    public String getPhoneNumber(){ return phoneNumber; }

    public List<String> getActiveLoanIsbns() {
        return Collections.unmodifiableList(activeLoanIsbns);
    }

    public List<String> getBorrowingHistory() {
        return Collections.unmodifiableList(borrowingHistory);
    }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setName(String name)           { this.name = Objects.requireNonNull(name); }
    public void setEmail(String email)         { this.email = email; }
    public void setPhoneNumber(String phone)   { this.phoneNumber = phone; }

    // ── Loan tracking ─────────────────────────────────────────────────────────

    public void addActiveLoan(String isbn) {
        activeLoanIsbns.add(isbn);
        if (!borrowingHistory.contains(isbn)) {
            borrowingHistory.add(isbn);
        }
    }

    public boolean removeActiveLoan(String isbn) {
        return activeLoanIsbns.remove(isbn);
    }

    public boolean hasActiveLoan(String isbn) {
        return activeLoanIsbns.contains(isbn);
    }

    public int activeLoanCount() { return activeLoanIsbns.size(); }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static class Builder {
        private final String patronId;
        private String name;
        private String email;
        private String phoneNumber;

        public Builder(String patronId) {
            this.patronId = Objects.requireNonNull(patronId);
        }

        public Builder name(String name)             { this.name = name;           return this; }
        public Builder email(String email)           { this.email = email;         return this; }
        public Builder phoneNumber(String phone)     { this.phoneNumber = phone;   return this; }

        public Patron build() {
            Objects.requireNonNull(name, "Patron name must not be null");
            return new Patron(this);
        }
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patron)) return false;
        return patronId.equals(((Patron) o).patronId);
    }

    @Override
    public int hashCode() { return patronId.hashCode(); }

    @Override
    public String toString() {
        return String.format("Patron{id='%s', name='%s', email='%s', activeLoans=%d}",
                patronId, name, email, activeLoanIsbns.size());
    }
}
