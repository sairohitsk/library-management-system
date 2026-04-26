package com.library.model;

import java.util.*;

/**
 * Represents a single physical branch of the library network.
 * Maintains its own inventory of books (by ISBN).
 */
public class LibraryBranch {

    private final String branchId;
    private String branchName;
    private String address;

    // ISBN → Book held at this branch
    private final Map<String, Book> inventory;

    public LibraryBranch(String branchId, String branchName, String address) {
        this.branchId   = Objects.requireNonNull(branchId);
        this.branchName = Objects.requireNonNull(branchName);
        this.address    = address;
        this.inventory  = new HashMap<>();
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getBranchId()   { return branchId; }
    public String getBranchName() { return branchName; }
    public String getAddress()    { return address; }

    public void setBranchName(String name)   { this.branchName = name; }
    public void setAddress(String address)   { this.address = address; }

    // ── Inventory operations ──────────────────────────────────────────────────

    public void addBook(Book book) {
        inventory.put(book.getIsbn(), book);
        book.setBranchId(this.branchId);
    }

    public boolean removeBook(String isbn) {
        return inventory.remove(isbn) != null;
    }

    public Optional<Book> findBook(String isbn) {
        return Optional.ofNullable(inventory.get(isbn));
    }

    public Collection<Book> getAllBooks() {
        return Collections.unmodifiableCollection(inventory.values());
    }

    public boolean hasBook(String isbn) {
        return inventory.containsKey(isbn);
    }

    public long availableCount() {
        return inventory.values().stream().filter(Book::isAvailable).count();
    }

    @Override
    public String toString() {
        return String.format("LibraryBranch{id='%s', name='%s', books=%d}",
                branchId, branchName, inventory.size());
    }
}
