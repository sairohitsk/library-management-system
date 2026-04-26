package com.library.exception;

/** Thrown when a book is not found in the system. */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String isbn) {
        super("Book not found with ISBN: " + isbn);
    }
}
