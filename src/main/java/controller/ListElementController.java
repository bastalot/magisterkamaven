package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ListElementController implements Initializable {



    String id;
    String titleString;
    String yearString;
    String dbId;

    @FXML
    public Label title;
    @FXML
    public Label year;
    @FXML
    public Pane listElement;


    public ListElementController() {
    }

    public void setElementData(String id, String title, String year, String dbId) {
        this.id = id;
        listElement.setId(id);
        this.titleString = title;
        this.title.setText(title);
        this.yearString = year;
        this.year.setText("(" + year + ")");
        this.dbId = dbId;
    }

    public void listElementClicked(MouseEvent mouseEvent) {

        try {

            URL url = ClassLoader.getSystemResource("SingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleMovieController singleMovieController = fxmlLoader.getController();
            singleMovieController.getMovieData(dbId);
            singleMovieController.getLastView("MoviesView.fxml");

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            System.out.println(e + " ListElementController listElementClicked");
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }



}
