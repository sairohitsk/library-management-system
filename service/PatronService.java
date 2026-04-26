package com.library.service;

import com.library.exception.PatronNotFoundException;
import com.library.model.LendingRecord;
import com.library.model.Patron;
import com.library.observer.LibraryEvent;
import com.library.observer.LibraryEventPublisher;
import com.library.repository.LendingRepository;
import com.library.repository.PatronRepository;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles patron registration, profile updates, and borrowing-history queries.
 */
public class PatronService {

    private static final Logger log = Logger.getLogger(PatronService.class.getName());
    private static final int MAX_ACTIVE_LOANS = 5;

    private final PatronRepository     patronRepository;
    private final LendingRepository    lendingRepository;
    private final LibraryEventPublisher publisher;

    public PatronService(PatronRepository patronRepository,
                         LendingRepository lendingRepository,
                         LibraryEventPublisher publisher) {
        this.patronRepository  = patronRepository;
        this.lendingRepository = lendingRepository;
        this.publisher         = publisher;
    }

    // ── Registration ──────────────────────────────────────────────────────────

    public Patron registerPatron(Patron patron) {
        if (patronRepository.findByEmail(patron.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "A patron with email " + patron.getEmail() + " already exists.");
        }
        patronRepository.save(patron);
        log.info("Patron registered: " + patron);
        publisher.publish(new LibraryEvent(
                LibraryEvent.Type.PATRON_REGISTERED, null, patron.getPatronId(), null,
                "Patron registered: " + patron.getName()));
        return patron;
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public Patron updatePatron(String patronId, String newName, String newEmail, String newPhone) {
        Patron patron = getPatron(patronId);
        if (newName  != null) patron.setName(newName);
        if (newEmail != null) patron.setEmail(newEmail);
        if (newPhone != null) patron.setPhoneNumber(newPhone);
        patronRepository.save(patron);
        log.info("Patron updated: " + patron);
        return patron;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Patron getPatron(String patronId) {
        return patronRepository.findById(patronId)
                .orElseThrow(() -> new PatronNotFoundException(patronId));
    }

    public Collection<Patron> getAllPatrons() {
        return patronRepository.findAll();
    }

    public List<LendingRecord> getBorrowingHistory(String patronId) {
        getPatron(patronId); // validate existence
        return lendingRepository.findByPatronId(patronId);
    }

    public boolean canBorrow(String patronId) {
        Patron patron = getPatron(patronId);
        return patron.activeLoanCount() < MAX_ACTIVE_LOANS;
    }

    public int getMaxActiveLoans() { return MAX_ACTIVE_LOANS; }
}
