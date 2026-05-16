package com.smartlibrary.service;

import com.smartlibrary.dao.*;
import com.smartlibrary.model.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryService {
    public static final int MAX_BORROWED_BOOKS = 3;
    public static final int DEFAULT_BORROW_DAYS = 14;
    public static final BigDecimal DAILY_FINE = new BigDecimal("2.00");

    private final BookDao bookDao;
    private final BorrowingDao borrowingDao;
    private final ReservationDao reservationDao;
    private final FineDao fineDao;
    private final ActivityLogDao activityLogDao;
    private final List<Book> books = new ArrayList<>();
    private final List<Borrowing> borrowings = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Fine> fines = new ArrayList<>();
    private final List<ActivityLog> logs = new ArrayList<>();
    private int nextBookId = 100;
    private int nextBorrowingId = 200;
    private int nextReservationId = 300;
    private int nextFineId = 400;
    private int nextLogId = 500;
    private boolean databaseMode;

    public LibraryService(DatabaseManager databaseManager) {
        this.bookDao = new BookDao(databaseManager);
        this.borrowingDao = new BorrowingDao(databaseManager);
        this.reservationDao = new ReservationDao(databaseManager);
        this.fineDao = new FineDao(databaseManager);
        this.activityLogDao = new ActivityLogDao(databaseManager);
        seedBooks();
        seedWorkflow();
        try {
            List<Book> databaseBooks = bookDao.findAll();
            if (!databaseBooks.isEmpty()) {
                books.clear();
                books.addAll(databaseBooks);
            }
            borrowings.clear();
            borrowings.addAll(borrowingDao.findAll());
            reservations.clear();
            reservations.addAll(reservationDao.findAll());
            fines.clear();
            fines.addAll(fineDao.findAll());
            logs.clear();
            logs.addAll(activityLogDao.findAll());
            databaseMode = true;
        } catch (SQLException ignored) {
            // Keeps presentation mode functional when MySQL is not running.
            databaseMode = false;
        }
    }

    public List<Book> searchBooks(String query, String category, String status) {
        String normalized = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        return books.stream()
                .filter(book -> normalized.isBlank()
                        || book.getTitle().toLowerCase(Locale.ROOT).contains(normalized)
                        || book.getAuthor().toLowerCase(Locale.ROOT).contains(normalized)
                        || book.getIsbn().toLowerCase(Locale.ROOT).contains(normalized))
                .filter(book -> category == null || category.equals("All") || book.getCategory().equals(category))
                .filter(book -> status == null || status.equals("All") || book.getStatus().equals(status))
                .sorted(Comparator.comparing(Book::getTitle))
                .collect(Collectors.toList());
    }

    public List<Book> allBooks() { return new ArrayList<>(books); }
    public List<Borrowing> allBorrowings() { return new ArrayList<>(borrowings); }
    public List<Reservation> allReservations() { return new ArrayList<>(reservations); }
    public List<Fine> allFines() { return new ArrayList<>(fines); }
    public List<ActivityLog> logs() { return new ArrayList<>(logs); }

    public List<String> categories() {
        List<String> categories = books.stream().map(Book::getCategory).distinct().sorted().collect(Collectors.toCollection(ArrayList::new));
        categories.add(0, "All");
        return categories;
    }

    public Book addBook(String title, String author, String isbn, String category, int year, int quantity, String shelf, String coverPath, String actor) {
        validateBook(title, author, isbn, category, year, quantity);
        if (books.stream().anyMatch(book -> book.getIsbn().equalsIgnoreCase(isbn))) {
            throw new IllegalArgumentException("Duplicate ISBN detected. Each book record must have a unique ISBN.");
        }
        Book book = new Book(nextBookId++, title, author, isbn, category, year, quantity, quantity, shelf, "Available", coverPath);
        if (databaseMode) {
            try {
                book = bookDao.save(book);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database insert failed for books. Check MySQL category/book constraints.", ex);
            }
        }
        books.add(book);
        log(actor, "BOOK_CREATED", title + " was added to catalog.");
        return book;
    }

    public void updateBook(Book book, String actor) {
        validateBook(book.getTitle(), book.getAuthor(), book.getIsbn(), book.getCategory(), book.getPublicationYear(), book.getQuantity());
        if (book.getAvailableCopies() < 0 || book.getAvailableCopies() > book.getQuantity()) {
            throw new IllegalArgumentException("Available copies must stay between zero and total quantity.");
        }
        book.setStatus(book.getAvailableCopies() > 0 ? "Available" : "Unavailable");
        if (databaseMode) {
            try {
                bookDao.update(book);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database update failed for books.", ex);
            }
        }
        log(actor, "BOOK_UPDATED", book.getTitle() + " was updated.");
    }

    public void deleteBook(Book book, String actor) {
        boolean borrowed = borrowings.stream().anyMatch(item -> item.getBookId() == book.getId() && item.getReturnDate() == null);
        if (borrowed) throw new IllegalArgumentException("This book cannot be deleted while borrowed.");
        if (databaseMode) {
            try {
                bookDao.deleteById(book.getId());
            } catch (SQLException ex) {
                throw new IllegalStateException("Database delete failed for books. Check related borrowings/reservations.", ex);
            }
        }
        books.remove(book);
        log(actor, "BOOK_DELETED", book.getTitle() + " was removed from catalog.");
    }

    public Borrowing borrowBook(Book book, User member, String actor) {
        if (member.getRole() != Role.MEMBER) throw new IllegalArgumentException("Books can only be issued to library members.");
        long activeBorrowings = borrowings.stream().filter(b -> b.getMemberId() == member.getId() && b.getReturnDate() == null).count();
        if (activeBorrowings >= MAX_BORROWED_BOOKS) throw new IllegalArgumentException("Borrowing limit reached. Maximum allowed books: " + MAX_BORROWED_BOOKS);
        boolean duplicate = borrowings.stream().anyMatch(b -> b.getMemberId() == member.getId() && b.getBookId() == book.getId() && b.getReturnDate() == null);
        if (duplicate) throw new IllegalArgumentException("Duplicate borrowing is not allowed for the same member and book.");
        if (book.getAvailableCopies() <= 0) throw new IllegalArgumentException("Book unavailable. Add the member to the reservation queue instead.");

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        book.setStatus(book.getAvailableCopies() > 0 ? "Available" : "Unavailable");
        Borrowing borrowing = new Borrowing(nextBorrowingId++, book.getId(), member.getId(), book.getTitle(), member.getFullName(),
                LocalDate.now(), LocalDate.now().plusDays(DEFAULT_BORROW_DAYS), null, "Borrowed");
        if (databaseMode) {
            try {
                borrowing = borrowingDao.save(borrowing, null);
                bookDao.updateAvailability(book);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database insert failed for borrowing.", ex);
            }
        }
        borrowings.add(borrowing);
        log(actor, "BOOK_BORROWED", member.getFullName() + " borrowed " + book.getTitle() + ".");
        return borrowing;
    }

    public void returnBook(Borrowing borrowing, String actor) {
        if (borrowing.getReturnDate() != null) throw new IllegalArgumentException("This borrowing is already returned.");
        borrowing.setReturnDate(LocalDate.now());
        borrowing.setStatus("Returned");
        books.stream().filter(book -> book.getId() == borrowing.getBookId()).findFirst().ifPresent(book -> {
            book.setAvailableCopies(Math.min(book.getQuantity(), book.getAvailableCopies() + 1));
            book.setStatus(book.getAvailableCopies() > 0 ? "Available" : "Unavailable");
            if (databaseMode) {
                try {
                    bookDao.updateAvailability(book);
                } catch (SQLException ex) {
                    throw new IllegalStateException("Database update failed for book availability.", ex);
                }
            }
        });
        long overdueDays = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());
        if (overdueDays > 0) {
            Fine fine = new Fine(nextFineId++, borrowing.getId(), borrowing.getMemberId(),
                    DAILY_FINE.multiply(BigDecimal.valueOf(overdueDays)), "Late return: " + overdueDays + " day(s)", "Unpaid", LocalDate.now());
            if (databaseMode) {
                try {
                    fine = fineDao.save(fine);
                } catch (SQLException ex) {
                    throw new IllegalStateException("Database insert failed for fines.", ex);
                }
            }
            fines.add(fine);
        }
        if (databaseMode) {
            try {
                borrowingDao.markReturned(borrowing);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database update failed for returned borrowing.", ex);
            }
        }
        reservations.stream()
                .filter(reservation -> reservation.getBookId() == borrowing.getBookId() && reservation.getStatus().equals("Waiting"))
                .findFirst()
                .ifPresent(reservation -> {
                    reservation.setStatus("Available");
                    if (databaseMode) {
                        try {
                            reservationDao.updateStatus(reservation);
                        } catch (SQLException ex) {
                            throw new IllegalStateException("Database update failed for reservation status.", ex);
                        }
                    }
                });
        log(actor, "BOOK_RETURNED", borrowing.getBookTitle() + " was returned by " + borrowing.getMemberName() + ".");
    }

    public Reservation reserveBook(Book book, User member, String actor) {
        boolean exists = reservations.stream().anyMatch(item -> item.getBookId() == book.getId() && item.getMemberId() == member.getId() && item.getStatus().equals("Waiting"));
        if (exists) throw new IllegalArgumentException("A reservation already exists for this member and book.");
        Reservation reservation = new Reservation(nextReservationId++, book.getId(), member.getId(), book.getTitle(), member.getFullName(), LocalDate.now(), "Waiting");
        if (databaseMode) {
            try {
                reservation = reservationDao.save(reservation);
            } catch (SQLException ex) {
                throw new IllegalStateException("Database insert failed for reservations.", ex);
            }
        }
        reservations.add(reservation);
        log(actor, "BOOK_RESERVED", member.getFullName() + " reserved " + book.getTitle() + ".");
        return reservation;
    }

    public List<Borrowing> currentBorrowingsFor(User user) {
        return borrowings.stream().filter(b -> b.getMemberId() == user.getId() && b.getReturnDate() == null).toList();
    }

    public List<Borrowing> historyFor(User user) {
        return borrowings.stream().filter(b -> b.getMemberId() == user.getId()).toList();
    }

    public List<Reservation> reservationsFor(User user) {
        return reservations.stream().filter(r -> r.getMemberId() == user.getId()).toList();
    }

    public List<Fine> finesFor(User user) {
        return fines.stream().filter(f -> f.getMemberId() == user.getId()).toList();
    }

    public DashboardStats dashboardStats(int activeUsers, int unreadNotifications) {
        int totalBooks = books.stream().mapToInt(Book::getQuantity).sum();
        int borrowed = (int) borrowings.stream().filter(item -> item.getReturnDate() == null).count();
        int overdue = (int) borrowings.stream().filter(item -> item.getReturnDate() == null && item.getDueDate().isBefore(LocalDate.now())).count();
        int today = (int) borrowings.stream().filter(item -> item.getBorrowDate().equals(LocalDate.now())).count();
        int reservationsCount = (int) reservations.stream().filter(r -> !r.getStatus().equals("Cancelled")).count();
        return new DashboardStats(totalBooks, borrowed, overdue, activeUsers, today, borrowed, unreadNotifications, reservationsCount);
    }

    public Map<String, Long> topCategories() {
        return books.stream().collect(Collectors.groupingBy(Book::getCategory, Collectors.counting()));
    }

    public void exportBooksCsv(Path path) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("title,author,isbn,category,year,quantity,available,shelf,status");
        for (Book book : books) {
            lines.add(String.join(",",
                    csv(book.getTitle()), csv(book.getAuthor()), csv(book.getIsbn()), csv(book.getCategory()),
                    String.valueOf(book.getPublicationYear()), String.valueOf(book.getQuantity()),
                    String.valueOf(book.getAvailableCopies()), csv(book.getShelfLocation()), csv(book.getStatus())));
        }
        Files.write(path, lines);
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void validateBook(String title, String author, String isbn, String category, int year, int quantity) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Book title is required.");
        if (author == null || author.isBlank()) throw new IllegalArgumentException("Author is required.");
        if (isbn == null || isbn.isBlank()) throw new IllegalArgumentException("ISBN is required.");
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Category is required.");
        if (year < 1400 || year > LocalDate.now().getYear() + 1) throw new IllegalArgumentException("Publication year is invalid.");
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be at least 1.");
    }

    public void log(String actor, String action, String details) {
        if (databaseMode) {
            try {
                activityLogDao.save(actor, action, details);
            } catch (SQLException ignored) {
                // Keep UI responsive even if log persistence fails.
            }
        }
        logs.add(0, new ActivityLog(nextLogId++, actor == null ? "System" : actor, action, details, java.time.LocalDateTime.now()));
    }

    public boolean isDatabaseMode() {
        return databaseMode;
    }

    private void seedBooks() {
        books.add(new Book(1, "Designing Interfaces", "Jenifer Tidwell", "9781492051961", "HCI", 2020, 5, 3, "A-12", "Available", ""));
        books.add(new Book(2, "Clean Code", "Robert C. Martin", "9780132350884", "Software Engineering", 2008, 4, 2, "B-04", "Available", ""));
        books.add(new Book(3, "Database System Concepts", "Silberschatz, Korth", "9780078022159", "Database", 2019, 3, 0, "C-02", "Unavailable", ""));
        books.add(new Book(4, "The Design of Everyday Things", "Don Norman", "9780465050659", "HCI", 2013, 6, 5, "A-07", "Available", ""));
        books.add(new Book(5, "Effective Java", "Joshua Bloch", "9780134685991", "Programming", 2018, 4, 4, "B-10", "Available", ""));
        books.add(new Book(6, "Refactoring", "Martin Fowler", "9780134757599", "Software Engineering", 2018, 2, 1, "B-11", "Available", ""));
    }

    private void seedWorkflow() {
        borrowings.add(new Borrowing(1, 1, 3, "Designing Interfaces", "Student Lina", LocalDate.now().minusDays(5), LocalDate.now().plusDays(9), null, "Borrowed"));
        borrowings.add(new Borrowing(2, 3, 4, "Database System Concepts", "Yassine Bennani", LocalDate.now().minusDays(20), LocalDate.now().minusDays(6), null, "Overdue"));
        borrowings.add(new Borrowing(3, 2, 5, "Clean Code", "Nora El Fassi", LocalDate.now().minusDays(18), LocalDate.now().minusDays(4), LocalDate.now().minusDays(1), "Returned"));
        reservations.add(new Reservation(1, 3, 3, "Database System Concepts", "Student Lina", LocalDate.now().minusDays(2), "Waiting"));
        fines.add(new Fine(1, 2, 4, new BigDecimal("12.00"), "Late return warning", "Unpaid", LocalDate.now().minusDays(1)));
        log("System", "SEED_DATA", "Demo data loaded for presentation mode.");
    }
}
