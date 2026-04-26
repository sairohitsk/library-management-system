package com.library.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Event publisher (Subject) in the Observer design pattern.
 *
 * <p>Services publish events here; registered observers react asynchronously.
 * Keeping the publisher decoupled means new observers (audit log, email notifier,
 * push notification) can be added without modifying any service.
 */
public class LibraryEventPublisher {

    private static final Logger log = Logger.getLogger(LibraryEventPublisher.class.getName());

    private final List<LibraryObserver> observers = new ArrayList<>();

    // ── Observer registration ─────────────────────────────────────────────────

    public void subscribe(LibraryObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.info("Observer registered: " + observer.getClass().getSimpleName());
        }
    }

    public void unsubscribe(LibraryObserver observer) {
        observers.remove(observer);
        log.info("Observer unregistered: " + observer.getClass().getSimpleName());
    }

    // ── Event publishing ──────────────────────────────────────────────────────

    public void publish(LibraryEvent event) {
        log.info("Publishing event: " + event);
        for (LibraryObserver observer : observers) {
            try {
                observer.onEvent(event);
            } catch (Exception ex) {
                // One bad observer must not block others
                log.severe("Observer " + observer.getClass().getSimpleName()
                        + " threw an exception: " + ex.getMessage());
            }
        }
    }
}
