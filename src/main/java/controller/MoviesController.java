package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.util.ResourceBundle;

public class MoviesController implements Initializable {

    @FXML
    DatePicker datePicker;

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MoviesController working");
    }
}
