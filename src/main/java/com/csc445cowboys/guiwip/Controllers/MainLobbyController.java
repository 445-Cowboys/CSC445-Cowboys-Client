package com.csc445cowboys.guiwip.Controllers;

import com.csc445cowboys.guiwip.Net.GameSession;
import com.csc445cowboys.guiwip.packets.GameRooms;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainLobbyController {


    static public GameSession gameSession;
    public Label lobby2_label;
    public Label lobby_label3;

    public GameSession getGameSession() {
        return gameSession;
    }
    private Scene scene;
    public BattleScreenController battleScreenController;

    public Label lobby1_curr_players_label;
    static int lobby1_curr_players;
    static int lobby2_curr_players;
    static int lobby3_curr_players;
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
        lobby2_curr_players = curr_players;
    }

    public void setLobby3(int curr_players, String game_status) {
        lobby3_curr_players_label.setText(String.valueOf(curr_players));
        lobby3_game_status_label.setText(game_status);
        lobby3_curr_players = curr_players;
    }

    public void setServerStatuses(String[] serverStatus) {
        System.out.println("Server Status: " + serverStatus);
    }

    public void JoinGame(ActionEvent actionEvent,int n) throws IOException, GeneralSecurityException {
        gameSession = new GameSession(battleScreenController,n);
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

    public String gameStatusFromN(int n){
        return switch (n) {
            case 0 -> "Server Offline";
            case 1 -> "Server Online";
            default -> "Unknown";
        };
    }

    public String roomStatusFromN(int n){
        return switch (n) {
            case 0 -> "Waiting for Players";
            case 1 -> "Game in Progress";
            case 2 -> "Game Full";
            default -> "Unknown";
        };
    }



    // TODO : Implement this method properly. takes in parse gamerooms datagram converted object
    public void setGameRooms(GameRooms gameRooms){

        int room1Status = gameRooms.getRoomStatus(1);
        int room2Status = gameRooms.getRoomStatus(2);
        int room3Status = gameRooms.getRoomStatus(3);
        int room1Players = gameRooms.getNumPlayers(1);
        int room2Players = gameRooms.getNumPlayers(2);
        int room3Players = gameRooms.getNumPlayers(3);

        int server1Status = gameRooms.getServerStatus(1);
        int server2Status = gameRooms.getServerStatus(2);
        int server3Status = gameRooms.getServerStatus(3);

    }

}
