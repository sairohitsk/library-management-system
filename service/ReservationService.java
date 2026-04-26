package com.library.service;

import com.library.model.Book;
import com.library.model.Patron;
import com.library.model.Reservation;
import com.library.model.enums.BookStatus;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Manages the reservation queue for unavailable books.
 *
 * <p>When a book is returned (status → AVAILABLE), this service is responsible
 * for checking the queue and notifying the next waiting patron via the
 * Observer/event mechanism.
 */
public class ReservationService {

    private static final Logger log = Logger.getLogger(ReservationService.class.getName());

    /** How many days a reservation stays active before it auto-expires. */
    private static final int RESERVATION_EXPIRY_DAYS = 7;

    private final ReservationRepository reservationRepository;
    private final BookRepository        bookRepository;
    private final PatronService         patronService;
    private final LibraryEventPublisher publisher;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              PatronService patronService,
                              LibraryEventPublisher publisher) {
        this.reservationRepository = reservationRepository;
        this.bookRepository        = bookRepository;
        this.patronService         = patronService;
        this.publisher             = publisher;
    }

    // ── Reserve ───────────────────────────────────────────────────────────────

    /**
     * Places a reservation for a patron on a given book.
     * Validates that the patron doesn't already have an active reservation
     * for the same book.
     */
    public Reservation reserveBook(String isbn, String patronId) {
        patronService.getPatron(patronId);  // validate patron exists

        if (reservationRepository.existsActiveForPatronAndIsbn(patronId, isbn)) {
            throw new IllegalStateException(
                    "Patron " + patronId + " already has an active reservation for ISBN: " + isbn);
        }

        // If the book is somehow available, suggest checkout instead
        bookRepository.findByIsbn(isbn).ifPresent(b -> {
            if (b.isAvailable()) {
                throw new IllegalStateException(
                        "Book " + isbn + " is currently AVAILABLE — please check it out directly.");
            }
        });

        LocalDate today  = LocalDate.now();
        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(), isbn, patronId,
                today, today.plusDays(RESERVATION_EXPIRY_DAYS));
        reservationRepository.save(reservation);

        log.info(String.format("Reservation created: patron=%s, isbn=%s", patronId, isbn));
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.BOOK_RESERVED, isbn, patronId, null,
                "Reservation created. Expires: " + reservation.getExpiryDate()));

        return reservation;
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    public void cancelReservation(String reservationId, String patronId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Reservation not found: " + reservationId));

        if (!reservation.getPatronId().equals(patronId)) {
            throw new IllegalStateException("Patron " + patronId
                    + " does not own reservation " + reservationId);
        }

        reservation.cancel();
        log.info("Reservation cancelled: " + reservationId);
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.RESERVATION_CANCELLED,
                reservation.getIsbn(), patronId, null, "Reservation cancelled"));
    }

    // ── Fulfil next in queue ──────────────────────────────────────────────────

    /**
     * Called after a book is returned to check if any patron is waiting.
     * If so, fulfils the oldest active reservation and notifies the patron.
     *
     * @param isbn     the returned book's ISBN
     * @param branchId the branch where the book is now available
     */
    public void checkAndFulfilReservation(String isbn, String branchId) {
        List<Reservation> queue = reservationRepository.findActiveByIsbn(isbn);
        if (queue.isEmpty()) return;

        Reservation next = queue.get(0);
        next.fulfil();

        // Mark the book as reserved so it is held for this patron
        bookRepository.findByIsbn(isbn).ifPresent(b -> {
            b.setStatus(BookStatus.RESERVED);
            bookRepository.save(b);
        });

        log.info(String.format("Reservation fulfilled: patron=%s, isbn=%s", next.getPatronId(), isbn));
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.RESERVATION_FULFILLED, isbn, next.getPatronId(), branchId,
                "Your reservation is ready for pickup!"));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Reservation> getActiveReservationsForPatron(String patronId) {
        return reservationRepository.findActiveByPatronId(patronId);
    }

    public List<Reservation> getQueueForBook(String isbn) {
        return reservationRepository.findActiveByIsbn(isbn);
    }
}
