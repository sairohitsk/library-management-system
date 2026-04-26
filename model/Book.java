package com.library.model;

import com.library.model.enums.BookStatus;
import com.library.model.enums.Genre;

import java.util.Objects;

/**
 * Represents a book in the library system.
 * Encapsulates all book-related attributes and enforces invariants.
 */
public class Book {

    private final String isbn;         // Immutable unique identifier
    private String title;
    private String author;
    private int publicationYear;
    private Genre genre;
    private BookStatus status;
    private String branchId;           // Which branch currently holds the book

    private Book(Builder builder) {
        this.isbn            = builder.isbn;
        this.title           = builder.title;
        this.author          = builder.author;
        this.publicationYear = builder.publicationYear;
        this.genre           = builder.genre;
        this.status          = BookStatus.AVAILABLE;
        this.branchId        = builder.branchId;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getIsbn()            { return isbn; }
    public String getTitle()           { return title; }
    public String getAuthor()          { return author; }
    public int    getPublicationYear() { return publicationYear; }
    public Genre  getGenre()           { return genre; }
    public BookStatus getStatus()      { return status; }
    public String getBranchId()        { return branchId; }

    // ── Setters (mutable fields only) ────────────────────────────────────────

    public void setTitle(String title)                   { this.title = Objects.requireNonNull(title, "Title must not be null"); }
    public void setAuthor(String author)                 { this.author = Objects.requireNonNull(author, "Author must not be null"); }
    public void setPublicationYear(int publicationYear)  { this.publicationYear = publicationYear; }
    public void setGenre(Genre genre)                    { this.genre = genre; }
    public void setStatus(BookStatus status)             { this.status = Objects.requireNonNull(status, "Status must not be null"); }
    public void setBranchId(String branchId)             { this.branchId = branchId; }

    public boolean isAvailable() {
        return this.status == BookStatus.AVAILABLE;
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static class Builder {
        private final String isbn;
        private String title;
        private String author;
        private int publicationYear;
        private Genre genre = Genre.OTHER;
        private String branchId;

        public Builder(String isbn) {
            this.isbn = Objects.requireNonNull(isbn, "ISBN must not be null");
        }

        public Builder title(String title)                   { this.title = title;           return this; }
        public Builder author(String author)                 { this.author = author;         return this; }
        public Builder publicationYear(int year)             { this.publicationYear = year;  return this; }
        public Builder genre(Genre genre)                    { this.genre = genre;           return this; }
        public Builder branchId(String branchId)             { this.branchId = branchId;     return this; }

        public Book build() {
            Objects.requireNonNull(title,  "Title must not be null");
            Objects.requireNonNull(author, "Author must not be null");
            return new Book(this);
        }
    }

    // ── Object overrides ─────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return isbn.equals(((Book) o).isbn);
    }

    @Override
    public int hashCode() { return isbn.hashCode(); }

    @Override
    public String toString() {
        return String.format("Book{isbn='%s', title='%s', author='%s', year=%d, status=%s, branch=%s}",
                isbn, title, author, publicationYear, status, branchId);
    }
}
