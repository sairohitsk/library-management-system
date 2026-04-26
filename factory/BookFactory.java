package com.library.factory;

import com.library.model.Book;
import com.library.model.enums.Genre;

import java.util.UUID;

/**
 * Factory for creating {@link Book} instances.
 *
 * <p>Centralises ISBN generation and default-value assignment so that
 * object-creation logic is never scattered across multiple services.
 * Implements the Factory design pattern.
 */
public class BookFactory {

    private BookFactory() { /* utility class */ }

    /**
     * Creates a book with an auto-generated UUID as the ISBN.
     */
    public static Book create(String title, String author, int publicationYear, Genre genre) {
        String isbn = UUID.randomUUID().toString();
        return create(isbn, title, author, publicationYear, genre);
    }

    /**
     * Creates a book with the supplied ISBN.
     */
    public static Book create(String isbn, String title, String author,
                              int publicationYear, Genre genre) {
        return new Book.Builder(isbn)
                .title(title)
                .author(author)
                .publicationYear(publicationYear)
                .genre(genre)
                .build();
    }

    /**
     * Convenience overload — genre defaults to {@code Genre.OTHER}.
     */
    public static Book create(String isbn, String title, String author, int publicationYear) {
        return create(isbn, title, author, publicationYear, Genre.OTHER);
    }
}
