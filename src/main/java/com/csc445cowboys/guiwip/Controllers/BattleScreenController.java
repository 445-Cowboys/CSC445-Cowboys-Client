package com.csc445cowboys.guiwip.Controllers;

import com.csc445cowboys.guiwip.Net.MainNet;
import com.csc445cowboys.guiwip.Net.PacketHandler;
import com.csc445cowboys.guiwip.packets.Factory;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BattleScreenController {
    public static Label boss_curr_health_label;
    public static Label boss_curr_ammo_label;
    public static TextArea activity_writer = new TextArea();
    public static Label player1_curr_health_label;
    public static Label player1_curr_ammo_label;
    public static Label player2_curr_health_label;
    public static Label player2_curr_ammo_label;
    public static Label player3_curr_health_label;
    public static Label player3_curr_ammo_label;
    public static Label round_indicator;
    public static Label curr_server_name_label;
    public static Label curr_player_label;
    public static Label boss_weapon_type_label;
    public static Label boss_max_health_label;
    public static Label boss_max_ammo_label;
    public static Label boss_ability_label;
    public static Label boss_ability_status;
    public static Label player1_name_label;
    public static ImageView player1_picture;
    public static Label player1_weapon_type_label;
    public static Label player1_max_health_label;
    public static Label player1_max_ammo_label;
    public static Label player1_ability_label;
    public static Label player1_ability_status1;
    public static Label player2_name_label;
    public static ImageView player2_picture;
    public static Label player2_weapon_type_label;
    public static Label player2_max_health_label;
    public static Label player2_max_ammo_label;
    public static Label player2_ability_label;
    public static Label player2_ability_status1;
    public static Label player3_name_label;
    public static ImageView player3_picture;
    public static Label player3_weapon_type_label;
    public static Label player3_max_health_label;
    public static Label player3_max_ammo_label;
    public static Label player3_ability_label;
    public static Label player3_ability_status1;
    public static Label boss_name_label;
    public static ImageView boss_picture;
    static Lock lock = new ReentrantLock();
    static AtomicInteger clietPlayerNumber = new AtomicInteger(0);
    static AtomicInteger roundNumber = new AtomicInteger(0);
    static AtomicInteger serverPlayerNumber = new AtomicInteger(0);
    private Scene scene;
    public Button action_user_ability_button;
    public Button action_user_leave_button;
    public Button action_user_fire_button;
    public Button action_user_reload_button;
    public VBox player1_frame;
    public VBox boss_frame;

    /*
     *  Called upon initial game instantiation, sets the bosses fields
     *  according to the bosses stats
     * TODO set the bosses fields according to the bosses stats, based on int catalog value from GameStartPacket
     */
    public static void setBossFields(GameStart gameStart) {
        boss_name_label.setText("Boss");
        boss_weapon_type_label.setText("Weapon Type");
        boss_curr_health_label.setText("Current Health");
        boss_max_health_label.setText("Max Health");
        boss_curr_ammo_label.setText("Current Ammo");
        boss_max_ammo_label.setText("Max Ammo");
        boss_ability_label.setText("Ability");
        boss_ability_status.setText("Ability Status");
        boss_picture.setImage(null);
    }

    public static void setPlayer1Fields(GameStart gameStart) {
        player1_name_label.setText("Player 1");
        player1_weapon_type_label.setText("Weapon Type");
        player1_curr_health_label.setText("Current Health");
        player1_max_health_label.setText("Max Health");
        player1_curr_ammo_label.setText("Current Ammo");
        player1_max_ammo_label.setText("Max Ammo");
        player1_ability_label.setText("Ability");
        player1_ability_status1.setText("Ability Status");
        player1_picture.setImage(null);
    }

    public static void setPlayer2Fields(GameStart gameStart) {
        player2_name_label.setText("Player 2");
        player2_weapon_type_label.setText("Weapon Type");
        player2_curr_health_label.setText("Current Health");
        player2_max_health_label.setText("Max Health");
        player2_curr_ammo_label.setText("Current Ammo");
        player2_max_ammo_label.setText("Max Ammo");
        player2_ability_label.setText("Ability");
        player2_ability_status1.setText("Ability Status");
        player2_picture.setImage(null);
    }

    public static void setPlayer3Fields(GameStart gameStart) {
        player3_name_label.setText("Player 3");
        player3_weapon_type_label.setText("Weapon Type");
        player3_curr_health_label.setText("Current Health");
        player3_max_health_label.setText("Max Health");
        player3_curr_ammo_label.setText("Current Ammo");
        player3_max_ammo_label.setText("Max Ammo");
        player3_ability_label.setText("Ability");
        player3_ability_status1.setText("Ability Status");
        player3_picture.setImage(null);
    }

    public static void setAllFields(GameStart gameStart) {
        setBossFields(gameStart);
    }

    public static void appendToBattleWriter(String text) {
        // If Last not \n
        if (!text.endsWith("\n")) {
            text = text.concat("\n");
        }
        activity_writer.appendText(text);
    }

    /*
     *  Called upon receiving a GameStartPacket, sets the fields according ot the Character library
     * according to the GameStartPacket
     *
     */
    public static void setGameStart(GameStart gameStart, SocketAddress sa) {
        lock.lock();
        clietPlayerNumber.set(gameStart.getCharacter());
        setAllFields(gameStart);
        lock.unlock();
    }

    /*
     *  Called upon receiving a GameStatePacket, updates the fields
     * according to the GameStatePacket
     * May be some issues with parsing
     */
    public static void updateFromGameStatePacket(GameState gs, SocketAddress sa) {
        lock.lock();
        boss_curr_health_label.setText(Integer.toString(gs.getBossHealth()));
        boss_curr_ammo_label.setText(Integer.toString(gs.getBossAmmo()));
        // Player Stats
        // Player 1
        player1_curr_health_label.setText(Integer.toString(gs.getPlayerHealth(0)));
        player1_curr_ammo_label.setText(Integer.toString(gs.getPlayerAmmo(0)));
        // Player 2
        player2_curr_health_label.setText(Integer.toString(gs.getPlayerHealth(1)));
        player2_curr_ammo_label.setText(Integer.toString(gs.getPlayerAmmo(1)));
        // Player 3
        player3_curr_health_label.setText(Integer.toString(gs.getPlayerHealth(2)));
        player3_curr_ammo_label.setText(Integer.toString(gs.getPlayerAmmo(2)));

        int temp = gs.getCurrentPlayer();
        serverPlayerNumber = new AtomicInteger(temp);
        String s = String.format("%d: %s", gs.getBlockNum(), gs.getActionMessage());
        round_indicator.setText(String.valueOf(gs.getBlockNum()));
        curr_player_label.setText(Integer.toString(serverPlayerNumber.get()));
        curr_server_name_label.setText(sa.toString());
        appendToBattleWriter(s);
        lock.unlock();
    }

    public  void setMainScreen(Scene mainMenuScene) {
        this.scene = mainMenuScene;
    }

    /*
     * Called when the player clicks the leave game button, it will block the players
     * TODO Implement leave handling to server
     */
    public void onLeaveGameClick(ActionEvent actionEvent) throws IOException {
        new PacketHandler(MainNet.sa).sendActionPacket(1);
        OpenMainMenuScreen(actionEvent);
    }

    /*
     * Called when the player clicks the fire button, it will block the players
     * action if it is not their turn
     */
    public void onFireClick(ActionEvent actionEvent) throws IOException {
        if (Objects.equals(clietPlayerNumber, serverPlayerNumber)) {
            new PacketHandler(MainNet.sa).sendActionPacket(2);
        } else {
            notTurn();
        }
    }

    public void onReloadClick(ActionEvent actionEvent) throws IOException {
        if (Objects.equals(clietPlayerNumber, serverPlayerNumber)) {
            new PacketHandler(MainNet.sa).sendActionPacket(3);
        } else {
            notTurn();
        }
    }

    public void setBossImage(String path) {
        Image image = new Image(path);
        boss_picture.setImage(image);
    }

    public void OpenMainMenuScreen(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void notTurn() {
        // Disable all buttons
        Alerts.displayAlert("Not Your Turn", "It is not your turn yet.", Alert.AlertType.ERROR);
    }
}