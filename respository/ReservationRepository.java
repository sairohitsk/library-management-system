package com.library.repository;

import com.library.model.Reservation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory store for {@link Reservation} objects.
 */
public class ReservationRepository {

    private final Map<String, Reservation> store = new HashMap<>();

    public void save(Reservation reservation) {
        store.put(reservation.getReservationId(), reservation);
    }

    public Optional<Reservation> findById(String reservationId) {
        return Optional.ofNullable(store.get(reservationId));
    }

    /** Active reservations for a specific ISBN, in insertion order. */
    public List<Reservation> findActiveByIsbn(String isbn) {
        return store.values().stream()
                .filter(r -> r.getIsbn().equals(isbn) && r.isActive())
                .sorted(Comparator.comparing(Reservation::getReservationDate))
                .collect(Collectors.toList());
    }

    /** Active reservations held by a specific patron. */
    public List<Reservation> findActiveByPatronId(String patronId) {
        return store.values().stream()
                .filter(r -> r.getPatronId().equals(patronId) && r.isActive())
                .collect(Collectors.toList());
    }

    public boolean existsActiveForPatronAndIsbn(String patronId, String isbn) {
        return store.values().stream()
                .anyMatch(r -> r.getPatronId().equals(patronId)
                        && r.getIsbn().equals(isbn)
                        && r.isActive());
    }

    public Collection<Reservation> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }
}
