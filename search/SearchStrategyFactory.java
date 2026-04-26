package com.library.search;

/**
 * Factory for {@link SearchStrategy} instances.
 *
 * <p>Centralises strategy creation so callers never depend on concrete
 * strategy classes directly (Dependency Inversion principle).
 */
public class SearchStrategyFactory {

    public enum SearchType { TITLE, AUTHOR, ISBN, GENRE }

    private SearchStrategyFactory() { /* utility class */ }

    /**
     * Returns the appropriate search strategy for the given type.
     *
     * @param type the desired search axis
     * @return a concrete {@link SearchStrategy}
     */
    public static SearchStrategy create(SearchType type) {
        switch (type) {
            case TITLE:  return new TitleSearchStrategy();
            case AUTHOR: return new AuthorSearchStrategy();
            case ISBN:   return new ISBNSearchStrategy();
            case GENRE:  return new GenreSearchStrategy();
            default:     throw new IllegalArgumentException("Unknown search type: " + type);
        }
    }
}
