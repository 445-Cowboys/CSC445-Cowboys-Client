package com.csc445cowboys.guiwip;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainLobbyController {


        private Scene scene;

    public Label lobby1_curr_players_label;
    private int lobby1_curr_players;
    public Label lobby1_game_status_label;
    public Label lobby2_curr_players_label1;
    public Label lobby2_game_status_label;
    public Label lobby3_game_status_label;
    public Label lobby3_curr_players_label;


    public void onLobby1EnterGame(ActionEvent actionEvent) throws IOException {
        System.out.println("Lobby 1 Enter Game");
        if (checkFull(lobby1_curr_players)) {
            System.out.println("Lobby 1 is full");
        } else {
            System.out.println("Lobby 1 is not full");
            JoinGame(actionEvent);
        }
    }

    public void onLobby2EnterGame(ActionEvent actionEvent) {
        System.out.println("Lobby 2 Enter Game");
    }

    public void onLobby3EnterGame(ActionEvent actionEvent) {
        System.out.println("Lobby 3 Enter Game");
    }

    public void setLobby1(int curr_players, String game_status) {
        lobby1_curr_players_label.setText(String.valueOf(curr_players));
        lobby1_game_status_label.setText(game_status);
    }

    public void setLobby2(int curr_players, String game_status) {
        lobby2_curr_players_label1.setText(String.valueOf(curr_players));
        lobby2_game_status_label.setText(game_status);
    }

    public void setLobby3(int curr_players, String game_status) {
        lobby3_curr_players_label.setText(String.valueOf(curr_players));
        lobby3_game_status_label.setText(game_status);
    }

    public void JoinGame(ActionEvent actionEvent) throws IOException {

    }

    public boolean checkFull(int curr_players)  {
        return curr_players == 3;
    }

    public initBs(Scene scene){
        this.scene = scene;
    }

    public void setBattleScreen(Scene battle) {
        this.scene = battle;
    }
}
