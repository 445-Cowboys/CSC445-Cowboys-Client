package com.csc445cowboys.guiwip;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiTester {

    BattleScreenController battleScreenController;
    MainLobbyController mainLobbyController;
    FXMLLoader fxmlLoader;
    Scene scene;

    public GuiTester(){}

    public GuiTester(Stage stage){
        try {
            GuiMainScreenTester(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void GuiBattleScreenTester(Stage stage) throws IOException {
        this.fxmlLoader = new FXMLLoader(Main.class.getResource("battle.fxml"));
        this.scene = new Scene(fxmlLoader.load(), 1080, 720);
        this.battleScreenController = fxmlLoader.getController();
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public void GuiMainScreenTester(Stage stage) throws IOException {
        this.fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        this.scene = new Scene(fxmlLoader.load(), 1080, 720);
        this.mainLobbyController = fxmlLoader.getController();
        stage.setTitle("Test Main Menu Screen!");
        stage.setScene(scene);
        stage.show();
    }

    public void setAllFields(){
        this.battleScreenController.setPlayer1Fields();
        this.battleScreenController.setPlayer2Fields();
        this.battleScreenController.setPlayer3Fields();
        this.battleScreenController.setBossFields();
        this.battleScreenController.setBossImage("file:src/main/resources/com/csc445cowboys/guiwip/img/boss_angel.jpeg");
        this.battleScreenController.setPlayer1Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_grit.jpeg");
        this.battleScreenController.setPlayer2Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_jango.jpeg");
        this.battleScreenController.setPlayer3Image("file:src/main/resources/com/csc445cowboys/guiwip/img/player_no_name.jpeg");
    }

    public void writerAppend(){
        // iterate 50 times with random actions
        for(int i = 0; i < 50; i++){
            // Get random bytes
            this.battleScreenController.appendTextToWriter("Player 1 attacks Boss for 10 damage!\n");
        }
    }
}

