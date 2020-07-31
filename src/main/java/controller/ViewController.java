package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ViewController implements Initializable {

    URL url;
    @FXML
    private Label label;

    @FXML
    public BorderPane mainPane;

    public ViewController() {
    }


    @FXML
    public void handleHomeButton(javafx.event.ActionEvent actionEvent) {
        System.out.println("Home button clicked");
        try {
            //HomeController homeController = new HomeController(this);
                    url = new File("src/main/resources/HomeView.fxml").toURI().toURL();
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            mainPane.setCenter(view);


        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    public void handleMoviesButton(javafx.event.ActionEvent actionEvent) {
        System.out.println("Movies button clicked");
        try {
            url = new File("src/main/resources/MoviesView.fxml").toURI().toURL();
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            mainPane.setCenter(view);

        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    public void handleSeriesButton(javafx.event.ActionEvent actionEvent) {
        System.out.println("Series button clicked");
        try {
            url = new File("src/main/resources/SeriesView.fxml").toURI().toURL();
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            mainPane.setCenter(view);

        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    public void handlePeopleButton(javafx.event.ActionEvent actionEvent) {
        System.out.println("People button clicked");
        try {
            url = new File("src/main/resources/PeopleView.fxml").toURI().toURL();
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            mainPane.setCenter(view);

        } catch(Exception e) {
            System.out.println(e);
        }
    }

    @FXML
    public void handleAddButton(ActionEvent actionEvent) {
        System.out.println("Add button clicked");
        try {
            url = new File("src/main/resources/AddView.fxml").toURI().toURL();
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            mainPane.setCenter(view);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

   // @FXML
   // public void handlePicClick(javafx.event.ActionEvent actionEvent) {

    //}



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            //url = new File("src/main/resources/HomeView.fxml").toURI().toURL();
            url = ClassLoader.getSystemResource("HomeView.fxml");
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            mainPane.setCenter(view);


        } catch(Exception e) {
            System.out.println(e + " viewcontroller initialize method");
        }
    }

    public void setMainPane(URL url){
        try {
            AnchorPane view = (AnchorPane) FXMLLoader.load(url);
            System.out.println(url);
            mainPane.setCenter(view);
        } catch (Exception e){
            System.out.println(e + " setmainpane");
        }

    }

}

