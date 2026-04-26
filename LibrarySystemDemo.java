package com.library;

import com.library.factory.BookFactory;
import com.library.factory.PatronFactory;
import com.library.model.*;
import com.library.model.enums.Genre;
import com.library.service.*;

import java.util.List;
import java.util.logging.*;

/**
 * End-to-end demonstration of the Library Management System.
 *
 * <p>Exercises every core requirement and optional extension:
 * <ol>
 *   <li>Book management (add / update / search)</li>
 *   <li>Patron management (register / update)</li>
 *   <li>Lending process (checkout / return)</li>
 *   <li>Inventory management (available vs borrowed)</li>
 *   <li>Multi-branch support (add branch / transfer book)</li>
 *   <li>Reservation system (reserve / auto-fulfil on return)</li>
 *   <li>Recommendation system</li>
 * </ol>
 */
public class LibrarySystemDemo {

    public static void main(String[] args) {

        // ── Logger setup ─────────────────────────────────────────────────────
        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);
        for (Handler h : root.getHandlers()) {
            h.setFormatter(new SimpleFormatter());
        }

        sep("LIBRARY MANAGEMENT SYSTEM — DEMO");

        LibrarySystem lib = LibrarySystem.getInstance();

        BookService          books        = lib.getBookService();
        PatronService        patrons      = lib.getPatronService();
        LendingService       lending      = lib.getLendingService();
        ReservationService   reservations = lib.getReservationService();
        RecommendationService recommend   = lib.getRecommendationService();
        BranchService        branches     = lib.getBranchService();

        // ════════════════════════════════════════════════════════════════
        // 1. ADD BRANCHES
        // ════════════════════════════════════════════════════════════════
        sep("1. BRANCH SETUP");

        LibraryBranch mainBranch  = new LibraryBranch("BR-001", "Main Branch",  "123 Library Ave");
        LibraryBranch eastBranch  = new LibraryBranch("BR-002", "East Branch",  "456 East St");

        branches.addBranch(mainBranch);
        branches.addBranch(eastBranch);
        System.out.println("Added: " + mainBranch);
        System.out.println("Added: " + eastBranch);

        // ════════════════════════════════════════════════════════════════
        // 2. ADD BOOKS  (Factory Pattern)
        // ════════════════════════════════════════════════════════════════
        sep("2. BOOK MANAGEMENT");

        Book b1 = books.addBook(BookFactory.create("ISBN-001", "Clean Code",
                "Robert C. Martin", 2008, Genre.TECHNOLOGY), "BR-001");
        Book b2 = books.addBook(BookFactory.create("ISBN-002", "The Pragmatic Programmer",
                "David Thomas", 1999, Genre.TECHNOLOGY), "BR-001");
        Book b3 = books.addBook(BookFactory.create("ISBN-003", "Dune",
                "Frank Herbert", 1965, Genre.SCIENCE_FICTION), "BR-001");
        Book b4 = books.addBook(BookFactory.create("ISBN-004", "1984",
                "George Orwell", 1949, Genre.FICTION), "BR-002");
        Book b5 = books.addBook(BookFactory.create("ISBN-005", "Design Patterns",
                "Gang of Four", 1994, Genre.TECHNOLOGY), "BR-001");

        System.out.println("Books added to catalogue: " + books.totalBookCount());

        // Search demos
        System.out.println("\n🔍 Search by title 'code': "   + books.searchByTitle("code"));
        System.out.println("🔍 Search by author 'Orwell': "  + books.searchByAuthor("Orwell"));
        System.out.println("🔍 Search by ISBN 'ISBN-003': "  + books.searchByIsbn("ISBN-003"));
        System.out.println("🔍 Search by genre TECHNOLOGY: " + books.searchByGenre("TECHNOLOGY"));

        // Update a book
        books.updateBook("ISBN-002", "The Pragmatic Programmer (20th Anniversary)", null, 2019);
        System.out.println("\n✏️ Updated: " + books.getBook("ISBN-002"));

        // ════════════════════════════════════════════════════════════════
        // 3. PATRON MANAGEMENT  (Factory Pattern)
        // ════════════════════════════════════════════════════════════════
        sep("3. PATRON MANAGEMENT");

        Patron alice = patrons.registerPatron(
                PatronFactory.create("Alice Singh", "alice@example.com", "+91-9000000001"));
        Patron bob = patrons.registerPatron(
                PatronFactory.create("Bob Kumar", "bob@example.com", "+91-9000000002"));
        Patron carol = patrons.registerPatron(
                PatronFactory.create("Carol Das", "carol@example.com", "+91-9000000003"));

        System.out.println("Registered: " + alice);
        System.out.println("Registered: " + bob);
        System.out.println("Registered: " + carol);

        patrons.updatePatron(carol.getPatronId(), "Carol Dutta", null, "+91-9000000099");
        System.out.println("\n✏️ Updated patron: " + patrons.getPatron(carol.getPatronId()));

        // ════════════════════════════════════════════════════════════════
        // 4. LENDING PROCESS
        // ════════════════════════════════════════════════════════════════
        sep("4. LENDING PROCESS");

        LendingRecord lr1 = lending.checkoutBook("ISBN-001", alice.getPatronId());
        LendingRecord lr2 = lending.checkoutBook("ISBN-003", alice.getPatronId());
        LendingRecord lr3 = lending.checkoutBook("ISBN-002", bob.getPatronId());

        System.out.println("Checked out: " + lr1);
        System.out.println("Checked out: " + lr2);
        System.out.println("Checked out: " + lr3);
        System.out.println("\nAlice active loans: " + alice.getActiveLoanIsbns());
        System.out.println("Book ISBN-001 status: " + books.getBook("ISBN-001").getStatus());

        // ════════════════════════════════════════════════════════════════
        // 5. RESERVATION SYSTEM  (Observer Pattern triggered on return)
        // ════════════════════════════════════════════════════════════════
        sep("5. RESERVATION SYSTEM");

        // Carol reserves Clean Code (currently borrowed by Alice)
        Reservation res = reservations.reserveBook("ISBN-001", carol.getPatronId());
        System.out.println("Reservation created: " + res);
        System.out.println("Queue for ISBN-001: " + reservations.getQueueForBook("ISBN-001").size() + " patron(s) waiting");

        // Alice returns the book → triggers reservation fulfilment notification
        System.out.println("\n↩️ Alice returns Clean Code ...");
        lending.returnBook("ISBN-001", alice.getPatronId());
        // Manually trigger reservation check (in a real system this would be
        // orchestrated by a post-return event handler / application coordinator)
        reservations.checkAndFulfilReservation("ISBN-001", b1.getBranchId());

        // ════════════════════════════════════════════════════════════════
        // 6. MULTI-BRANCH TRANSFER
        // ════════════════════════════════════════════════════════════════
        sep("6. MULTI-BRANCH TRANSFER");

        System.out.println("ISBN-004 currently at: " + books.getBook("ISBN-004").getBranchId());
        branches.transferBook("ISBN-004", "BR-002", "BR-001");
        System.out.println("ISBN-004 after transfer: " + books.getBook("ISBN-004").getBranchId());
        System.out.println("East branch available books: " + eastBranch.availableCount());
        System.out.println("Main branch total books: "     + mainBranch.getAllBooks().size());

        // ════════════════════════════════════════════════════════════════
        // 7. RECOMMENDATION SYSTEM
        // ════════════════════════════════════════════════════════════════
        sep("7. RECOMMENDATION SYSTEM");

        System.out.println("Alice's borrowing history: " + alice.getBorrowingHistory());
        List<Book> aliceRecs = recommend.recommend(alice.getPatronId());
        System.out.println("Recommendations for Alice:");
        aliceRecs.forEach(b -> System.out.println("  • " + b.getTitle() + " [" + b.getGenre() + "]"));

        List<Book> bobRecs = recommend.recommend(bob.getPatronId());
        System.out.println("\nRecommendations for Bob:");
        bobRecs.forEach(b -> System.out.println("  • " + b.getTitle() + " [" + b.getGenre() + "]"));

        // ════════════════════════════════════════════════════════════════
        // 8. INVENTORY SUMMARY
        // ════════════════════════════════════════════════════════════════
        sep("8. INVENTORY SUMMARY");

        System.out.println("Active loans:  " + lending.getActiveLoans().size());
        System.out.println("Overdue loans: " + lending.getOverdueLoans().size());

        System.out.println("\nBorrowing history — Alice: "
                + patrons.getBorrowingHistory(alice.getPatronId()).size() + " record(s)");
        System.out.println("Borrowing history — Bob:   "
                + patrons.getBorrowingHistory(bob.getPatronId()).size() + " record(s)");

        // ════════════════════════════════════════════════════════════════
        // 9. AUDIT LOG (Observer pattern in action)
        // ════════════════════════════════════════════════════════════════
        sep("9. AUDIT LOG");
        lib.getAuditLog().printAuditLog();

        sep("DEMO COMPLETE");
    }

    private static void sep(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.printf( "║  %-48s║%n", title);
        System.out.println("╚══════════════════════════════════════════════════╝\n");
    }
}
