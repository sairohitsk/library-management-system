package com.library.observer;

import java.util.logging.Logger;

/**
 * Concrete observer that simulates sending a notification to a patron.
 *
 * <p>In production this would integrate with an email / SMS gateway.
 * Here it logs to console so the pattern is visible without external dependencies.
 */
public class PatronNotificationObserver implements LibraryObserver {

    private static final Logger log = Logger.getLogger(PatronNotificationObserver.class.getName());

    @Override
    public void onEvent(LibraryEvent event) {
        switch (event.getType()) {

            case RESERVATION_FULFILLED:
                log.info(String.format(
                        "📬 NOTIFICATION → Patron [%s]: Your reserved book (ISBN: %s) is now available! "
                      + "Please collect it from branch [%s].",
                        event.getPatronId(), event.getIsbn(), event.getBranchId()));
                break;

            case BOOK_RETURNED:
                log.info(String.format(
                        "✅ NOTIFICATION → Patron [%s]: Book (ISBN: %s) returned successfully.",
                        event.getPatronId(), event.getIsbn()));
                break;

            case RESERVATION_CANCELLED:
                log.info(String.format(
                        "❌ NOTIFICATION → Patron [%s]: Your reservation for ISBN: %s has been cancelled.",
                        event.getPatronId(), event.getIsbn()));
                break;

            default:
                // Not all events require patron notification
                break;
        }
    }
}
