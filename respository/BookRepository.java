package com.library.repository;

import com.library.model.Book;

import java.util.*;

/**
 * In-memory store for {@link Book} objects, keyed by ISBN.
 *
 * <p>Following the Repository pattern keeps persistence concerns (even if
 * in-memory) completely isolated from business logic in the service layer.
 */
public class BookRepository {

    // Master catalogue — all books across all branches
    private final Map<String, Book> store = new HashMap<>();

    public void save(Book book) {
        store.put(book.getIsbn(), book);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(store.get(isbn));
    }

    public boolean delete(String isbn) {
        return store.remove(isbn) != null;
    }

    public boolean existsByIsbn(String isbn) {
        return store.containsKey(isbn);
    }

    public Collection<Book> findAll() {
        return Collections.unmodifiableCollection(store.values());
    }

    public int count() { return store.size(); }
}
