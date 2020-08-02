package controller;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class MoviesController implements Initializable {


    ListElementController listElementController;
    Set<ListElementController> listElementControllerSet;

    @FXML
    public ComboBox<Integer> movies_combo_box;
    @FXML
    public TextField movies_search;
    @FXML
    public VBox movies_view_main_box;




    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MoviesController working");

        for(int i = 2020; i >= 1900; i--){
            movies_combo_box.getItems().add(i);
        }


        try {
            getAllMovies();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public void pickYear(ActionEvent actionEvent) throws IOException, ParseException {
        System.out.println(movies_combo_box.getValue());

        getAllMovies();
    }

    public void searchMovie(ActionEvent actionEvent) {

        System.out.println(movies_search.textProperty().getValue());

        movies_search.textProperty().addListener(((observable, oldValue, newValue) -> {
        //    System.out.println("changed from " + oldValue + " to " + newValue);
        }));
    }


    public /*List<JSONObject>*/ void getAllMovies() throws IOException, ParseException {

        //List<JSONObject> movies;

        String link = "http://localhost:8080/movie/all";
        URL url = new URL(link);


        InputStream inputStream = url.openStream();

        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();

        JsonArray moviesJsonArray = (JsonArray) jsonParser.parse(reader);


        System.out.println(moviesJsonArray);

        for (int i=0; i<moviesJsonArray.size(); i++) {
            JsonObject movieObj = moviesJsonArray.get(i).getAsJsonObject();
            //System.out.println(movieObj);

            //System.out.println(String.valueOf(movieObj.get("title").getAsString()));
            //System.out.println(String.valueOf(movieObj.get("release_date").getAsString()));


            try{
                url = new File("src/main/resources/ListElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListElementController listElementController = fxmlLoader.getController();
                if ( i%2 == 1)
                {
                    listElementController.listElement.setStyle("-fx-background-color: #ffffff");
                    //pane.setStyle("-fx-background-color: #ffffff");
                }

                if ((movieObj.get("title").toString().equals("null")) && (movieObj.get("release_date").toString().equals("null"))) {
                    listElementController.setElementData(String.valueOf(i), "Brak tytułu", "Brak", movieObj.get("id_movie").getAsString());
                } else if (movieObj.get("release_date").toString().equals("null")){
                    listElementController.setElementData(String.valueOf(i), movieObj.get("title").getAsString(), "Brak", movieObj.get("id_movie").getAsString());
                } else if (movieObj.get("title").toString().equals("null")){
                    listElementController.setElementData(String.valueOf(i), "Brak tytułu", movieObj.get("release_date").getAsString(), movieObj.get("id_movie").getAsString());
                } else {
                    listElementController.setElementData(String.valueOf(i), movieObj.get("title").getAsString(), movieObj.get("release_date").getAsString(), movieObj.get("id_movie").getAsString());
                }

                movies_view_main_box.getChildren().add(pane);
            } catch (Exception e) {
                System.out.println(e + " MoviesController get movies");
            }


        }


    }
}
