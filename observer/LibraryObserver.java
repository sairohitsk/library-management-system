package com.library.observer;

/**
 * Observer interface — part of the Observer design pattern.
 *
 * <p>Any component that needs to react to library events (e.g., book returned,
 * reservation fulfilled) implements this interface and registers with the
 * {@link LibraryEventPublisher}.
 */
public interface LibraryObserver {

    /**
     * Called by the publisher when a relevant event occurs.
     *
     * @param event the event payload carrying all relevant context
     */
    void onEvent(LibraryEvent event);
}
