package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.packets.GameRooms;
import com.google.crypto.tink.Aead;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.Callable;

public class MainLobbyController {


        private Scene scene;
        public BattleScreenController battleScreenController;

    public Label lobby1_curr_players_label;
    private int lobby1_curr_players;
    private int lobby2_curr_players;
        private int lobby3_curr_players;
    public Label lobby1_game_status_label;
    public Label lobby2_curr_players_label1;
    public Label lobby2_game_status_label;
    public Label lobby3_game_status_label;
    public Label lobby3_curr_players_label;


    public void onLobby1EnterGame(ActionEvent actionEvent) throws IOException, GeneralSecurityException {

        System.out.println("Attempting to Enter Lobby 3...");
        if (checkFull(lobby1_curr_players)) {
            System.out.println("Lobby 1 is full");
            gameFullAlert();

        } else {
            System.out.println("Lobby 1 is not full");
            JoinGame(actionEvent,1);
        }
    }

    public void onLobby2EnterGame(ActionEvent actionEvent) throws IOException, GeneralSecurityException {
        System.out.println("Attempting to Enter Lobby 2...");
        if (checkFull(lobby2_curr_players)) {
            System.out.println("Lobby 2 is full");
            gameFullAlert();

        } else {
            System.out.println("Lobby 2 is not full");
            JoinGame(actionEvent,2);
        }
    }

    public void onLobby3EnterGame(ActionEvent actionEvent) throws IOException, GeneralSecurityException {
        System.out.println("Attempting to Enter Lobby 3...");
        if (checkFull(lobby3_curr_players)) {
            gameFullAlert();
            System.out.println("Lobby 3 is full");

        } else {
            System.out.println("Lobby 3 is not full");
            JoinGame(actionEvent,3);
        }
    }

    public void setLobby1(int curr_players, String game_status) {
        lobby1_curr_players_label.setText(String.valueOf(curr_players));
        lobby1_game_status_label.setText(game_status);
        lobby1_curr_players = curr_players;
    }

    public void setLobby2(int curr_players, String game_status) {
        lobby2_curr_players_label1.setText(String.valueOf(curr_players));
        lobby2_game_status_label.setText(game_status);
    }

    public void setLobby3(int curr_players, String game_status) {
        lobby3_curr_players_label.setText(String.valueOf(curr_players));
        lobby3_game_status_label.setText(game_status);
    }

    public void JoinGame(ActionEvent actionEvent,int n) throws IOException, GeneralSecurityException {
        GameSession gameSession = new GameSession(battleScreenController,n);
        OpenBattleScreen(actionEvent,n);
    }

    public boolean checkFull(int curr_players)  {
        return curr_players == 3;
    }

    public void setBattleScreen(Scene battle) {
        this.scene = battle;
    }

    public void OpenBattleScreen(ActionEvent actionEvent, int  l) throws IOException {
        Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setTitle("Battling: Game Room " + l);
        stage.setScene(scene);
    }

    public void setBattleScreenController(BattleScreenController battleScreenController) {
        this.battleScreenController = battleScreenController;

    }

    public void gameFullAlert(){
        Alerts.displayAlert("Game is full","The game you are trying to join is full. Please try again later.", Alert.AlertType.INFORMATION);
    }



    // TODO : Implement this method properly. takes in parse gamerooms datagram converted object
    public void setGameRooms(GameRooms gameRooms){
    }

}
