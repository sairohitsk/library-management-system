package com.library.service;

import com.library.model.Book;
import com.library.model.Patron;
import com.library.model.enums.Genre;
import com.library.repository.BookRepository;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Generates personalised book recommendations for a patron.
 *
 * <p>Algorithm (two-pass):
 * <ol>
 *   <li><b>Genre affinity</b> — count genres from the patron's borrowing history;
 *       rank candidate books by how often their genre appears in the history.</li>
 *   <li><b>Author affinity</b> — give a secondary boost to books whose author
 *       the patron has read before.</li>
 * </ol>
 *
 * Books the patron has already read, and books that are not AVAILABLE, are
 * excluded from results.
 */
public class RecommendationService {

    private static final Logger log = Logger.getLogger(RecommendationService.class.getName());
    private static final int    DEFAULT_LIMIT = 5;

    private final BookRepository bookRepository;
    private final PatronService  patronService;

    public RecommendationService(BookRepository bookRepository, PatronService patronService) {
        this.bookRepository = bookRepository;
        this.patronService  = patronService;
    }

    /**
     * Returns up to {@code limit} recommended books for the given patron.
     *
     * @param patronId patron to generate recommendations for
     * @param limit    maximum number of results
     * @return ranked list of recommended books
     */
    public List<Book> recommend(String patronId, int limit) {
        Patron patron = patronService.getPatron(patronId);

        Set<String> alreadyRead = new HashSet<>(patron.getBorrowingHistory());
        List<String> history    = patron.getBorrowingHistory();

        if (history.isEmpty()) {
            log.info("No borrowing history for patron " + patronId + " — returning popular books.");
            return fallbackToPopular(alreadyRead, limit);
        }

        // ── Build genre frequency map ─────────────────────────────────────────
        Map<Genre, Long> genreFrequency = new HashMap<>();
        Set<String> readAuthors = new HashSet<>();

        for (String isbn : history) {
            bookRepository.findByIsbn(isbn).ifPresent(b -> {
                if (b.getGenre() != null) {
                    genreFrequency.merge(b.getGenre(), 1L, Long::sum);
                }
                readAuthors.add(b.getAuthor());
            });
        }

        // ── Score and rank candidates ─────────────────────────────────────────
        Map<Book, Double> scored = new HashMap<>();

        for (Book candidate : bookRepository.findAll()) {
            if (alreadyRead.contains(candidate.getIsbn())) continue;
            if (!candidate.isAvailable()) continue;

            double score = 0.0;

            // Genre affinity: each match adds its relative frequency weight
            if (candidate.getGenre() != null) {
                long freq = genreFrequency.getOrDefault(candidate.getGenre(), 0L);
                score += freq * 2.0;  // genre weight
            }

            // Author affinity bonus
            if (readAuthors.contains(candidate.getAuthor())) {
                score += 3.0;
            }

            if (score > 0) {
                scored.put(candidate, score);
            }
        }

        List<Book> recommendations = scored.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info(String.format("Recommendations for patron %s: %d book(s)", patronId, recommendations.size()));
        return recommendations;
    }

    /** Convenience overload with default limit. */
    public List<Book> recommend(String patronId) {
        return recommend(patronId, DEFAULT_LIMIT);
    }

    /**
     * Fallback when a patron has no borrowing history.
     * Returns the first {@code limit} available books in catalogue order.
     */
    private List<Book> fallbackToPopular(Set<String> exclude, int limit) {
        return bookRepository.findAll().stream()
                .filter(b -> b.isAvailable() && !exclude.contains(b.getIsbn()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
