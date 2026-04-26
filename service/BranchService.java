package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.BranchNotFoundException;
import com.library.model.Book;
import com.library.model.LibraryBranch;
import com.library.model.enums.BookStatus;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;

import java.util.*;
import java.util.logging.Logger;

/**
 * Manages library branches and inter-branch book transfers.
 *
 * <p>Follows Single Responsibility: owns branch lifecycle and transfer logic only.
 */
public class BranchService {

    private static final Logger log = Logger.getLogger(BranchService.class.getName());

    private final Map<String, LibraryBranch> branches = new HashMap<>();
    private final BookRepository             bookRepository;
    private final LibraryEventPublisher      publisher;

    public BranchService(BookRepository bookRepository, LibraryEventPublisher publisher) {
        this.bookRepository = bookRepository;
        this.publisher      = publisher;
    }

    // ── Branch management ─────────────────────────────────────────────────────

    public LibraryBranch addBranch(LibraryBranch branch) {
        branches.put(branch.getBranchId(), branch);
        log.info("Branch added: " + branch);
        return branch;
    }

    public LibraryBranch getBranch(String branchId) {
        return Optional.ofNullable(branches.get(branchId))
                .orElseThrow(() -> new BranchNotFoundException(branchId));
    }

    public Collection<LibraryBranch> getAllBranches() {
        return Collections.unmodifiableCollection(branches.values());
    }

    // ── Book transfer ──────────────────────────────────────────────────────────

    /**
     * Transfers a book from one branch to another.
     * The book must be AVAILABLE at the source branch.
     *
     * @param isbn         the book to transfer
     * @param fromBranchId source branch
     * @param toBranchId   destination branch
     * @throws IllegalStateException if the book is not available (e.g., borrowed)
     */
    public void transferBook(String isbn, String fromBranchId, String toBranchId) {
        LibraryBranch from = getBranch(fromBranchId);
        LibraryBranch to   = getBranch(toBranchId);

        Book book = from.findBook(isbn)
                .orElseThrow(() -> new BookNotFoundException(isbn));

        if (!book.isAvailable()) {
            throw new IllegalStateException(
                    "Cannot transfer book that is not AVAILABLE. ISBN: " + isbn);
        }

        from.removeBook(isbn);
        to.addBook(book);
        book.setStatus(BookStatus.TRANSFERRED);   // updated post-transfer
        book.setStatus(BookStatus.AVAILABLE);     // immediately available at new branch
        book.setBranchId(toBranchId);

        log.info(String.format("Book %s transferred from branch %s to %s", isbn, fromBranchId, toBranchId));
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.BOOK_TRANSFERRED, isbn, null, toBranchId,
                "Transferred from " + fromBranchId + " to " + toBranchId));
    }

    /** Returns books held at a specific branch. */
    public Collection<Book> getBooksAtBranch(String branchId) {
        return getBranch(branchId).getAllBooks();
    }
}
