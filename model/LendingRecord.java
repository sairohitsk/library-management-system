package com.library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable record of a single book checkout/return transaction.
 * Serves as the audit trail for the lending process.
 */
public class LendingRecord {

    private final String recordId;
    private final String isbn;
    private final String patronId;
    private final LocalDate checkoutDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;        // null until book is returned

    public LendingRecord(String recordId, String isbn, String patronId,
                         LocalDate checkoutDate, LocalDate dueDate) {
        this.recordId     = Objects.requireNonNull(recordId);
        this.isbn         = Objects.requireNonNull(isbn);
        this.patronId     = Objects.requireNonNull(patronId);
        this.checkoutDate = Objects.requireNonNull(checkoutDate);
        this.dueDate      = Objects.requireNonNull(dueDate);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String    getRecordId()     { return recordId; }
    public String    getIsbn()         { return isbn; }
    public String    getPatronId()     { return patronId; }
    public LocalDate getCheckoutDate() { return checkoutDate; }
    public LocalDate getDueDate()      { return dueDate; }
    public LocalDate getReturnDate()   { return returnDate; }

    public boolean isActive()    { return returnDate == null; }
    public boolean isOverdue()   { return isActive() && LocalDate.now().isAfter(dueDate); }

    public void markReturned(LocalDate returnDate) {
        this.returnDate = Objects.requireNonNull(returnDate);
    }

    @Override
    public String toString() {
        return String.format("LendingRecord{id='%s', isbn='%s', patron='%s', checkout=%s, due=%s, returned=%s}",
                recordId, isbn, patronId, checkoutDate, dueDate,
                returnDate != null ? returnDate : "ACTIVE");
    }
}
