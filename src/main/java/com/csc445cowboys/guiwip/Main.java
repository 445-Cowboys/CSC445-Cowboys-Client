package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.Controllers.BattleScreenController;
import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.Net.MainNet;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

//7806
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

        battleScreenController.setMainLobbyController(mainLobbyController);
       mainLobbyController.setBattleScreenController(battleScreenController);

        stage.setScene(mainMenuScene);
        stage.setTitle("Main Menu");
        stage.show();
        MainNet mainNet = new MainNet(mainLobbyController);
        Thread mainNetThread = new Thread(mainNet);
        mainNetThread.start();


    }

    public static void main(String[] args) {
        LoadConfig(args);
        launch();
    }

    public static void LoadConfig(String[] args){
      //

        // get env vars
        // SERVER1_NAME="Moxie"
        // SERVER1_IP="https://moxie.cs.oswego.edu"
        // SERVER1_PORT=7806
        //
        // SERVER2_NAME="Pi"
        // SERVER2_IP="https://pi.cs.oswego.edu"
        // SERVER2_PORT=7806

        // SERVER3_NAME="Rho"
        // SERVER3_IP="https://rho.cs.oswego.edu"
        // SERVER3_PORT=7806

        // Array
        String[] serverNames = {
            "Moxie",
            "Pi",
            "Rho"
        };
        String[] serverIPs = {
            "https://moxie.cs.oswego.edu",
            "https://pi.cs.oswego.edu",
            "https://rho.cs.oswego.edu"
        };
        int[] serverPorts = {
            7806,
            7806,
            7806
        };
    }
}