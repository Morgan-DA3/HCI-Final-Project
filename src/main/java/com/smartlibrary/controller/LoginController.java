package com.smartlibrary.controller;

import com.smartlibrary.model.User;
import com.smartlibrary.service.LibraryService;
import com.smartlibrary.service.NotificationService;
import com.smartlibrary.service.ReportService;
import com.smartlibrary.service.UserService;
import com.smartlibrary.util.SessionManager;
import com.smartlibrary.util.UiUtil;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class LoginController {
    private final Stage stage;
    private final UserService userService;
    private final LibraryService libraryService;
    private final NotificationService notificationService;
    private final ReportService reportService;
    private final SessionManager sessionManager;
    private final StackPane root = new StackPane();
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private Label statusLabel;

    public LoginController(Stage stage, UserService userService, LibraryService libraryService,
                           NotificationService notificationService, ReportService reportService,
                           SessionManager sessionManager) {
        this.stage = stage;
        this.userService = userService;
        this.libraryService = libraryService;
        this.notificationService = notificationService;
        this.reportService = reportService;
        this.sessionManager = sessionManager;
        build();
    }

    public Parent getView() {
        return root;
    }

    private void build() {
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("login-shell");

        VBox hero = new VBox(18);
        hero.setPadding(new Insets(52));
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("login-hero");
        Label badge = new Label("HCI Final Project");
        badge.getStyleClass().add("badge");
        Label title = new Label("Smart Library\nManagement System");
        title.getStyleClass().add("login-title");
        Label subtitle = new Label("A role-based desktop system for catalog management, circulation workflows, analytics, notifications, fines, and academic reporting.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("login-subtitle");
        VBox demo = new VBox(8,
                new Label("Demo accounts"),
                new Label("Admin: admin@library.edu / admin123"),
                new Label("Librarian: librarian@library.edu / lib123"),
                new Label("Member: student@library.edu / student123"));
        demo.getStyleClass().add("demo-box");
        hero.getChildren().addAll(badge, title, subtitle, demo);

        VBox form = new VBox(18);
        form.setPadding(new Insets(44));
        form.setMaxWidth(430);
        form.setAlignment(Pos.CENTER);
        form.getStyleClass().add("login-card");
        Label formTitle = new Label("Secure sign in");
        formTitle.getStyleClass().add("section-title");
        TextField emailField = new TextField("admin@library.edu");
        emailField.setPromptText("Email address");
        emailField.getStyleClass().add("input");
        passwordField = new PasswordField();
        passwordField.setText("admin123");
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input");
        visiblePasswordField = new TextField();
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());
        visiblePasswordField.getStyleClass().add("input");
        StackPane passwordStack = new StackPane(passwordField, visiblePasswordField);
        CheckBox showPassword = new CheckBox("Show password");
        showPassword.selectedProperty().addListener((obs, oldValue, show) -> {
            passwordField.setVisible(!show);
            passwordField.setManaged(!show);
            visiblePasswordField.setVisible(show);
            visiblePasswordField.setManaged(show);
        });
        CheckBox rememberMe = new CheckBox("Remember me");
        HBox options = new HBox(18, rememberMe, showPassword);
        options.setAlignment(Pos.CENTER_LEFT);
        Button loginButton = new Button("Sign in");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");
        form.getChildren().addAll(formTitle, emailField, passwordStack, options, loginButton, statusLabel);

        loginButton.setOnAction(event -> authenticate(emailField.getText(), passwordField.getText(), loginButton));
        passwordField.setOnAction(event -> authenticate(emailField.getText(), passwordField.getText(), loginButton));
        visiblePasswordField.setOnAction(event -> authenticate(emailField.getText(), visiblePasswordField.getText(), loginButton));

        shell.setLeft(hero);
        shell.setCenter(form);
        root.getChildren().setAll(shell);
        UiUtil.fadeIn(shell);
    }

    private void authenticate(String email, String password, Button loginButton) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }
        loginButton.setDisable(true);
        loginButton.setText("Checking access...");
        PauseTransition pause = new PauseTransition(Duration.millis(520));
        pause.setOnFinished(event -> {
            Optional<User> user = userService.authenticate(email, password);
            if (user.isPresent()) {
                sessionManager.login(user.get());
                notificationService.notify(user.get(), "Login successful", "Welcome back, " + user.get().getFullName(), "SUCCESS");
                MainController mainController = new MainController(stage, userService, libraryService, notificationService, reportService, sessionManager);
                Scene scene = stage.getScene();
                scene.setRoot(mainController.getView());
            } else {
                statusLabel.setText("Invalid credentials or inactive account.");
                loginButton.setDisable(false);
                loginButton.setText("Sign in");
            }
        });
        pause.play();
    }
}
