package com.csc445cowboys.guiwip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader mainLdr = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent mainMenuPane  = mainLdr.load();
        Scene mainMenuScene = new Scene(mainMenuPane);

        FXMLLoader battle = new FXMLLoader(getClass().getResource("battle.fxml"));
        Parent battlePane = battle.load();
        Scene battleScene = new Scene(battlePane);

        MainLobbyController mainLobbyController = mainLdr.getController();
        mainLobbyController.setBattleScreen(battleScene);

        BattleScreenController battleScreenController = battle.getController();
        battleScreenController.setMainScreen(mainMenuScene);

        stage.setScene(mainMenuScene);
        stage.setTitle("Main Menu");
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}