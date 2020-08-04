package controller;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MoviesController implements Initializable {

    ObservableList<Pane> entries = FXCollections.observableArrayList();
    String pickedYearGlobal = "Wybierz rok";
    String searchGlobal = "";

    @FXML
    public ComboBox<String> movies_combo_box;
    @FXML
    public TextField movies_search;
    @FXML
    public VBox movies_view_main_box;
    @FXML
    public ListView<Pane> movies_list_view;




    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MoviesController working");

        movies_combo_box.getItems().add("Wybierz rok");
        for(int i = 2020; i >= 1900; i--){
            movies_combo_box.getItems().add(String.valueOf(i));
        }


        Map<Integer, ListElementController> allMovies;
        try {
            allMovies = getAllMovies();
            System.out.println(allMovies.size());


            HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
            for (Map.Entry<Integer, ListElementController> entry: allMovies.entrySet()) {
                toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

            int i=0;

            for (Map.Entry<Integer, String> entry: sortedByTitle.entrySet()) {
                //System.out.println(entry.getKey() + " / " + entry.getValue());
                i++;

                URL url = new File("src/main/resources/ListElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListElementController listElementController = fxmlLoader.getController();

                ListElementController listElementController1 = allMovies.get(entry.getKey());

                if (i % 2 == 1){
                    listElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                listElementController.setId(listElementController1.getId());
                listElementController.setTitleString(listElementController1.getTitleString());
                listElementController.setYearString(listElementController1.getYearString());;
                listElementController.setDbId(listElementController1.getDbId());
                listElementController.setValues();

                //movies_view_main_box.getChildren().add(pane);\
                movies_list_view.getItems().add(pane);
                entries.add(pane);
                //System.out.println(entries.);

            }


/*
           for (Map.Entry<Integer, ListElementController> entry : allMovies.entrySet()) {
               //System.out.println(entry.getKey() + " / " + entry.getValue().getTitleString());

               URL url = new File("src/main/resources/ListElement.fxml").toURI().toURL();
               FXMLLoader fxmlLoader = new FXMLLoader(url);
               Pane pane = (Pane) fxmlLoader.load();

               ListElementController listElementController = fxmlLoader.getController();
               //listElementController = new ListElementController(entry.getKey().toString(), entry.getValue().getTitleString(), entry.getValue().getYearString(), entry.getValue().getDbId());
               if (entry.getKey() % 2 == 1){
                   listElementController.listElement.setStyle("-fx-background-color: #ffffff");
               }

               listElementController.setId(entry.getKey().toString());
               listElementController.setTitleString(entry.getValue().getTitleString());
               listElementController.setYearString(entry.getValue().getYearString());;
               listElementController.setDbId(entry.getValue().getDbId());
               listElementController.setValues();


               //movies_view_main_box.getChildren().add(pane);
           } */

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    public void pickYear(ActionEvent actionEvent) throws IOException, ParseException {
        System.out.println(movies_combo_box.getValue());


        movies_list_view.getItems().clear();

        if (movies_combo_box.getValue().equals("Wybierz rok")) {
            movies_list_view.setItems(entries);
        }


        Map<Integer, ListElementController> allMovies;
        try {
            allMovies = getAllMovies();
            System.out.println(allMovies.size());

            HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
            for (Map.Entry<Integer, ListElementController> entry: allMovies.entrySet()) {
                toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

            int i=0;

            for (Map.Entry<Integer, String> entry: sortedByTitle.entrySet()) {
                //System.out.println(entry.getKey() + " / " + entry.getValue());
                i++;

                URL url = new File("src/main/resources/ListElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListElementController listElementController = fxmlLoader.getController();

                ListElementController listElementController1 = allMovies.get(entry.getKey());

                if (i % 2 == 1){
                    listElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                listElementController.setId(listElementController1.getId());
                listElementController.setTitleString(listElementController1.getTitleString());
                listElementController.setYearString(listElementController1.getYearString());;
                listElementController.setDbId(listElementController1.getDbId());
                listElementController.setValues();


                pickedYearGlobal = movies_combo_box.getValue();
                String pickedYear ="(" + movies_combo_box.getValue() + ")";

                if(searchGlobal.equals("")){
                    System.out.println("pick year if 1");
                    if (listElementController.getYearString().equals(pickedYear)){
                        movies_list_view.getItems().add(pane);
                        System.out.println("pick year if 1.1");
                    }

                } else {
                    System.out.println("pick year else 1");
                    if (pickedYear.equals(listElementController.getYearString()) && listElementController.getTitleString().toLowerCase().contains(searchGlobal)) {
                        System.out.println("pick year else 1.1");
                        movies_list_view.getItems().add(pane);
                    }
                }


            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }


    public void searchMovie(ActionEvent actionEvent) {

        System.out.println(movies_search.textProperty().getValue());

        movies_list_view.getItems().clear();

        if (movies_search.textProperty().getValue().equals("")) {
            movies_list_view.setItems(entries);
        }

        Map<Integer, ListElementController> allMovies;
        try {
            allMovies = getAllMovies();
            System.out.println(allMovies.size());

            HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
            for (Map.Entry<Integer, ListElementController> entry: allMovies.entrySet()) {
                toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

            int i=0;

            for (Map.Entry<Integer, String> entry: sortedByTitle.entrySet()) {
                //System.out.println(entry.getKey() + " / " + entry.getValue());
                i++;

                URL url = new File("src/main/resources/ListElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListElementController listElementController = fxmlLoader.getController();

                ListElementController listElementController1 = allMovies.get(entry.getKey());

                if (i % 2 == 1){
                    listElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                listElementController.setId(listElementController1.getId());
                listElementController.setTitleString(listElementController1.getTitleString());
                listElementController.setYearString(listElementController1.getYearString());;
                listElementController.setDbId(listElementController1.getDbId());
                listElementController.setValues();

                searchGlobal = movies_search.textProperty().getValue().toLowerCase();

                if (pickedYearGlobal.equals("Wybierz rok")){
                    System.out.println("search movie if 1");
                    if (listElementController.getTitleString().toLowerCase().contains(movies_search.textProperty().getValue().toLowerCase())) {
                        movies_list_view.getItems().add(pane);
                        System.out.println("search movie if 1.1");
                    }

                } else {
                    String pickedYear = "(" + pickedYearGlobal + ")";
                    String tempYear = listElementController.getYearString();
                    System.out.println("search movie else 1 "+pickedYear+ " / "+tempYear + " | " + searchGlobal + " / " + movies_search.textProperty().getValue().toLowerCase());
                    if (listElementController.getTitleString().toLowerCase().contains(movies_search.textProperty().getValue().toLowerCase()) && (pickedYear.equals(tempYear) )) {
                        movies_list_view.getItems().add(pane);
                        System.out.println("search movie else 1.1");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }



    public Map<Integer, ListElementController> getAllMovies() throws IOException, ParseException {

        Map<Integer, ListElementController> movies = new HashMap<>();

        String link = "http://localhost:8080/movie/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray moviesJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(moviesJsonArray);

        for (int i=0; i<moviesJsonArray.size(); i++) {
            JsonObject movieObj = moviesJsonArray.get(i).getAsJsonObject();

            try {
                url = new File("src/main/resources/ListElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListElementController listElementController = fxmlLoader.getController();
                if (i % 2 == 1) {
                    listElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                if ((movieObj.get("title").toString().equals("null"))) {
                    listElementController.setTitleString("Brak tytułu");
                } else {
                    listElementController.setTitleString(movieObj.get("title").getAsString());
                }

                if ((movieObj.get("release_date").toString().equals("null"))) {
                    listElementController.setYearString("(Brak)");
                } else {
                    listElementController.setYearString("(" + movieObj.get("release_date").getAsString() + ")");
                }

                listElementController.setId(String.valueOf(i));
                listElementController.setDbId(movieObj.get("id_movie").getAsString());
                listElementController.setValues();

                //movies.add(new ListElementController(listElementController.getId(), listElementController.getTitleString(),listElementController.getYearString(), listElementController.getDbId()));

                movies.put(i, listElementController);

            } catch (Exception e) {
                System.out.println(e + " MoviesController get movies");
            }
        }

        return movies;
    }

    public LinkedHashMap<Integer, String> sortHashMapByValues(
            HashMap<Integer, String> passedMap) {
        List<Integer> mapKeys = new ArrayList<>(passedMap.keySet());
        List<String> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<Integer, String> sortedMap = new LinkedHashMap<>();

        Iterator<String> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            String val = valueIt.next();
            Iterator<Integer> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Integer key = keyIt.next();
                String comp1 = passedMap.get(key);
                String comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        } return sortedMap;

    }



}
