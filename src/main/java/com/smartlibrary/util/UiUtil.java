package com.smartlibrary.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Optional;

public final class UiUtil {
    private UiUtil() {}

    public static void fadeIn(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.CANCEL, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(title);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void toast(StackPane host, String message, String styleClass) {
        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", styleClass);
        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        host.getChildren().add(toast);
        fadeIn(toast);
        PauseTransition wait = new PauseTransition(Duration.seconds(2.4));
        wait.setOnFinished(event -> host.getChildren().remove(toast));
        wait.play();
    }
}
