package com.library.search;

import com.library.model.Book;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// ─────────────────────────────────────────────────────────────────────────────
// Title search (case-insensitive, partial match)
// ─────────────────────────────────────────────────────────────────────────────

class TitleSearchStrategy implements SearchStrategy {
    @Override
    public List<Book> search(Collection<Book> books, String query) {
        String q = query.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Author search (case-insensitive, partial match)
// ─────────────────────────────────────────────────────────────────────────────

class AuthorSearchStrategy implements SearchStrategy {
    @Override
    public List<Book> search(Collection<Book> books, String query) {
        String q = query.toLowerCase();
        return books.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ISBN search (exact match)
// ─────────────────────────────────────────────────────────────────────────────

class ISBNSearchStrategy implements SearchStrategy {
    @Override
    public List<Book> search(Collection<Book> books, String query) {
        return books.stream()
                .filter(b -> b.getIsbn().equalsIgnoreCase(query.trim()))
                .collect(Collectors.toList());
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Genre search
// ─────────────────────────────────────────────────────────────────────────────

class GenreSearchStrategy implements SearchStrategy {
    @Override
    public List<Book> search(Collection<Book> books, String query) {
        return books.stream()
                .filter(b -> b.getGenre() != null
                        && b.getGenre().name().equalsIgnoreCase(query.trim()))
                .collect(Collectors.toList());
    }
}
