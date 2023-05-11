package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.Net.MainNet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;

//7806
public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, InterruptedException, GeneralSecurityException {
        FXMLLoader mainLdr = new FXMLLoader(getClass().getResource("main.fxml")); // Load Main Menu FXML
        Parent mainMenuPane = mainLdr.load();  // Load Main Menu Pane
        Scene mainMenuScene = new Scene(mainMenuPane);  // Create Main Menu Scene

        FXMLLoader battle = new FXMLLoader(getClass().getResource("battle.fxml"));  // Load Battle Screen FXML
        Parent battlePane = battle.load();  // Load Battle Screen Pane
        Scene battleScene = new Scene(battlePane); // Create Battle Screen Scene


        MainLobbyController mainLobbyController = mainLdr.getController();  // Get Main Menu Controller
        mainLobbyController.appendToMainLobbyWriter("Starting 445 Cowboys Client...");
        mainLobbyController.appendToMainLobbyWriter("Yeehaw! Giddyup Cowboys!");

        mainLobbyController.setBattleScreen(battleScene);  // Set Battle Screen Scene
        BattleScreenController battleScreenController = battle.getController();  // Get Battle Screen Controller
        battleScreenController.setMainScreen(mainMenuScene);  // Set Main Menu Scene

        stage.setScene(mainMenuScene);  // Set Main Menu Scene to the Stage
        stage.setTitle("Main Menu");  // Set Stage Title
        stage.show();  // Show Stage
        MainNet mainNet = new MainNet(mainLobbyController,battleScreenController);  // Create MainNet instance with Main Menu Controller// Initialize Server Response Thread
        Thread mainNetThread = new Thread(mainNet); // Create MainNet Thread
        mainNetThread.start(); // Start MainNet Thread
    }

}