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

    static String SERVER1_NAME;
    static String SERVER1_IP;
    static int SERVER1_PORT;
    static String SERVER2_NAME;
    static String SERVER2_IP;
    static int SERVER2_PORT;
    static String SERVER3_NAME;
    static String SERVER3_IP;
    static int SERVER3_PORT;
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        ServerConfig serverConfig = new ServerConfig();
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
        // Set target server names
        mainLobbyController.SetServerNames(serverConfig);
        stage.show();
        MainNet mainNet = new MainNet(mainLobbyController);
        Thread mainNetThread = new Thread(mainNet);
        mainNetThread.start();
//        mainNetThread.join();


    }

    public static void main(String[] args) {
        launch();
    }

}