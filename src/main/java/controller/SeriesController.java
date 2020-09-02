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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.util.*;

public class SeriesController implements Initializable {

    ObservableList<Pane> entries = FXCollections.observableArrayList();
    String searchGlobal = "";
    String years = "";

    @FXML
    public TextField series_search;
    @FXML
    public ListView<Pane> series_list_view;


    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SeriesController working");



        Map<Integer, ListSeriesElementController> allSeries;
        try {
            allSeries = getAllSeries();
            System.out.println(allSeries.size());


            HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
            for (Map.Entry<Integer, ListSeriesElementController> entry: allSeries.entrySet()) {
                toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
            }

            LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

            int i=0;

            for (Map.Entry<Integer, String> entry: sortedByTitle.entrySet()) {
                i++;

                URL url = new File("src/main/resources/ListSeriesElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListSeriesElementController listSeriesElementController = fxmlLoader.getController();

                ListSeriesElementController listSeriesElementController1 = allSeries.get(entry.getKey());

                if (i % 2 == 1){
                    listSeriesElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                listSeriesElementController.setId(listSeriesElementController1.getId());
                listSeriesElementController.setTitleString(listSeriesElementController1.getTitleString());
                listSeriesElementController.setYearString(listSeriesElementController1.getYearString());;
                listSeriesElementController.setDbId(listSeriesElementController1.getDbId());
                listSeriesElementController.setValues();

                series_list_view.getItems().add(pane);
                entries.add(pane);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        }


    public void searchSeries(ActionEvent actionEvent) {
        System.out.println(series_search.textProperty().getValue());

        series_list_view.getItems().clear();

        if (series_search.textProperty().getValue().equals("")) {
            series_list_view.setItems(entries);
        }
            Map<Integer, ListSeriesElementController> allSeries;
            try {
                allSeries = getAllSeries();
                System.out.println(allSeries.size());

                HashMap<Integer, String> toBeSortedByTitle = new HashMap<>();
                for (Map.Entry<Integer, ListSeriesElementController> entry : allSeries.entrySet()) {
                    toBeSortedByTitle.put(entry.getKey(), entry.getValue().titleString.toLowerCase());
                }

                LinkedHashMap<Integer, String> sortedByTitle = sortHashMapByValues(toBeSortedByTitle);

                int i = 0;

                for (Map.Entry<Integer, String> entry : sortedByTitle.entrySet()) {
                    //System.out.println(entry.getKey() + " / " + entry.getValue());
                    i++;

                    URL url = new File("src/main/resources/ListSeriesElement.fxml").toURI().toURL();
                    FXMLLoader fxmlLoader = new FXMLLoader(url);
                    Pane pane = (Pane) fxmlLoader.load();

                    ListSeriesElementController listSeriesElementController = fxmlLoader.getController();

                    ListSeriesElementController listSeriesElementController1 = allSeries.get(entry.getKey());

                    if (i % 2 == 1) {
                        listSeriesElementController.listElement.setStyle("-fx-background-color: #ffffff");
                    }

                    listSeriesElementController.setId(listSeriesElementController1.getId());
                    listSeriesElementController.setTitleString(listSeriesElementController1.getTitleString());
                    listSeriesElementController.setYearString(listSeriesElementController1.getYearString());
                    ;
                    listSeriesElementController.setDbId(listSeriesElementController1.getDbId());
                    listSeriesElementController.setValues();

                    searchGlobal = series_search.textProperty().getValue().toLowerCase();


                        if (listSeriesElementController.getTitleString().toLowerCase().contains(series_search.textProperty().getValue().toLowerCase())) {
                            series_list_view.getItems().add(pane);
                            System.out.println("search series else 1.1");
                        }
                    }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }



    }

    public Map<Integer, ListSeriesElementController> getAllSeries() throws IOException, ParseException {

        Map<Integer, ListSeriesElementController> series = new HashMap<>();

        String link = "http://localhost:8080/tvseries/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray seriesJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(seriesJsonArray);

        for (int i=0; i<seriesJsonArray.size(); i++) {
            JsonObject seriesObj = seriesJsonArray.get(i).getAsJsonObject();

            try {
                url = new File("src/main/resources/ListSeriesElement.fxml").toURI().toURL();
                FXMLLoader fxmlLoader = new FXMLLoader(url);
                Pane pane = (Pane) fxmlLoader.load();

                ListSeriesElementController listSeriesElementController = fxmlLoader.getController();
                if (i % 2 == 1) {
                    listSeriesElementController.listElement.setStyle("-fx-background-color: #ffffff");
                }

                if ((seriesObj.get("title").toString().equals("null"))) {
                    listSeriesElementController.setTitleString("Brak tytułu");
                } else {
                    listSeriesElementController.setTitleString(seriesObj.get("title").getAsString());
                }

                if ((seriesObj.get("start_year").toString().equals("null"))) {
                    years += "(Brak ";
                } else {
                    years += "(" + seriesObj.get("start_year").getAsString();
                }

                if (seriesObj.get("end_year").toString().equals("null")) {
                    years += "-Brak)";
                } else {
                    years += "-" + seriesObj.get("end_year").getAsString() + ")";
                }
                listSeriesElementController.setYearString(years);
                years="";

                listSeriesElementController.setId(String.valueOf(i));
                listSeriesElementController.setDbId(seriesObj.get("id_tvseries").getAsString());
                listSeriesElementController.setValues();

                //Seriess.add(new ListElementController(listElementController.getId(), listElementController.getTitleString(),listElementController.getYearString(), listElementController.getDbId()));

                series.put(i, listSeriesElementController);

            } catch (Exception e) {
                System.out.println(e + " SeriessController get Seriess");
            }
        }

        return series;
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
