package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.packets.GameRooms;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

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
        launch();
    }
}