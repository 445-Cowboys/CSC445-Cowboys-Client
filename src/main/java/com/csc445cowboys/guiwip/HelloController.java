package com.csc445cowboys.guiwip;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class HelloController {
    public Button action_user_ability_button;
    public Button action_user_leave_button;
    public Button action_user_fire_button;
    public Button action_user_reload_button;
    public Label boss_weapon_type_label;
    public Label boss_curr_health_label;
    public Label boss_health_label;
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
    public Label player1_health_label;
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
    public Label player2_health_label;
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
    public Label player3_health_label;
    public Label player3_curr_health_label;
    public Label player3_max_health_label;
    public Label player3_curr_ammo_label;
    public Label player3_max_ammo_label;
    public Label player3_ability_label;
    public Label player3_ability_status1;
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }


    

}