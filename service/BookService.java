package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.model.Book;
import com.library.model.LibraryBranch;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.search.SearchStrategy;
import com.library.search.SearchStrategyFactory;

import java.util.List;
import java.util.logging.Logger;

/**
 * Handles all book-management operations: add, remove, update, search.
 *
 * <p>Single Responsibility: this service owns the book catalogue lifecycle only.
 * Lending and reservation logic live in their own services.
 */
public class BookService {

    private static final Logger log = Logger.getLogger(BookService.class.getName());

    private final BookRepository       bookRepository;
    private final BranchService        branchService;
    private final LibraryEventPublisher publisher;

    public BookService(BookRepository bookRepository,
                       BranchService branchService,
                       LibraryEventPublisher publisher) {
        this.bookRepository = bookRepository;
        this.branchService  = branchService;
        this.publisher      = publisher;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    /**
     * Adds a book to the master catalogue and assigns it to a branch.
     */
    public Book addBook(Book book, String branchId) {
        LibraryBranch branch = branchService.getBranch(branchId);
        bookRepository.save(book);
        branch.addBook(book);
        log.info("Book added: " + book);
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.BOOK_ADDED, book.getIsbn(), null, branchId,
                "Book added to branch " + branchId));
        return book;
    }

    /**
     * Removes a book from the catalogue and its current branch inventory.
     * Throws {@link BookNotFoundException} if the ISBN is unknown.
     */
    public void removeBook(String isbn) {
        Book book = getBook(isbn);
        branchService.getAllBranches()
                .stream()
                .filter(b -> b.hasBook(isbn))
                .findFirst()
                .ifPresent(b -> b.removeBook(isbn));
        bookRepository.delete(isbn);
        log.info("Book removed: " + isbn);
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.BOOK_REMOVED, isbn, null, null, "Book removed"));
    }

    /**
     * Updates mutable fields of an existing book (title, author, year, genre).
     * ISBN is immutable and cannot be changed.
     */
    public Book updateBook(String isbn, String newTitle, String newAuthor, int newYear) {
        Book book = getBook(isbn);
        if (newTitle  != null) book.setTitle(newTitle);
        if (newAuthor != null) book.setAuthor(newAuthor);
        if (newYear   >  0)   book.setPublicationYear(newYear);
        bookRepository.save(book);
        log.info("Book updated: " + book);
        return book;
    }

    // ── Search ────────────────────────────────────────────────────────────────

    public List<Book> searchByTitle(String query) {
        return executeSearch(SearchStrategyFactory.SearchType.TITLE, query);
    }

    public List<Book> searchByAuthor(String query) {
        return executeSearch(SearchStrategyFactory.SearchType.AUTHOR, query);
    }

    public List<Book> searchByIsbn(String isbn) {
        return executeSearch(SearchStrategyFactory.SearchType.ISBN, isbn);
    }

    public List<Book> searchByGenre(String genre) {
        return executeSearch(SearchStrategyFactory.SearchType.GENRE, genre);
    }

    /**
     * Executes a search using the Strategy pattern — the concrete algorithm
     * is selected at runtime through {@link SearchStrategyFactory}.
     */
    private List<Book> executeSearch(SearchStrategyFactory.SearchType type, String query) {
        SearchStrategy strategy = SearchStrategyFactory.create(type);
        List<Book> results = strategy.search(bookRepository.findAll(), query);
        log.info(String.format("Search [%s, query='%s'] → %d result(s)", type, query, results.size()));
        return results;
    }

    // ── Lookup helpers ────────────────────────────────────────────────────────

    public Book getBook(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    public List<Book> getAllBooks() {
        return List.copyOf(bookRepository.findAll());
    }

    public int totalBookCount() { return bookRepository.count(); }
}
