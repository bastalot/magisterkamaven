package controller;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainController extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        URL myFxmlURL = ClassLoader.getSystemResource("MainView.fxml");
        FXMLLoader loader = new FXMLLoader();

        Parent root = loader.load(myFxmlURL);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}

