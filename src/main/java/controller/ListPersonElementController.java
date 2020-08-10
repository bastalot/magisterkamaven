package controller;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class ListPersonElementController implements Initializable {

    String id;
    String nameString;
    String dbId;

    public Pane listElement;
    public Label name;

    public ListPersonElementController() {
    }

    public ListPersonElementController(String id, String nameString, String dbId) {
        this.id = id;
        this.nameString = nameString;
        this.dbId = dbId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameString() {
        return nameString;
    }

    public void setNameString(String nameString) {
        this.nameString = nameString;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public void setValues() {
        listElement.setId(id);
        name.setText(nameString);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
