package com.library.observer;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable value object that describes a library event.
 * Passed to every registered {@link LibraryObserver}.
 */
public class LibraryEvent {

    public enum Type {
        BOOK_CHECKED_OUT,
        BOOK_RETURNED,
        BOOK_RESERVED,
        RESERVATION_FULFILLED,
        RESERVATION_CANCELLED,
        BOOK_TRANSFERRED,
        BOOK_ADDED,
        BOOK_REMOVED,
        PATRON_REGISTERED
    }

    private final Type          type;
    private final String        isbn;
    private final String        patronId;
    private final String        branchId;
    private final LocalDateTime occurredAt;
    private final String        message;

    public LibraryEvent(Type type, String isbn, String patronId,
                        String branchId, String message) {
        this.type       = Objects.requireNonNull(type);
        this.isbn       = isbn;
        this.patronId   = patronId;
        this.branchId   = branchId;
        this.message    = message;
        this.occurredAt = LocalDateTime.now();
    }

    public Type          getType()       { return type; }
    public String        getIsbn()       { return isbn; }
    public String        getPatronId()   { return patronId; }
    public String        getBranchId()   { return branchId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public String        getMessage()    { return message; }

    @Override
    public String toString() {
        return String.format("[%s] %s | isbn=%s patron=%s branch=%s",
                occurredAt, type, isbn, patronId, branchId);
    }
}
