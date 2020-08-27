package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class EditMoviePeopleController {
    public ImageView edit_movie_people_poster;
    public Button edit_movie_people_cancel_button;
    public Button edit_movie_people_save_button;
    public Label edit_movie_people_title;
    public ListView<Label> edit_movie_people_all_list;
    public ListView<HBox> edit_movie_people_cast_list;
    public TextField peoples_search;

    String id;
    String title;
    Image poster;
    String lastView;
    Map<Integer, Label> people = new HashMap<>();
    Map<Integer, JsonObject> peopleMapJson = new HashMap<>();
    Label pickedActor;
    JsonArray allCast = new JsonArray();

    public String getLastView() {
        return lastView;
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

    public void searchPeople(ActionEvent actionEvent) {
        edit_movie_people_all_list.getItems().clear();

        if (peoples_search.textProperty().getValue().equals("")) {
            getAllPeople();
        } else {

            Map<Integer, Label> allPeoples;

            allPeoples = people;

            HashMap<Integer, String> toBeSortedByName = new HashMap<>();
            for (Map.Entry<Integer, Label> entry : allPeoples.entrySet()) {
                toBeSortedByName.put(entry.getKey(), entry.getValue().getText().toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByName = sortHashMapByValues(toBeSortedByName);

            for (Map.Entry<Integer, String> entry : sortedByName.entrySet()) {

                Label label = new Label();
                label.setText(allPeoples.get(entry.getKey()).getText());
                label.setId(allPeoples.get(entry.getKey()).getId());

                if (label.getText().toLowerCase().contains(peoples_search.textProperty().getValue().toLowerCase())) {
                    edit_movie_people_all_list.getItems().add(label);
                }
            }
        }
    }

    public void moveToCast(ActionEvent actionEvent) {

        HBox hBox = new HBox();
        Label label = new Label();
        label.setText(edit_movie_people_all_list.getSelectionModel().getSelectedItem().getText());
        label.setId(edit_movie_people_all_list.getSelectionModel().getSelectedItem().getId().toString());
        hBox.getChildren().add(label);
        hBox.getChildren().add(new Label(" jako "));
        TextField textField = new TextField();
        textField.setId("character_name_field");


        hBox.getChildren().add(textField);
        Button delete = new Button("usuń");
        delete.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                edit_movie_people_cast_list.getItems().remove(hBox);
                //edit_movie_people_cast_list.refresh();
            }
        });
        hBox.getChildren().add(delete);

        hBox.setId(label.getId());
        hBox.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println(hBox.getId());
            }
        });

        edit_movie_people_cast_list.getItems().add(hBox);
        //edit_movie_people_cast_list.refresh();
    }

    public void loadInitialData(String id, String title, Image poster) {
        this.id = id;
        if (title != null)
            this.title = title;
        if (poster != null)
            this.poster = poster;

        edit_movie_people_title.setText(title);
        edit_movie_people_poster.setImage(poster);

        getAllPeople();

    }

    public void getAllPeople() {
        try {
            String link = "http://localhost:8080/person/all";
            URL url = new URL(link);
            InputStream inputStream = url.openStream();
            Reader reader = new InputStreamReader(inputStream, "utf-8");
            JsonParser jsonParser = new JsonParser();
            JsonArray peopleJsonArray = (JsonArray) jsonParser.parse(reader);

            for (int i = 0; i < peopleJsonArray.size(); i++) {
                JsonObject peopleObj = peopleJsonArray.get(i).getAsJsonObject();;

                Label label = new Label();
                label.setText(peopleObj.get("person_name").getAsString());
                label.setId(peopleObj.get("id_person").getAsString());
                //edit_movie_people_all_list.getItems().add(label);
                label.setOnMouseClicked(new EventHandler<MouseEvent>() {
                            @Override
                                public void handle(MouseEvent event) {
                                    System.out.println(label.getId().toString() + " / " + label.getText());
                                    pickedActor = label;
                                }
                            });

                people.put(i, label);
                peopleMapJson.put(i, peopleObj);

            }

            HashMap<Integer, String> toBeSortedByName = new HashMap<>();
            for (Map.Entry<Integer,Label> entry : people.entrySet()) {
                toBeSortedByName.put(entry.getKey(), entry.getValue().getText().toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByName = sortHashMapByValues(toBeSortedByName);

            for (Map.Entry<Integer, String> entry : sortedByName.entrySet()) {
                edit_movie_people_all_list.getItems().add(people.get(entry.getKey()));
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCast() throws IOException {

        for (int i = 0; i < edit_movie_people_cast_list.getItems().size(); i++) {

            JsonObject moviePeople = new JsonObject();
            String characterName;
            JsonObject id_movie = new JsonObject();
            JsonObject id_person = new JsonObject();

            HBox element = new HBox();
            element = edit_movie_people_cast_list.getItems().get(i);
            Label label = new Label();
            label = (Label) element.getChildren().get(0);
            TextField textField = new TextField();
            textField = (TextField) element.getChildren().get(2);
            id_person = peopleMapJson.get(Integer.valueOf(label.getId())-1);
            id_movie = getMovieData();
            characterName = textField.getText();

            moviePeople.addProperty("character_name", characterName);
            moviePeople.add("id_person", id_person);
            moviePeople.add("id_movie", id_movie);

            allCast.add(moviePeople);
        }

        for (int i = 0; i < allCast.size(); i++) {
            System.out.println(allCast.get(i).toString());
        }


    }

    public void getInitialMoviePeople() throws IOException {

        JsonArray allMoviePeople = new JsonArray();

        String link = "http://localhost:8080/moviepeople/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        allMoviePeople = (JsonArray) jsonParser.parse(reader);

        JsonArray thisMoviePeople = new JsonArray();
        JsonObject id_movie = getMovieData();

        for (int i = 0; i < allMoviePeople.size(); i++) {

            if (allMoviePeople.get(i).getAsJsonObject().get("id_movie").equals(id_movie)) {
                thisMoviePeople.add(allMoviePeople.get(i).getAsJsonObject());
            }

        }

        //return moviePeople;
    }

    public JsonObject getMovieData() throws IOException {

        String link = "http://localhost:8080/movie/" + id;
        HttpURLConnection httpURLConnection;
        StringBuilder stringBuilder = new StringBuilder();
        URL url = new URL(link);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        httpURLConnection.disconnect();

        JsonObject jsonObject = new JsonParser().parse(stringBuilder.toString()).getAsJsonObject();
        return jsonObject;

    }

    public void cancelChanges(ActionEvent actionEvent) {

        try {
            URL url = ClassLoader.getSystemResource("SingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleMovieController singleMovieController = fxmlLoader.getController();
            singleMovieController.getMovieData(id);
            singleMovieController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch(Exception e) {
            System.out.println(e + " cancelChanges in EditmoviePeopleController");
        }

    }

    public void saveChanges(ActionEvent actionEvent) {

        try {
            saveCast();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private LinkedHashMap<Integer, String> sortHashMapByValues(
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
        }
        return sortedMap;
    }

}
