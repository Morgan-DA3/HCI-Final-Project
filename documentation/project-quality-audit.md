# Project Quality Audit

## Requirements Checklist

| Professor Requirement | Status | Explanation |
|---|---|---|
| JavaFX or Swing UI | Implemented | JavaFX is used throughout the desktop application. |
| MySQL database | Implemented | MySQL schema and JDBC DAOs are provided. |
| UML Use Case Diagram | Implemented | PlantUML file generated in `/uml`. |
| UML Class Diagram | Implemented | PlantUML file generated in `/uml`. |
| UML Sequence Diagrams | Implemented | Login, borrowing, return, add book, and user management diagrams generated. |
| At least two roles | Implemented | Three roles exist: Admin, Librarian, Member. |
| Content organization | Implemented | Sidebar, dashboards, tables, dialogs, and tabs organize content. |
| Good layouts | Implemented | JavaFX BorderPane, VBox, HBox, GridPane, TabPane, and TableView are used. |
| Space optimization | Implemented | Tables resize columns and forms are moved into dialogs. |
| Task decomposition | Implemented | Each major workflow has a separate screen/action. |
| Tabs | Improved | Tabs were added for users, member library, and reports. |
| Popup menus | Improved | Book catalog now contains a contextual menu. |
| Descriptions/tooltips | Improved | Navigation and key controls now include tooltips. |
| Animations/transitions | Implemented | Fade transitions and loading pauses are used. |
| Interactive interface | Implemented | Search, filters, tables, charts, dialogs, and controls are interactive. |
| Error handling | Implemented | Validation and database errors are shown through friendly alerts. |
| Notifications | Implemented | Toasts and notification records exist. |
| Success/error alerts | Implemented | `UiUtil` provides centralized alerts and confirmations. |

## Code Quality Review

### MVC Structure

The project follows a practical MVC-oriented academic architecture:

- `model`: entity classes such as `Book`, `User`, `Borrowing`, `Fine`.
- `dao`: JDBC database access classes.
- `service`: business logic and validation.
- `controller`: JavaFX screen orchestration.
- `util`: reusable utilities.
- `view`: reusable visual components.

### Naming Conventions

Classes use clear names such as `LibraryService`, `UserDao`, `NotificationService`, and `SessionManager`. Method names are mostly action-oriented and readable.

### Database Access

Database operations are separated into DAO classes. This is a positive design choice because SQL code is not mixed directly into the JavaFX controllers.

### Reusability

Reusable helpers exist for:

- Password hashing.
- Session management.
- Alerts, confirmations, transitions, and toasts.
- Status badge component.

### Separation of Concerns

Business rules are mostly in service classes. UI classes call service methods and handle display feedback. This separation is appropriate for a beginner-to-intermediate academic project.

### JavaFX Practices

The UI uses standard JavaFX controls: `BorderPane`, `VBox`, `HBox`, `GridPane`, `TableView`, `TabPane`, `Dialog`, `ComboBox`, charts, tooltips, and context menus. Styling is centralized in CSS.

### Security Concerns

Passwords are hashed using SHA-256. For a production system, a salted password hashing algorithm such as BCrypt or Argon2 would be recommended. For an academic desktop project, the current approach is acceptable if documented as a simplification.

### Realistic Suggestions

- Add unit tests for service-layer business rules.
- Add pagination for very large book catalogs.
- Replace SHA-256 with BCrypt for stronger password security.
- Add database migration tooling for future schema changes.
- Add real PDF library support if richer reports are required.

