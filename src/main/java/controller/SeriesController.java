package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SeriesController implements Initializable {

    @FXML
    public ComboBox<Integer> series_combo_box;
    @FXML
    public TextField series_search;




    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SeriesController working");

        for (int i = 2020; i>=1900; i--){
            series_combo_box.getItems().add(i);
        }

    }

    public void pickyear(ActionEvent actionEvent) {
        System.out.println(series_combo_box.getValue());
    }

    public void serie_search(ActionEvent actionEvent) {
        System.out.println(series_search.textProperty().getValue());


    }
}
