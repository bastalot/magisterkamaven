package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class PeopleController implements Initializable {


    ObservableList<Pane> entries = FXCollections.observableArrayList();
    String searchGlobal = "";


    public VBox series_view_main_box;
    public TextField peoples_search;
    public ListView<Pane> peoples_list_view;

    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("PeopleController working");



        Map<Integer, ListPersonElementController> allPersons;
        try {
            allPersons = getAllPersons();
            System.out.println(allPersons.size());

            HashMap<Integer, String> toBeSortedByNames = new HashMap<>();
            for (Map.Entry<Integer, ListPersonElementController> entry: allPersons.entrySet()) {
                toBeSortedByNames.put(entry.getKey(), entry.getValue().nameString.toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByNames = sortHashMapByValues(toBeSortedByNames);

            int i=0;
            for (Map.Entry<Integer, String> entry: sortedByNames.entrySet()) {
                i++;

                URL url = new File("src/main/resources/ListPersonElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListPersonElementController listPersonElementController = fxmlLoader.getController();

                ListPersonElementController listPersonElementController1 = allPersons.get(entry.getKey());

                if (i % 2 == 1){
                    listPersonElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                listPersonElementController.setId(listPersonElementController1.getId());
                listPersonElementController.setNameString(listPersonElementController1.getNameString());
                listPersonElementController.setDbId(listPersonElementController1.getDbId());
                listPersonElementController.setValues();

                peoples_list_view.getItems().add(pane);
                entries.add(pane);

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
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


    private Map<Integer, ListPersonElementController> getAllPersons() throws IOException {

        Map<Integer, ListPersonElementController> people = new HashMap<>();

        String link = "http://localhost:8080/person/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray peopleJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(peopleJsonArray);

        for (int i = 0; i < peopleJsonArray.size(); i++) {
            JsonObject peopleObj = peopleJsonArray.get(i).getAsJsonObject();

            try {
                url = new File("src/main/resources/ListPersonElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListPersonElementController listPersonElementController = fxmlLoader.getController();
                if (i % 2 == 1) {
                    listPersonElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                if ((peopleObj.get("person_name").toString().equals("null"))) {
                    listPersonElementController.setNameString("Brak imienia");
                } else {
                    listPersonElementController.setNameString(peopleObj.get("person_name").getAsString());
                }

                listPersonElementController.setId(String.valueOf(i));
                listPersonElementController.setDbId(peopleObj.get("id_person").getAsString());
                listPersonElementController.setValues();

                people.put(i, listPersonElementController);
            } catch (Exception e) {
                System.out.println(e + "PeopleController get people");
            }
        }

        return people;
    }

    public void searchPeople(ActionEvent actionEvent) {

        System.out.println(peoples_search.textProperty().getValue());

        peoples_list_view.getItems().clear();

        if (peoples_search.textProperty().getValue().equals("")) {
            peoples_list_view.setItems(entries);
        }

            Map<Integer, ListPersonElementController> allPeoples;
            try {
                allPeoples = getAllPersons();
                System.out.println(allPeoples.size());

                HashMap<Integer, String> toBeSortedByName = new HashMap<>();
                for (Map.Entry<Integer, ListPersonElementController> entry : allPeoples.entrySet()) {
                    toBeSortedByName.put(entry.getKey(), entry.getValue().nameString.toLowerCase());
                }

                LinkedHashMap<Integer, String> sortedByName = sortHashMapByValues(toBeSortedByName);

                int i = 0;

                for (Map.Entry<Integer, String> entry : sortedByName.entrySet()) {
                    i++;

                    URL url = new File("src/main/resources/ListPersonElement.fxml").toURI().toURL();
                    FXMLLoader fxmlLoader = new FXMLLoader(url);
                    Pane pane = (Pane) fxmlLoader.load();

                    ListPersonElementController listPersonElementController = fxmlLoader.getController();
                    ListPersonElementController listPersonElementController1 = allPeoples.get(entry.getKey());

                    if (i % 2 == 1) {
                        listPersonElementController.listElement.setStyle("-fx-background-color: #ffffff");
                    }

                    listPersonElementController.setId(listPersonElementController1.getId());
                    listPersonElementController.setNameString(listPersonElementController1.getNameString());
                    listPersonElementController.setDbId(listPersonElementController1.getDbId());
                    listPersonElementController.setValues();

                    searchGlobal = peoples_search.textProperty().getValue().toLowerCase();

                    if (listPersonElementController.getNameString().toLowerCase().contains(peoples_search.textProperty().getValue().toLowerCase())) {
                        peoples_list_view.getItems().add(pane);
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
