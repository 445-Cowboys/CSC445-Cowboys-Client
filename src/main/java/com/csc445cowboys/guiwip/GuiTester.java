package com.csc445cowboys.guiwip;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiTester {

    BattleScreenController controller;
    FXMLLoader fxmlLoader;
    Scene scene;

    public GuiTester(Stage stage) throws IOException {
        this.fxmlLoader = new FXMLLoader(Main.class.getResource("battle.fxml"));
        this.scene = new Scene(fxmlLoader.load(), 1080, 720);
        this.controller = fxmlLoader.getController();
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public void setAllFields(){
        this.controller.setPlayer1Fields();
        this.controller.setPlayer2Fields();
        this.controller.setPlayer3Fields();
        this.controller.setBossFields();
        this.controller.setBossImage("file:src/main/resources/com/csc445cowboys/guiwip/img/boss_angel.jpeg");
        this.controller.setPlayer1Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_grit.jpeg");
        this.controller.setPlayer2Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_jango.jpeg");
        this.controller.setPlayer3Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_no_name.jpeg");
    }

    public void writerAppend(){
        // iterate 50 times with random actions
        for(int i = 0; i < 50; i++){
            // Get random bytes
            this.controller.appendTextToWriter("Player 1 attacks Boss for 10 damage!\n");
        }
    }
}

