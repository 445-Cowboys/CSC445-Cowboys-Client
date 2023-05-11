package com.csc445cowboys.guiwip.Controllers;

import com.csc445cowboys.guiwip.Main;
import com.csc445cowboys.guiwip.Net.Character;
import com.csc445cowboys.guiwip.Net.MainNet;
import com.csc445cowboys.guiwip.Net.PacketHandler;
import com.csc445cowboys.guiwip.packets.GameStart;
import com.csc445cowboys.guiwip.packets.GameState;
import javafx.application.Platform;
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
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BattleScreenController {
    static Lock lock = new ReentrantLock();
    static AtomicInteger clientPlayerNumber = new AtomicInteger(0);
    static AtomicInteger roundNumber = new AtomicInteger(0);
    static AtomicInteger serverPlayerNumber = new AtomicInteger(0);
    public Label boss_curr_health_label;
    public Label boss_curr_ammo_label;
    public TextArea activity_writer;
    public Label player1_curr_health_label;
    public Label player1_curr_ammo_label;
    public Label player2_curr_health_label;
    public Label player2_curr_ammo_label;
    public Label player3_curr_health_label;
    public Label player3_curr_ammo_label;
    public Label round_indicator;
    public Label curr_server_name_label;
    public Label curr_player_label;
    public Label boss_weapon_type_label;
    public Label boss_max_health_label;
    public Label boss_max_ammo_label;
    public Label boss_ability_label;
    public Label boss_ability_status;
    public Label player1_name_label;
    public ImageView player1_picture;
    public Label player1_weapon_type_label;
    public Label player1_max_health_label;
    public Label player1_max_ammo_label;
    public Label player1_ability_label;
    public Label player1_ability_status1;
    public Label player2_name_label;
    public ImageView player2_picture;
    public Label player2_weapon_type_label;
    public Label player2_max_health_label;
    public Label player2_max_ammo_label;
    public Label player2_ability_label;
    public Label player2_ability_status1;
    public Label player3_name_label;
    public ImageView player3_picture;
    public Label player3_weapon_type_label;
    public Label player3_max_health_label;
    public Label player3_max_ammo_label;
    public Label player3_ability_label;
    public Label player3_ability_status1;
    public Label boss_name_label;
    public ImageView boss_picture;
    public Button action_user_leave_button;
    public Button action_user_fire_button;
    public Button action_user_reload_button;
    public VBox player1_frame;
    public VBox boss_frame;
    private Scene scene;

    private boolean isMyTurn = false;


    public void appendToBattleWriter(String text) {
        // If Last not \n
        if (!text.endsWith("\n")) {
            text = text.concat("\n");
        }
        activity_writer.appendText(text);
    }

    /*
     *  Called upon receiving a GameStatePacket, updates the fields
     * according to the GameStatePacket
     * May be some issues with parsing
     */
    public void updateFromGameStatePacket(GameState gs, SocketAddress sa) {
        lock.lock();

         if(gs.getBossHealth() <= 0){
            Alerts.displayAlert("Winner", "Your Posse has beaten Doug Lea! Now you can graduate!", Alert.AlertType.ERROR,true);
            // Sleep
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(gs.getBlockNum() < roundNumber.get()) return;

                roundNumber.set(gs.getBlockNum());
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
                serverPlayerNumber.set(temp);
                if(temp == clientPlayerNumber.get()) isMyTurn = true;
                String s = String.format("%d: %s", gs.getBlockNum(), gs.getActionMessage());
                //round_indicator.setText(String.valueOf(gs.getBlockNum()));
                if(serverPlayerNumber.get() != 3)
                    curr_player_label.setText(Character.getPlayer(serverPlayerNumber.get()).getName());
                else
                    curr_player_label.setText(boss_name_label.getText());
                curr_server_name_label.setText(sa.toString());
                appendToBattleWriter(s);
//                //if boss health is 0, send a splash screen saying that the group has won. Exit out after that
//                if(gs.getBossHealth() <= 0){
//
//                }
            }
        });
        lock.unlock();
    }

    public void setMainScreen(Scene mainMenuScene) {
        this.scene = mainMenuScene;
    }

    /*
     * Called when the player clicks the leave game button, it will block the players
     */
    public void onLeaveGameClick(ActionEvent actionEvent) throws IOException {
        new PacketHandler(MainNet.sa).sendCourtesyLeave();
        //NOTE: This should probably be a system exit, they can always just start the game up again later if they wanna
        //join again
        OpenMainMenuScreen(actionEvent);
    }

    /*
     * Called when the player clicks the fire button, it will block the players
     * action if it is not their turn
     */
    public void onFireClick(ActionEvent actionEvent) throws IOException, GeneralSecurityException {
        if (isMyTurn) {
            new PacketHandler(MainNet.sa).sendActionPacket(1, clientPlayerNumber.get());
            isMyTurn = false;
        } else {
            notTurn();
        }
    }

    public void onReloadClick(ActionEvent actionEvent) throws IOException, GeneralSecurityException {
        if (isMyTurn) {
            new PacketHandler(MainNet.sa).sendActionPacket(2, clientPlayerNumber.get());
            isMyTurn = false;
        }else {
            notTurn();
        }
    }

    public void OpenMainMenuScreen(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    public void setClientPlayerNumber(int number){
        //set up the initial client player number info
        clientPlayerNumber.set(number);
        if(clientPlayerNumber.get() == 0)
            isMyTurn = true;
    }

    public void setServerNameAndRoundLabel() {
        curr_server_name_label.setText(MainNet.sa.toString().split("/")[0]);
        curr_player_label.setText(Character.getPlayer(0).getName());
        appendToBattleWriter("You are " + Character.getPlayer(clientPlayerNumber.get()).getName());
    }

    public void changeServerName() {
        //will reset the server name to whatever sa is now for the MainNet
        curr_server_name_label.setText(MainNet.sa.toString().split("/")[0]);
    }

    private void notTurn() {
        // Disable all buttons
        Alerts.displayAlert("Not Your Turn", "It is not your turn yet.", Alert.AlertType.ERROR, false);
    }
}