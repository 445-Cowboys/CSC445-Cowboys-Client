package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.Net.MainNet;
import com.csc445cowboys.guiwip.Net.ServerConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

//7806
public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        ServerConfig serverConfig = new ServerConfig();
        FXMLLoader mainLdr = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent mainMenuPane = mainLdr.load();
        Scene mainMenuScene = new Scene(mainMenuPane);

        FXMLLoader battle = new FXMLLoader(getClass().getResource("battle.fxml"));
        Parent battlePane = battle.load();
        Scene battleScene = new Scene(battlePane);


        MainLobbyController mainLobbyController = mainLdr.getController();
        mainLobbyController.setBattleScreen(battleScene);
        mainLobbyController.appendToWriter("Starting 445 Cowboys Client...");
        mainLobbyController.appendToWriter("Yeehaw! Giddyup Cowboys!");

        BattleScreenController battleScreenController = battle.getController();
        battleScreenController.setMainScreen(mainMenuScene);

        battleScreenController.setMainLobbyController(mainLobbyController);
        mainLobbyController.setBattleScreenController(battleScreenController);

        stage.setScene(mainMenuScene);
        stage.setTitle("Main Menu");
        stage.show();
        // Start Round Robin for
        MainNet mainNet = new MainNet(mainLobbyController);
//        mainNet.sendAwakeLoop();
        Thread mainNetThread = new Thread(mainNet);
        mainNetThread.start();
    }

}