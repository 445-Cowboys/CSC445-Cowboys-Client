package com.csc445cowboys.guiwip.Controllers;

import com.csc445cowboys.guiwip.Net.MainNet;
import com.csc445cowboys.guiwip.Net.PacketHandler;
import com.csc445cowboys.guiwip.packets.GameRooms;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainLobbyController {
    static int lobby1_curr_players;
    static int lobby2_curr_players;
    static int lobby3_curr_players;
    public Label server1_name_label;
    public Label main_server_status_label;
    public Label server2_name_label;
    public Label server3_name_label;
    public static Label serve3_status_label;
    public static Label server2_status_label;
    public static Label server1_status_label;
    public Label players_in_game_label;
    public Label player_in_main_lobby_label;
    public static Label lobby1_curr_players_label;
    public static Label lobby1_game_status_label;
    public static Label lobby2_curr_players_label1;
    public static Label lobby2_game_status_label;
    public static Label lobby3_game_status_label;
    public static Label lobby3_curr_players_label;
    public static TextArea main_menu_act_writer = new TextArea();
    private static Scene scene;
    static Lock lock = new ReentrantLock();
    static ActionEvent actionEvent; // set when a user clicks on a lobby to join to hold the reference to which window to switch to
    public static AtomicInteger GameRoom = new AtomicInteger(0);

    public static void setBattleScreen(Scene battleScene) {
        scene = battleScene;
    }

    public void onLobby1EnterGame(ActionEvent actionEvent) throws IOException {
        appendToWriter("Attempting to Enter Lobby 1...");
        if (checkFull(lobby1_curr_players)) {
            appendToWriter("Lobby 1 is full, cannot join...");
            gameFullAlert();
        } else if (MainNet.programState.get() == 1){
            waitingForGame();
        }else{
            enterGameRequest(0, actionEvent);
        }
    }

    public void setActionEvent(ActionEvent actionEvent) {
        MainLobbyController.actionEvent = actionEvent;
    }
    public void onLobby2EnterGame(ActionEvent actionEvent) throws IOException, GeneralSecurityException, TimeoutException {
        appendToWriter("Attempting to Enter Lobby 2...");
        if (checkFull(lobby2_curr_players)) {
            appendToWriter("Lobby 2 is full,cannot join...");
            gameFullAlert();
        } else if(MainNet.programState.get() == 1){
            waitingForGame();
        }else {
            enterGameRequest(1, actionEvent);
        }
    }

    public void onLobby3EnterGame(ActionEvent actionEvent) throws IOException, GeneralSecurityException, TimeoutException {
        appendToWriter("Attempting to Enter Lobby 3...");
        if (checkFull(lobby3_curr_players)) {
            gameFullAlert();
            appendToWriter("Lobby 3 is full, cannot join.");
        } else if (MainNet.programState.get() == 1){
            waitingForGame();
        } else {
            enterGameRequest(2, actionEvent);
        }
    }

    public void enterGameRequest(int room, ActionEvent actionEvent) throws IOException {
            appendToWriter("Lobby 1 is not full, attempting to join...");
            GameRoom.set(room);
            setActionEvent(actionEvent);
            new PacketHandler(MainNet.sa).sendGameRequestPacket(room);
            MainNet.programState.set(1);
    }

    public static void setLobby1(int curr_players, String game_status) {
        lobby1_curr_players_label.setText(String.valueOf(curr_players));
        lobby1_game_status_label.setText(game_status);
        lobby1_curr_players = curr_players;
    }

    public static void setLobby2(int curr_players, String game_status) {
        lobby2_curr_players_label1.setText(String.valueOf(curr_players));
        lobby2_game_status_label.setText(game_status);
        lobby2_curr_players = curr_players;
    }

    public static void setLobby3(int curr_players, String game_status) {
        lobby3_curr_players_label.setText(String.valueOf(curr_players));
        lobby3_game_status_label.setText(game_status);
        lobby3_curr_players = curr_players;
    }

    public boolean checkFull(int curr_players) {
        return curr_players == 3;
    }

    /*
    *   Attempts to open the battle screen
     */
    public static void OpenBattleScreen() {
        try {
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setTitle("Battling: Game Room " + GameRoom.get());
            stage.setScene(scene);
        } catch (Exception e) {
            MainLobbyController.appendToWriter("Error opening battle screen: " + e.getMessage());
        }
    }

    public void gameFullAlert() {
        Alerts.displayAlert("Game is full", "The game you are trying to join is full. Please try again later.", Alert.AlertType.INFORMATION);
    }

    public static String serverStatusFromN(int n) {
        return switch (n) {
            case 0 -> "Offline";  // Server is reporting server as offline
            case 1 -> "Main";  // Server is elected leader in zookeeper
            case 2 -> "Backup"; // Server is not elected leader in zookeeper
            default -> "Unknown"; // Server is reporting unknown status
        };
    }

    public static String roomStatusFromN(int n) {
        return switch (n) {
            case 0 -> "Waiting for Players";
            case 1 -> "Game in Progress";
            case 2 -> "Game Full";
            default -> "Unknown";
        };
    }

    // TODO : Implement this method properly. takes in parse gamerooms datagram converted object
    public static void setGameRooms(GameRooms gameRooms) {
        // Update Server Status Labels
        lock.lock();
        server1_status_label.setText(serverStatusFromN(gameRooms.getServerStatus(0)));
        server2_status_label.setText(serverStatusFromN(gameRooms.getServerStatus(1)));
        serve3_status_label.setText(serverStatusFromN(gameRooms.getServerStatus(2)));
        setLobby1(gameRooms.getNumPlayers(0), roomStatusFromN(gameRooms.getRoomStatus(0)));
        setLobby2(gameRooms.getNumPlayers(1), roomStatusFromN(gameRooms.getRoomStatus(1)));
        setLobby3(gameRooms.getNumPlayers(2), roomStatusFromN(gameRooms.getRoomStatus(2)));
        lock.unlock();
    }

    // exitGameButton is called when the user clicks the exit button
    // it completely exits the game
    public void exitGameButton(ActionEvent actionEvent) {
        System.out.println("Exit Game Button Pressed: Exiting Game...");
        System.exit(0);
    }

    public static void appendToWriter(String s) {
        System.out.println(s);
    }


    public static void appendToWriter2(String s) {
        System.out.println(s);
        main_menu_act_writer.appendText(s+"\n");
    }

    public void waitingForGame() {
        Alerts.displayAlert("Waiting for Game", "Waiting for game to start...", Alert.AlertType.INFORMATION);
    }
}
