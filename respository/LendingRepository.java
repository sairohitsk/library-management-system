package com.library.repository;

import com.library.model.LendingRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory store for {@link LendingRecord} objects.
 */
public class LendingRepository {

    private final Map<String, LendingRecord> store = new HashMap<>();

    public void save(LendingRecord record) {
        store.put(record.getRecordId(), record);
    }

    public Optional<LendingRecord> findById(String recordId) {
        return Optional.ofNullable(store.get(recordId));
    }

    /** Returns the single active (not-yet-returned) record for a given ISBN, if any. */
    public Optional<LendingRecord> findActiveByIsbn(String isbn) {
        return store.values().stream()
                .filter(r -> r.getIsbn().equals(isbn) && r.isActive())
                .findFirst();
    }

    /** Returns all records (active and historical) for a given patron. */
    public List<LendingRecord> findByPatronId(String patronId) {
        return store.values().stream()
                .filter(r -> r.getPatronId().equals(patronId))
                .collect(Collectors.toList());
    }

    public List<LendingRecord> findAllActive() {
        return store.values().stream()
                .filter(LendingRecord::isActive)
                .collect(Collectors.toList());
    }

    public List<LendingRecord> findOverdue() {
        return store.values().stream()
                .filter(LendingRecord::isOverdue)
                .collect(Collectors.toList());
    }

    public Collection<LendingRecord> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }
}
