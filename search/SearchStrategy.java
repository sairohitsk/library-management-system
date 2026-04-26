package com.library.search;

import com.library.model.Book;

import java.util.Collection;
import java.util.List;

/**
 * Strategy interface for book search algorithms.
 *
 * <p>Applying the Strategy design pattern here allows the search behaviour to
 * be swapped at runtime (title search, author search, ISBN search, genre filter,
 * full-text search, etc.) without changing the BookService.
 */
public interface SearchStrategy {

    /**
     * Filters the given collection of books using this strategy's criteria.
     *
     * @param books  candidate pool (typically the full branch inventory)
     * @param query  the search term supplied by the caller
     * @return       matching books (may be empty, never null)
     */
    List<Book> search(Collection<Book> books, String query);
}
