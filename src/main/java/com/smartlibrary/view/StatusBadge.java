package com.smartlibrary.view;

import javafx.scene.control.Label;

/**
 * Reusable view component for compact status feedback in tables and panels.
 * It keeps the UI layer modular for the MVC structure required by the project.
 */
public class StatusBadge extends Label {
    public StatusBadge(String text, String tone) {
        super(text);
        getStyleClass().addAll("badge-view", "badge-" + tone);
    }
}
