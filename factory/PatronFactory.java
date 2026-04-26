package com.library.factory;

import com.library.model.Patron;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory for creating {@link Patron} instances.
 * Generates sequential, human-readable patron IDs (P-0001, P-0002 …).
 */
public class PatronFactory {

    private static final AtomicLong counter = new AtomicLong(0);

    private PatronFactory() { /* utility class */ }

    /**
     * Creates a patron with an auto-generated ID.
     */
    public static Patron create(String name, String email, String phoneNumber) {
        String patronId = String.format("P-%04d", counter.incrementAndGet());
        return create(patronId, name, email, phoneNumber);
    }

    /**
     * Creates a patron with a caller-supplied ID (useful for data migration).
     */
    public static Patron create(String patronId, String name, String email, String phoneNumber) {
        return new Patron.Builder(patronId)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
    }
}
