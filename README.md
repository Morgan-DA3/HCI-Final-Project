# Smart Library Management System

Professional Java 17 + JavaFX + Maven + MySQL/JDBC desktop application built for an HCI final project.

## Highlights
- MVC-style package separation: `controller`, `model`, `dao`, `service`, `util`, `view`.
- Role-based authentication for Admin, Librarian, and Member.
- Modern JavaFX UI with sidebar navigation, dashboard cards, charts, dialogs, notifications, dark mode, and smooth transitions.
- Catalog management with search, filters, sorting-ready tables, cover upload path, QR-style book code, and CSV export.
- Borrowing workflow with due dates, reservation queue, duplicate prevention, max-book rule, returns, overdue detection, and fines.
- Academic artifacts: SQL schema, ER diagram, UML diagrams, requirements, and installation guide.

## Run
```bash
mvn clean javafx:run
```

## Demo Accounts
- Admin: `admin@library.edu` / `admin123`
- Librarian: `librarian@library.edu` / `lib123`
- Member: `student@library.edu` / `student123`

## MySQL
Run [schema.sql](src/main/resources/sql/schema.sql), then start with optional properties if needed:

```bash
mvn javafx:run -Dsmartlibrary.db.user=root -Dsmartlibrary.db.password=your_password
```

If MySQL is unavailable, the app runs with built-in presentation data.

## Persistence Map
- Members, librarians, admins: `users`
- Roles: `roles`
- Books: `books`
- Categories: `categories`
- Borrow/return operations: `borrowings`
- Reservations: `reservations`
- Late penalties: `fines`
- Alerts and email-simulation messages: `notifications`
- Audit trail: `activity_logs`
