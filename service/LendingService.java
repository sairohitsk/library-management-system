package com.library.service;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BookNotFoundException;
import com.library.model.Book;
import com.library.model.LendingRecord;
import com.library.model.Patron;
import com.library.model.enums.BookStatus;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.LendingRepository;
import com.library.repository.PatronRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Core lending operations: checkout and return.
 *
 * <p>Coordinates state changes across Book, Patron, and LendingRecord while
 * publishing events so observers (notifications, audit log) react independently.
 */
public class LendingService {

    private static final Logger log = Logger.getLogger(LendingService.class.getName());

    /** Default loan period in days. */
    public static final int LOAN_PERIOD_DAYS = 14;

    private final BookRepository        bookRepository;
    private final PatronRepository      patronRepository;
    private final LendingRepository     lendingRepository;
    private final PatronService         patronService;
    private final LibraryEventPublisher publisher;

    public LendingService(BookRepository bookRepository,
                          PatronRepository patronRepository,
                          LendingRepository lendingRepository,
                          PatronService patronService,
                          LibraryEventPublisher publisher) {
        this.bookRepository    = bookRepository;
        this.patronRepository  = patronRepository;
        this.lendingRepository = lendingRepository;
        this.patronService     = patronService;
        this.publisher         = publisher;
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    /**
     * Checks out a book to a patron.
     *
     * @param isbn      the ISBN of the book to borrow
     * @param patronId  the ID of the borrowing patron
     * @return          the created {@link LendingRecord}
     * @throws BookNotAvailableException if the book is not AVAILABLE
     * @throws IllegalStateException     if the patron has reached their loan limit
     */
    public LendingRecord checkoutBook(String isbn, String patronId) {
        Book   book   = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        Patron patron = patronService.getPatron(patronId);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(isbn);
        }
        if (!patronService.canBorrow(patronId)) {
            throw new IllegalStateException(
                    String.format("Patron %s has reached the maximum of %d active loans.",
                            patronId, patronService.getMaxActiveLoans()));
        }

        // Update book status
        book.setStatus(BookStatus.BORROWED);
        bookRepository.save(book);

        // Update patron active loans
        patron.addActiveLoan(isbn);
        patronRepository.save(patron);

        // Create lending record
        LocalDate today   = LocalDate.now();
        LocalDate dueDate = today.plusDays(LOAN_PERIOD_DAYS);
        LendingRecord record = new LendingRecord(
                UUID.randomUUID().toString(), isbn, patronId, today, dueDate);
        lendingRepository.save(record);

        log.info(String.format("Checkout: patron=%s, isbn=%s, due=%s", patronId, isbn, dueDate));
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.BOOK_CHECKED_OUT, isbn, patronId, book.getBranchId(),
                "Book checked out. Due: " + dueDate));

        return record;
    }

    // ── Return ────────────────────────────────────────────────────────────────

    /**
     * Returns a borrowed book.
     *
     * @param isbn     the ISBN of the book being returned
     * @param patronId the patron returning it
     * @return         the closed {@link LendingRecord}
     */
    public LendingRecord returnBook(String isbn, String patronId) {
        LendingRecord record = lendingRepository.findActiveByIsbn(isbn)
                .orElseThrow(() -> new IllegalStateException(
                        "No active loan found for ISBN: " + isbn));

        if (!record.getPatronId().equals(patronId)) {
            throw new IllegalStateException(
                    "Patron " + patronId + " did not borrow book " + isbn);
        }

        // Close the lending record
        record.markReturned(LocalDate.now());
        lendingRepository.save(record);

        // Update patron
        Patron patron = patronService.getPatron(patronId);
        patron.removeActiveLoan(isbn);
        patronRepository.save(patron);

        // Update book (ReservationService will further update if a reservation exists)
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));
        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);

        log.info(String.format("Return: patron=%s, isbn=%s, overdue=%s",
                patronId, isbn, record.isOverdue()));
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.BOOK_RETURNED, isbn, patronId, book.getBranchId(),
                "Book returned" + (record.isOverdue() ? " (OVERDUE)" : "")));

        return record;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<LendingRecord> getOverdueLoans() {
        return lendingRepository.findOverdue();
    }

    public List<LendingRecord> getActiveLoans() {
        return lendingRepository.findAllActive();
    }
}
