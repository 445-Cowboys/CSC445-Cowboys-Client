package com.csc445cowboys.guiwip;

import javafx.scene.control.Alert;

public final class Alerts {

    public static void displayAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);

        // Show the alert
        alert.show();

    }
}
