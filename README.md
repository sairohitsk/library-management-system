#  Library Management System

A fully-featured Library Management System implemented in Java, demonstrating clean OOP design,
SOLID principles, and multiple design patterns.

---

## Table of Contents

- [Features](#features)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Class Diagram](#class-diagram)
- [Design Patterns](#design-patterns)
- [SOLID Principles](#solid-principles)
- [OOP Concepts](#oop-concepts)
- [Tech Stack](#tech-stack)

---

## Features

### Core Requirements
| Feature | Description |
|---|---|
| **Book Management** | Add, remove, update books; search by title, author, ISBN, or genre |
| **Patron Management** | Register patrons, update profile, track borrowing history |
| **Lending Process** | Checkout (14-day window) and return with overdue detection |
| **Inventory Management** | Real-time status tracking per book and per branch |

### Optional Extensions
| Feature | Description |
|---|---|
| **Multi-Branch Support** | Add unlimited branches; transfer books between them |
| **Reservation System** | Queue-based reservations; auto-fulfilled on book return |
| **Recommendation System** | Genre + author affinity scoring from borrowing history |

---

## Getting Started

### Prerequisites
- Java 11 or higher (`java -version`)
- No external dependencies — pure Java standard library

### Compile & Run

```bash
# Clone the repo
git clone https://github.com/<your-username>/library-management-system.git
cd library-management-system

# Compile
find src -name "*.java" > sources.txt
javac -d out @sources.txt

# Run the end-to-end demo
java -cp out com.library.LibrarySystemDemo
```

The demo exercises every feature in sequence and prints a full audit log at the end.

---

## Project Structure

```
src/main/java/com/library/
│
├── LibrarySystem.java          # Singleton façade / composition root
├── LibrarySystemDemo.java      # End-to-end runnable demo
│
├── model/                      # Pure domain objects
│   ├── Book.java
│   ├── Patron.java
│   ├── LendingRecord.java
│   ├── Reservation.java
│   ├── LibraryBranch.java
│   └── enums/
│       ├── BookStatus.java     # AVAILABLE | BORROWED | RESERVED | …
│       └── Genre.java
│
├── factory/                    # Factory pattern
│   ├── BookFactory.java
│   └── PatronFactory.java
│
├── observer/                   # Observer pattern
│   ├── LibraryObserver.java         # Observer interface
│   ├── LibraryEvent.java            # Event value object
│   ├── LibraryEventPublisher.java   # Subject
│   ├── PatronNotificationObserver.java
│   └── AuditLogObserver.java
│
├── search/                     # Strategy pattern
│   ├── SearchStrategy.java          # Strategy interface
│   ├── SearchStrategies.java        # Title / Author / ISBN / Genre impls
│   └── SearchStrategyFactory.java   # Strategy factory
│
├── repository/                 # In-memory data stores
│   ├── BookRepository.java
│   ├── PatronRepository.java
│   ├── LendingRepository.java
│   └── ReservationRepository.java
│
├── service/                    # Business logic
│   ├── BookService.java
│   ├── BranchService.java
│   ├── PatronService.java
│   ├── LendingService.java
│   ├── ReservationService.java
│   └── RecommendationService.java
│
└── exception/                  # Domain exceptions
    ├── BookNotFoundException.java
    ├── BookNotAvailableException.java
    ├── PatronNotFoundException.java
    └── BranchNotFoundException.java
```

---

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         LibrarySystem (Singleton)                       │
│  ─────────────────────────────────────────────────────────────────────  │
│  - publisher: LibraryEventPublisher                                     │
│  - bookRepo / patronRepo / lendingRepo / reservationRepo                │
│  + getBookService() / getPatronService() / getLendingService() / ...    │
└──────────────────────────────┬──────────────────────────────────────────┘
                               │ wires
         ┌─────────────────────┼──────────────────────┐
         ▼                     ▼                      ▼
   ┌───────────┐        ┌────────────┐        ┌──────────────┐
   │BookService│        │PatronService│        │LendingService│
   │───────────│        │────────────│        │──────────────│
   │+addBook() │        │+register() │        │+checkout()   │
   │+remove()  │        │+update()   │        │+return()     │
   │+update()  │        │+getHistory │        │+getOverdue() │
   │+search*() │        └─────┬──────┘        └──────┬───────┘
   └─────┬─────┘              │                      │
         │ uses               │ uses                 │ uses
         ▼                    ▼                      ▼
   ┌───────────┐       ┌─────────────┐       ┌──────────────────┐
   │BookRepo   │       │PatronRepo   │       │LendingRepo       │
   └───────────┘       └─────────────┘       └──────────────────┘
         │                    │
         ▼                    ▼
   ┌─────────┐         ┌────────┐
   │  Book   │         │ Patron │
   │─────────│         │────────│
   │isbn     │         │patronId│
   │title    │         │name    │
   │author   │         │email   │
   │year     │         │phone   │
   │genre    │         │activeLoans[]
   │status   │         │history[]
   │branchId │         └────────┘
   └─────────┘
         ▲
         │ held by
   ┌─────────────┐
   │LibraryBranch│
   │─────────────│
   │branchId     │
   │branchName   │
   │inventory{}  │
   └─────────────┘

  ┌────────────────────────────────────────────────────────────────┐
  │                   OBSERVER PATTERN                             │
  │                                                                │
  │  LibraryEventPublisher          <<interface>>                  │
  │  ─────────────────────          LibraryObserver                │
  │  - observers: List<>       ◄────────────────────               │
  │  + subscribe(observer)          + onEvent(event)               │
  │  + unsubscribe(observer)                                       │
  │  + publish(event)          ┌────────────────────────┐          │
  │                            │PatronNotificationObserver│         │
  │                            ├────────────────────────┤          │
  │                            │AuditLogObserver        │          │
  │                            └────────────────────────┘          │
  └────────────────────────────────────────────────────────────────┘

  ┌──────────────────────────────────────────────────────────────┐
  │                   STRATEGY PATTERN (Search)                  │
  │                                                              │
  │  BookService ──uses──► SearchStrategyFactory                 │
  │                               │ creates                      │
  │                        <<interface>>                         │
  │                        SearchStrategy                        │
  │                        + search(books, query): List<Book>    │
  │                               ▲                              │
  │               ┌───────────────┼───────────────┐             │
  │       TitleSearch   AuthorSearch   ISBNSearch  GenreSearch   │
  └──────────────────────────────────────────────────────────────┘

  ┌──────────────────────────────────────────────────────────────┐
  │                   FACTORY PATTERN                            │
  │                                                              │
  │  BookFactory                  PatronFactory                  │
  │  ────────────                 ─────────────                  │
  │  + create(isbn,title,…)       + create(name,email,…)         │
  │       └──► Book.Builder            └──► Patron.Builder       │
  └──────────────────────────────────────────────────────────────┘

  ┌──────────────────────────────────────────────────────────────┐
  │                   RESERVATION FLOW                           │
  │                                                              │
  │  Patron ──reserve()──► ReservationService                   │
  │                               │                              │
  │                        Reservation (PENDING)                 │
  │                               │                              │
  │  Book returned ──────► checkAndFulfilReservation()           │
  │                               │                              │
  │                        Reservation (FULFILLED)               │
  │                               │                              │
  │                        publisher.publish(RESERVATION_FULFILLED)
  │                               │                              │
  │                        PatronNotificationObserver.onEvent()  │
  └──────────────────────────────────────────────────────────────┘
```

---

## Design Patterns

### 1. Observer Pattern
**Location:** `com.library.observer`

The `LibraryEventPublisher` (Subject) maintains a list of `LibraryObserver` subscribers.
Every significant state change (checkout, return, reservation fulfilled, transfer) publishes
a typed `LibraryEvent`. Two concrete observers are included:

- `PatronNotificationObserver` — simulates email/SMS notifications to patrons
- `AuditLogObserver` — maintains a full in-memory audit trail of every event

**Why:** Completely decouples business logic from cross-cutting concerns (notifications, logging).
New observers (push notifications, fraud detection) can be added without touching any service.

### 2. Strategy Pattern
**Location:** `com.library.search`

`SearchStrategy` defines the search contract. `SearchStrategyFactory` creates the correct
concrete strategy (title, author, ISBN, genre) at runtime. `BookService` never depends on
any concrete search class.

**Why:** Search algorithms can change or be extended independently; the service remains stable.

### 3. Factory Pattern
**Location:** `com.library.factory`

`BookFactory` and `PatronFactory` centralise object construction and auto-generate identifiers,
keeping creation logic out of services and models.

**Why:** Single place to enforce invariants, assign IDs, and apply defaults.

### 4. Builder Pattern
**Location:** `Book.Builder`, `Patron.Builder`

Used on the domain models where many optional fields exist. Prevents telescoping constructors
and keeps objects in a valid state at all times.

### 5. Singleton Pattern
**Location:** `LibrarySystem`

Thread-safe double-checked locking ensures one shared composition root.

### 6. Repository Pattern
**Location:** `com.library.repository`

All data access is mediated through typed repository classes, isolating persistence
(even in-memory) from business logic. Services depend on repositories, not raw maps.

---

## SOLID Principles

| Principle | Application |
|---|---|
| **S** — Single Responsibility | Each service owns exactly one domain slice (BookService ≠ LendingService ≠ ReservationService) |
| **O** — Open/Closed | New search strategies or observers extend behaviour without modifying existing classes |
| **L** — Liskov Substitution | Any `LibraryObserver` implementation can replace another without breaking the publisher |
| **I** — Interface Segregation | `LibraryObserver` has a single narrow method; `SearchStrategy` has a single narrow method |
| **D** — Dependency Inversion | Services depend on repository/observer *abstractions*, not concrete classes; wired in `LibrarySystem` |

---

## OOP Concepts

| Concept | Where |
|---|---|
| **Encapsulation** | Models expose only what's needed; mutable state protected via methods (e.g., `addActiveLoan`) |
| **Abstraction** | `LibraryObserver`, `SearchStrategy` abstract away implementation details |
| **Inheritance** | `BookNotAvailableException extends RuntimeException` and other domain exceptions |
| **Polymorphism** | `publisher.publish(event)` triggers different behaviour in each observer; `strategy.search()` executes different algorithms |

---

## Tech Stack

- **Language:** Java 21 (compatible with Java 11+)
- **Build:** No build tool required — pure `javac`
- **Logging:** `java.util.logging` (JUL) — zero external dependencies
- **Collections used:** `HashMap`, `ArrayList`, `LinkedHashMap`, `Optional`, `Stream API`
- **Dependencies:** None
