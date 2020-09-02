package controller;

import com.google.gson.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;

public class MoviesController implements Initializable {

    ObservableList<Pane> entries = FXCollections.observableArrayList();
    String searchGlobal = "";
    ;
    @FXML
    public TextField movies_search;
    @FXML
    public ListView<Pane> movies_list_view;


    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("MoviesController working");

        Map<Integer, ListMovieElementController> allMovies;
        try {
            allMovies = getAllMovies();
            System.out.println(allMovies.size());


            HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
            for (Map.Entry<Integer, ListMovieElementController> entry: allMovies.entrySet()) {
                toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

            int i=0;

            for (Map.Entry<Integer, String> entry: sortedByTitle.entrySet()) {
                i++;

                URL url = new File("src/main/resources/ListMovieElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListMovieElementController listMovieElementController = fxmlLoader.getController();

                ListMovieElementController listMovieElementController1 = allMovies.get(entry.getKey());

                if (i % 2 == 1){
                    listMovieElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                listMovieElementController.setId(listMovieElementController1.getId());
                listMovieElementController.setTitleString(listMovieElementController1.getTitleString());
                listMovieElementController.setYearString(listMovieElementController1.getYearString());;
                listMovieElementController.setDbId(listMovieElementController1.getDbId());
                listMovieElementController.setValues();

                movies_list_view.getItems().add(pane);
                entries.add(pane);
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
            Map<Integer, ListMovieElementController> allMovies;
            try {
                allMovies = getAllMovies();
                System.out.println(allMovies.size());

                HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
                for (Map.Entry<Integer, ListMovieElementController> entry : allMovies.entrySet()) {
                    toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
                }

                LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

                int i = 0;

                for (Map.Entry<Integer, String> entry : sortedByTitle.entrySet()) {
                    //System.out.println(entry.getKey() + " / " + entry.getValue());
                    i++;

                    URL url = new File("src/main/resources/ListMovieElement.fxml").toURI().toURL();
                    FXMLLoader fxmlLoader = new FXMLLoader(url);
                    Pane pane = (Pane) fxmlLoader.load();

                    ListMovieElementController listMovieElementController = fxmlLoader.getController();

                    ListMovieElementController listMovieElementController1 = allMovies.get(entry.getKey());

                    if (i % 2 == 1) {
                        listMovieElementController.listElement.setStyle("-fx-background-color: #ffffff");
                    }

                    listMovieElementController.setId(listMovieElementController1.getId());
                    listMovieElementController.setTitleString(listMovieElementController1.getTitleString());
                    listMovieElementController.setYearString(listMovieElementController1.getYearString());
                    ;
                    listMovieElementController.setDbId(listMovieElementController1.getDbId());
                    listMovieElementController.setValues();

                    searchGlobal = movies_search.textProperty().getValue().toLowerCase();


                        if (listMovieElementController.getTitleString().toLowerCase().contains(movies_search.textProperty().getValue().toLowerCase())) {
                            movies_list_view.getItems().add(pane);
                            System.out.println("search movie else 1.1");
                        }
                    }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }


    }

    public Map<Integer, ListMovieElementController> getAllMovies() throws IOException, ParseException {

        Map<Integer, ListMovieElementController> movies = new HashMap<>();

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
                url = new File("src/main/resources/ListMovieElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListMovieElementController listMovieElementController = fxmlLoader.getController();
                if (i % 2 == 1) {
                    listMovieElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                if ((movieObj.get("title").toString().equals("null"))) {
                    listMovieElementController.setTitleString("Brak tytułu");
                } else {
                    listMovieElementController.setTitleString(movieObj.get("title").getAsString());
                }

                if ((movieObj.get("release_date").toString().equals("null"))) {
                    listMovieElementController.setYearString("(Brak)");
                } else {
                    listMovieElementController.setYearString("(" + movieObj.get("release_date").getAsString() + ")");
                }

                listMovieElementController.setId(String.valueOf(i));
                listMovieElementController.setDbId(movieObj.get("id_movie").getAsString());
                listMovieElementController.setValues();

                //movies.add(new ListElementController(listElementController.getId(), listElementController.getTitleString(),listElementController.getYearString(), listElementController.getDbId()));

                movies.put(i, listMovieElementController);

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
