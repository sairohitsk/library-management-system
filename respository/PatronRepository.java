package com.library.repository;

import com.library.model.Patron;

import java.util.*;

/**
 * In-memory store for {@link Patron} objects, keyed by patron ID.
 */
public class PatronRepository {

    private final Map<String, Patron> store = new HashMap<>();

    public void save(Patron patron) {
        store.put(patron.getPatronId(), patron);
    }

    public Optional<Patron> findById(String patronId) {
        return Optional.ofNullable(store.get(patronId));
    }

    public Optional<Patron> findByEmail(String email) {
        return store.values().stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                .findFirst();
    }

    public boolean delete(String patronId) {
        return store.remove(patronId) != null;
    }

    public boolean existsById(String patronId) {
        return store.containsKey(patronId);
    }

    public Collection<Patron> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }

    public int count() { return store.size(); }
}
