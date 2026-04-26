package com.library.exception;

public class BookNotAvailableException extends RuntimeException {
    public BookNotAvailableException(String isbn) {
        super("Book is not available for checkout. ISBN: " + isbn);
    }
}
