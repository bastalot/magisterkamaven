package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.util.ResourceBundle;

public class PeopleController implements Initializable {

    @FXML
    DatePicker datePicker;

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PeopleController working");
    }
}
