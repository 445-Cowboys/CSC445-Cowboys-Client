package com.csc445cowboys.guiwip.Controllers;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public final class Alerts {

    public static void displayAlert(String title, String content, Alert.AlertType alertType) {
            Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(content);

            // Show the alert
            alert.show();
        });
    }

}
