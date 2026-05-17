# Testing Summary

## Verification Performed

The project was compiled successfully using:

```bash
mvn -q -DskipTests compile
```

## Functional Test Scenarios

| Scenario | Expected Result |
|---|---|
| Login with valid admin account | Admin dashboard opens. |
| Login with invalid password | Error message appears. |
| Add user with empty name | Validation error appears. |
| Add user with valid database connection | User is inserted into `users`. |
| Add duplicate email | Validation/constraint error appears. |
| Add book with duplicate ISBN | Validation error appears. |
| Borrow unavailable book | Error message appears. |
| Borrow book with available copies | Borrowing is inserted and available copies decrease. |
| Return overdue book | Return date is saved and a fine is generated. |
| Reserve book | Reservation is inserted into `reservations`. |
| Broadcast notification | Notification appears in notification list and database. |

## Remaining Manual Tests

- Verify connection using the exact local MySQL password.
- Open the application with Admin, Librarian, and Member accounts.
- Capture real screenshots from the JavaFX window for final PDF submission.

