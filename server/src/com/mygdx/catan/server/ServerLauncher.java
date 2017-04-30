package com.mygdx.catan.server;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ServerLauncher extends Application {

    private CatanServer server;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        server = new CatanServer();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Catan Server");

        StackPane root = new StackPane();
        primaryStage.setScene(new Scene(root, 300, 100));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> server.stop());
    }
}
