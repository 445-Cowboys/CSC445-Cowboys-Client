package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.charTemplates.AckPacket;
import com.csc445cowboys.guiwip.charTemplates.PacketFactory;
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

import static sun.security.util.Debug.args;

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
        // TODO SPIN UP THEAD TO LISTEN FOR CHANGES FOR LOBBY THREADS
        DatagramChannel client = DatagramChannel.open().bind(null);
        InetSocketAddress serverAddr = new InetSocketAddress("localhost", 7806);
        AckPacket ackPacket = new AckPacket(serverAddr.getAddress(), 7806, AckPacket.TYPE, 0);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(ackPacket.getDatagramPacket().getData());
        // Loop Continuously
        for(;;){
            client.receive(buffer);
            buffer.flip();
            // TODO CHECK TO SEE IF GAME ROOM PACKET IS RECEIVED
            if(buffer.get(1) == 1){
                GameRooms gameRooms = new GameRooms(buffer);
                battleScreenController.setGameRooms(gameRooms);
                break;
            }
        }


        // INITIAL REQUEST TO SERVER FOR LOBBY INFO

        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}