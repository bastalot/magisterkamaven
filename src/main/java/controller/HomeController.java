package controller;

import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {



    public void initialize(URL location, ResourceBundle resources) {

    }

    public void handlePicClicked(MouseEvent mouseEvent) {
        ImageView img = (ImageView) mouseEvent.getSource();
        String id = img.getId().toString();
        System.out.println(id);
    }
}
