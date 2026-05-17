# Error Handling and Validation

## Validation Strategy

Validation is implemented mainly in the service layer. This keeps business rules independent from JavaFX controls and allows controllers to remain focused on interface behavior.

## Input Checking

Examples from the actual code:

- `UserService.createUser(...)` checks full name, email format, password length, and duplicate email.
- `LibraryService.addBook(...)` checks title, author, ISBN, category, year, quantity, and duplicate ISBN.
- `LibraryService.borrowBook(...)` checks selected book, selected member, member role, borrowing limit, duplicate borrowing, and availability.
- `LibraryService.returnBook(...)` checks that a borrowing record is selected and not already returned.

## Database Exception Handling

DAO classes throw `SQLException` when MySQL operations fail. Service classes catch these exceptions and convert them into user-readable runtime messages. This avoids exposing raw SQL details to the user interface while preserving meaningful debugging information.

Example behavior:

- If MySQL is unavailable, `UserService.createUser(...)` raises a clear message explaining that the action cannot be saved.
- If a duplicate ISBN violates a database constraint, the user receives an error dialog.

## User Alerts

The utility class `UiUtil` centralizes alerts:

- `showInfo(...)` for success and information.
- `showError(...)` for validation or database problems.
- `confirm(...)` for logout and destructive actions.
- `toast(...)` for temporary success feedback.

## Confirmation Dialogs

Confirmation dialogs are used before:

- Logout.
- Deleting a book from the catalog.

This reduces accidental destructive actions.

## Notifications

The notification module stores and displays user messages such as:

- Login success.
- Logout confirmation.
- Book issued.
- Broadcast overdue reminder.
- Reservation status changes.

Notifications are persisted in the `notifications` table when MySQL is connected.

