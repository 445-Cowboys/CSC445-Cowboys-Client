package com.csc445cowboys.guiwip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("battle.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        HelloController controller = fxmlLoader.getController();
        controller.setPlayer1Fields();
        controller.setPlayer2Fields();
        controller.setPlayer3Fields();
        controller.setBossFields();
        controller.setBossImage("file:src/main/resources/com/csc445cowboys/guiwip/img/boss_angel.jpeg");
        controller.setPlayer1Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_grit.jpeg");
        controller.setPlayer2Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_jango.jpeg");
        controller.setPlayer3Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_no_name.jpeg");

    }

    public static void main(String[] args) {
        launch();
    }
}