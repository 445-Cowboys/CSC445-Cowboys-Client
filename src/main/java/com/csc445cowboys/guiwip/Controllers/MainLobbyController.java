package com.csc445cowboys.guiwip.Controllers;

import com.csc445cowboys.guiwip.Main;
import com.csc445cowboys.guiwip.Net.MainNet;
import com.csc445cowboys.guiwip.Net.PacketHandler;
import com.csc445cowboys.guiwip.Net.ServerConfig;
import com.csc445cowboys.guiwip.packets.Factory;
import com.csc445cowboys.guiwip.packets.GameRooms;
import com.csc445cowboys.guiwip.packets.GameRoomsUpdate;
import com.csc445cowboys.guiwip.packets.PlayerCount;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainLobbyController {
     int lobby1_curr_players;
     int lobby2_curr_players;
     int lobby3_curr_players;
    public Label server1_name_label;
    public Label main_server_status_label;
    public Label server2_name_label;
    public Label server3_name_label;
    public Label serve3_status_label;
    public Label server2_status_label;
    public Label server1_status_label;
    public Label players_in_game_label;
    public Label lobby1_curr_players_label;
    public Label lobby1_game_status_label;
    public Label lobby2_curr_players_label1;
    public Label lobby2_game_status_label;
    public Label lobby3_game_status_label;
    public Label lobby3_curr_players_label;
    public TextArea main_menu_act_writer;
    private static Scene scene;
    static Lock lock = new ReentrantLock();
    static ActionEvent actionEvent; // set when a user clicks on a lobby to join to hold the reference to which window to switch to
    public static AtomicInteger GameRoom = new AtomicInteger(0);

    public long lastPlayerNumUpdateTime = 0L;
    public long lastGameLobbyUpdateTime = 0L;
    public void setBattleScreen(Scene battleScene) {
        scene = battleScene;
    }

    public void onLobby1EnterGame(ActionEvent actionEvent) throws IOException {
        appendToMainLobbyWriter("Attempting to Enter Lobby 1...");
        if (checkFull(lobby1_curr_players)) {
            appendToMainLobbyWriter("Lobby 1 is full, cannot join...");
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
        appendToMainLobbyWriter("Attempting to Enter Lobby 2...");
        if (checkFull(lobby2_curr_players)) {
            appendToMainLobbyWriter("Lobby 2 is full,cannot join...");
            gameFullAlert();
        } else if(MainNet.programState.get() == 1){
            waitingForGame();
        }else {
            enterGameRequest(1, actionEvent);
        }
    }

    public void onLobby3EnterGame(ActionEvent actionEvent) throws IOException, GeneralSecurityException, TimeoutException {
        appendToMainLobbyWriter("Attempting to Enter Lobby 3...");
        if (checkFull(lobby3_curr_players)) {
            gameFullAlert();
            appendToMainLobbyWriter("Lobby 3 is full, cannot join.");
        } else if (MainNet.programState.get() == 1){
            waitingForGame();
        } else {
            enterGameRequest(2, actionEvent);
        }
    }

    public void enterGameRequest(int room, ActionEvent actionEvent) throws IOException {
            appendToMainLobbyWriter("Lobby 1 is not full, attempting to join...");
            GameRoom.set(room);
            MainNet.roomID.set(room);
            setActionEvent(actionEvent);
            new PacketHandler(MainNet.sa).sendGameRequestPacket(room);
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

    public boolean checkFull(int curr_players) {
        return curr_players == 3;
    }

    /*
    *   Attempts to open the battle screen
     */
    public void OpenBattleScreen() {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                    stage.setTitle("Battling: Game Room " + GameRoom.get());
                    stage.setScene(scene);
                }
            });
        } catch (Exception e) {
            System.out.println("Error opening battle screen");
        }
    }

    public void gameFullAlert() {
        Alerts.displayAlert("Game is full", "The game you are trying to join is full. Please try again later.", Alert.AlertType.INFORMATION);
    }

    public static String serverStatusFromN(int n) {
        return switch (n) {
            case 0 -> "Offline";  // Server is reporting server as offline
            case 1 -> "Main";  // Server is elected leader in zookeeper
            case 2 -> "Follower"; // Server is not elected leader in zookeeper
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

    public void setGameRooms(GameRooms gameRooms) {
        // Update Server Status Labels
        lock.lock();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                server1_status_label.setText(serverStatusFromN(gameRooms.getServerStatus(0)));
                server2_status_label.setText(serverStatusFromN(gameRooms.getServerStatus(1)));
                serve3_status_label.setText(serverStatusFromN(gameRooms.getServerStatus(2)));
                setLobby1(gameRooms.getNumPlayers(0), roomStatusFromN(gameRooms.getRoomStatus(0)));
                setLobby2(gameRooms.getNumPlayers(1), roomStatusFromN(gameRooms.getRoomStatus(1)));
                setLobby3(gameRooms.getNumPlayers(2), roomStatusFromN(gameRooms.getRoomStatus(2)));
                players_in_game_label.setText(String.valueOf(gameRooms.getTotalNumOfPlayers()));
            }
        });
        lock.unlock();
    }

    public void updateGameRooms(GameRoomsUpdate gameRoomsUpdate){
        // Update Server Status Labels
        lock.lock();
        if(lastGameLobbyUpdateTime < gameRoomsUpdate.getTimeStamp()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    server1_status_label.setText(serverStatusFromN(gameRoomsUpdate.getServerStatus(0)));
                    server2_status_label.setText(serverStatusFromN(gameRoomsUpdate.getServerStatus(1)));
                    serve3_status_label.setText(serverStatusFromN(gameRoomsUpdate.getServerStatus(2)));
                    setLobby1(gameRoomsUpdate.getNumPlayers(0), roomStatusFromN(gameRoomsUpdate.getRoomStatus(0)));
                    setLobby2(gameRoomsUpdate.getNumPlayers(1), roomStatusFromN(gameRoomsUpdate.getRoomStatus(1)));
                    setLobby3(gameRoomsUpdate.getNumPlayers(2), roomStatusFromN(gameRoomsUpdate.getRoomStatus(2)));
                }
            });
            lastGameLobbyUpdateTime = gameRoomsUpdate.getTimeStamp();
        }
        lock.unlock();
    }

    public void updatePlayerCount(PlayerCount playerCount){
        lock.lock();
        System.out.println(lastPlayerNumUpdateTime);
        System.out.println(playerCount.getUpdateTime());
        if(lastPlayerNumUpdateTime < playerCount.getUpdateTime()) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    players_in_game_label.setText(String.valueOf(playerCount.getPlayerCount()));
                }
            });
            //update the last updated time
            lastPlayerNumUpdateTime = playerCount.getUpdateTime();
        }
        lock.unlock();
    }

    // exitGameButton is called when the user clicks the exit button
    // it completely exits the game
    public void exitGameButton(ActionEvent actionEvent) throws IOException {
        System.out.println("Exit Game Button Pressed: Exiting Game...");
        DatagramChannel channel = DatagramChannel.open().bind(null);
        InetSocketAddress serverAddress = new InetSocketAddress(ServerConfig.SERVER_NAMES[0], 7086);
        channel.send(new Factory().makeCourtesyLeave(Integer.parseInt(MainNet.channel.getLocalAddress().toString().split("]:")[1])), serverAddress);
        System.exit(0);
    }

    public void appendToMainLobbyWriter(String text) {
        if (!text.endsWith("\n")) {
            text = text.concat("\n");
        }
        main_menu_act_writer.appendText(text);

    }

    public void waitingForGame() {
        Alerts.displayAlert("Waiting for Game", "Waiting for game to start...", Alert.AlertType.INFORMATION);
    }
}
