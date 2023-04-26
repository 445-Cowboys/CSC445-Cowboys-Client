package com.csc445cowboys.guiwip;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;

public class MainLobbyController {

    public Label lobby1_curr_players_label;
    public Label lobby1_game_status_label;
    public Label lobby2_curr_players_label1;
    public Label lobby2_game_status_label;
    public Label lobby3_game_status_label;
    public Label lobby3_curr_players_label;


    public void onLobby1EnterGame(ActionEvent actionEvent) {
        System.out.println("Lobby 1 Enter Game");
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
}
