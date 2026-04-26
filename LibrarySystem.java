package com.library;

import com.library.observer.AuditLogObserver;
import com.library.observer.LibraryEventPublisher;
import com.library.observer.PatronNotificationObserver;
import com.library.repository.*;
import com.library.service.*;

/**
 * Top-level façade and composition root for the Library Management System.
 *
 * <p>Implemented as a Singleton so that the entire application shares a single
 * consistent state. Wires all repositories, services, and observers together
 * following the Dependency Inversion principle (high-level modules depend on
 * abstractions, not concrete implementations).
 *
 * <p><b>Design patterns present in this class:</b>
 * <ul>
 *   <li>Singleton — single shared instance</li>
 *   <li>Facade — hides assembly complexity from {@link LibrarySystemDemo}</li>
 * </ul>
 */
public class LibrarySystem {

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static volatile LibrarySystem instance;

    public static LibrarySystem getInstance() {
        if (instance == null) {
            synchronized (LibrarySystem.class) {
                if (instance == null) {
                    instance = new LibrarySystem();
                }
            }
        }
        return instance;
    }

    // ── Observers ─────────────────────────────────────────────────────────────

    private final LibraryEventPublisher publisher;
    private final AuditLogObserver      auditLog;

    // ── Repositories ─────────────────────────────────────────────────────────

    private final BookRepository        bookRepository;
    private final PatronRepository      patronRepository;
    private final LendingRepository     lendingRepository;
    private final ReservationRepository reservationRepository;

    // ── Services ──────────────────────────────────────────────────────────────

    private final BranchService         branchService;
    private final BookService           bookService;
    private final PatronService         patronService;
    private final LendingService        lendingService;
    private final ReservationService    reservationService;
    private final RecommendationService recommendationService;

    // ── Constructor (private — use getInstance()) ──────────────────────────────

    private LibrarySystem() {

        // 1. Publisher first (observers need it, services need it)
        publisher = new LibraryEventPublisher();
        auditLog  = new AuditLogObserver();
        publisher.subscribe(auditLog);
        publisher.subscribe(new PatronNotificationObserver());

        // 2. Repositories (pure data stores — no dependencies)
        bookRepository        = new BookRepository();
        patronRepository      = new PatronRepository();
        lendingRepository     = new LendingRepository();
        reservationRepository = new ReservationRepository();

        // 3. Services (depend on repositories and publisher)
        branchService      = new BranchService(bookRepository, publisher);
        patronService      = new PatronService(patronRepository, lendingRepository, publisher);
        bookService        = new BookService(bookRepository, branchService, publisher);
        lendingService     = new LendingService(bookRepository, patronRepository,
                                                lendingRepository, patronService, publisher);
        reservationService = new ReservationService(reservationRepository, bookRepository,
                                                    patronService, publisher);
        recommendationService = new RecommendationService(bookRepository, patronService);
    }

    // ── Public accessors (expose services, not internals) ─────────────────────

    public BranchService         getBranchService()         { return branchService; }
    public BookService           getBookService()           { return bookService; }
    public PatronService         getPatronService()         { return patronService; }
    public LendingService        getLendingService()        { return lendingService; }
    public ReservationService    getReservationService()    { return reservationService; }
    public RecommendationService getRecommendationService() { return recommendationService; }
    public AuditLogObserver      getAuditLog()              { return auditLog; }
}
