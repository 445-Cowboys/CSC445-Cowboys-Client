package com.csc445cowboys.guiwip;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        GuiTester guiTester = new GuiTester(stage);
//        guiTester.setAllFields();
//        guiTester.setAllFields();
//        guiTester.writerAppend();
    }

    public static void main(String[] args) {
        launch();
    }
}