package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.packets.GameState;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.security.PublicKey;

public class BattleScreenController {
    public Button action_user_ability_button;
    public Button action_user_leave_button;
    public Button action_user_fire_button;
    public Button action_user_reload_button;
    public Label boss_weapon_type_label;
    public Label boss_curr_health_label;
    public Label boss_max_health_label;
    public Label boss_curr_ammo_label;
    public Label boss_max_ammo_label;
    public Label boss_ability_label;
    public Label boss_ability_status;
    public TextArea activity_writer;
    public VBox player1_frame;
    public Label player1_name_label;
    public ImageView player1_picture;
    public Label player1_weapon_type_label;
    public Label player1_curr_health_label;
    public Label player1_max_health_label;
    public Label player1_curr_ammo_label;
    public Label player1_max_ammo_label;
    public Label player1_ability_label;
    public Label player1_ability_status1;
    public VBox player2_frame;
    public Label player2_name_label;
    public ImageView player2_picture;
    public Label player2_weapon_type_label;
    public Label player2_curr_health_label;
    public Label player2_max_health_label;
    public Label player2_curr_ammo_label;
    public Label player2_max_ammo_label;
    public Label player2_ability_label;
    public Label player2_ability_status1;
    public VBox player3_frame;
    public Label player3_name_label;
    public ImageView player3_picture;
    public Label player3_weapon_type_label;
    public Label player3_curr_health_label;
    public Label player3_max_health_label;
    public Label player3_curr_ammo_label;
    public Label player3_max_ammo_label;
    public Label player3_ability_label;
    public Label player3_ability_status1;
    public VBox boss_frame;
    public Label boss_name_label;
    public ImageView boss_picture;
    private int playerTurn;

    private Scene scene;
    public MainLobbyController mainLobbyController;


    public void onUseAbilityClick(ActionEvent actionEvent) {
        System.out.println("Use Ability Clicked");
    }

    public void onLeaveGameClick(ActionEvent actionEvent) throws IOException {
        System.out.println("Leave Game Clicked");
        OpenMainMenuScreen(actionEvent);
    }

    public void onFireClick(ActionEvent actionEvent) {
        System.out.println("Fire Clicked");
    }

    public void onReloadClick(ActionEvent actionEvent) {
        System.out.println("Reload Clicked");
    }

    public void setBossFields(){
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

    public void setPlayer1Fields(){
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

    public void setPlayer2Fields(){
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

    public void setPlayer3Fields(){
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

    public void setBossImage(String path){
        Image image = new Image(path);
        boss_picture.setImage(image);
    }

    public void setPlayer1Image(String path){
        Image image = new Image(path);
        player1_picture.setImage(image);
    }

    public void setPlayer2Image(String path){
        Image image = new Image(path);
        player2_picture.setImage(image);
    }

    public void setPlayer3Image(String path){
        Image image = new Image(path);
        player3_picture.setImage(image);
    }

    public void setAllFields(){
        setBossFields();
        setPlayer1Fields();
        setPlayer2Fields();
        setPlayer3Fields();
    }
    public void appendTextToWriter(String text){
        activity_writer.appendText(text);
    }

    public void setMainScreen(Scene mainMenuScene) {
        scene = mainMenuScene;
    }

    public void OpenMainMenuScreen(ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage)((Node)actionEvent.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }


    // TODO Listener will be called when a GameStatePacket is received
    public void updateFromGameStatePacket(GameState gs){

        // Boss Stats
        boss_curr_health_label.setText(Integer.toString(gs.getBossHealth()));
        boss_curr_ammo_label.setText(Integer.toString(gs.getBossAmmo()));

        // Player Stats
        // Player 1
        player1_curr_health_label.setText(Integer.toString(gs.getPlayerHealth(1)));
        player1_curr_ammo_label.setText(Integer.toString(gs.getPlayerAmmo(1)));
        // Player 2
        player2_curr_health_label.setText(Integer.toString(gs.getPlayerHealth(2)));
        player2_curr_ammo_label.setText(Integer.toString(gs.getPlayerAmmo(2)));
        // Player 3
        player3_curr_health_label.setText(Integer.toString(gs.getPlayerHealth(3)));
        player3_curr_ammo_label.setText(Integer.toString(gs.getPlayerAmmo(3)));

        playerTurn = gs.getCurrentPlayer();

    }

    public void initializeGameScreen(){
        setAllFields();
        activity_writer.setText("");
    }

    public void setMainLobbyController(MainLobbyController mainLobbyController) {
        this.mainLobbyController = mainLobbyController;
    }
}