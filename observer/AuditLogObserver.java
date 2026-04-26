package com.library.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Concrete observer that maintains an in-memory audit log of all library events.
 * Demonstrates how a second observer can be added without touching any service code.
 */
public class AuditLogObserver implements LibraryObserver {

    private static final Logger log = Logger.getLogger(AuditLogObserver.class.getName());

    private final List<LibraryEvent> eventLog = new ArrayList<>();

    @Override
    public void onEvent(LibraryEvent event) {
        eventLog.add(event);
        log.fine("AUDIT: " + event);
    }

    /** Returns an unmodifiable view of all captured events. */
    public List<LibraryEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    public void printAuditLog() {
        System.out.println("══════════════ AUDIT LOG ══════════════");
        eventLog.forEach(System.out::println);
        System.out.println("══════════════════════════════════════");
    }
}
