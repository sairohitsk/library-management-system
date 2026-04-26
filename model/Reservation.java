package com.library.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a patron's reservation for a currently-unavailable book.
 * When the book becomes available the Observer pattern triggers a notification.
 */
public class Reservation {

    public enum Status { PENDING, FULFILLED, CANCELLED, EXPIRED }

    private final String reservationId;
    private final String isbn;
    private final String patronId;
    private final LocalDate reservationDate;
    private final LocalDate expiryDate;          // Auto-expires after a configurable window
    private Status status;

    public Reservation(String reservationId, String isbn, String patronId,
                       LocalDate reservationDate, LocalDate expiryDate) {
        this.reservationId   = Objects.requireNonNull(reservationId);
        this.isbn            = Objects.requireNonNull(isbn);
        this.patronId        = Objects.requireNonNull(patronId);
        this.reservationDate = Objects.requireNonNull(reservationDate);
        this.expiryDate      = Objects.requireNonNull(expiryDate);
        this.status          = Status.PENDING;
    }

    public String    getReservationId()   { return reservationId; }
    public String    getIsbn()            { return isbn; }
    public String    getPatronId()        { return patronId; }
    public LocalDate getReservationDate() { return reservationDate; }
    public LocalDate getExpiryDate()      { return expiryDate; }
    public Status    getStatus()          { return status; }

    public void fulfil()  { this.status = Status.FULFILLED; }
    public void cancel()  { this.status = Status.CANCELLED; }

    public boolean isActive() {
        return status == Status.PENDING && !LocalDate.now().isAfter(expiryDate);
    }

    @Override
    public String toString() {
        return String.format("Reservation{id='%s', isbn='%s', patron='%s', status=%s, expiry=%s}",
                reservationId, isbn, patronId, status, expiryDate);
    }
}
