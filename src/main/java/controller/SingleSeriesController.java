package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SingleSeriesController {

    URL url;
    String id;
    Image poster;
    byte[] bytes = null;
    String lastView;

    public ImageView single_series_poster;
    public Button single_series_edit_button;
    public Button single_series_back_button;
    public Label single_series_title;
    public Label single_series_start_year;
    public Label single_series_end_year;
    public Label single_series_runtime;
    public TextFlow single_series_summary;
    public HBox series_seasons_hbox;

    public void editSeries(ActionEvent actionEvent) {

        try{
            url = ClassLoader.getSystemResource("EditSingleSeriesView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();


            ObservableList<Node> textFlowList = single_series_summary.getChildren();
            StringBuilder stringBuilder = new StringBuilder();
            for (Node node : textFlowList) {
                stringBuilder.append((((Text)node).getText()));
            }
            String summary = stringBuilder.toString();


            EditSingleSeriesController editSingleSeriesController = fxmlLoader.getController();
            editSingleSeriesController.loadInitialData(id, single_series_title.getText(),
                    single_series_start_year.getText(), single_series_end_year.getText(),
                    summary, poster);
            editSingleSeriesController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch (Exception e){
            System.out.println(e + " editmoviebutton");
        }

    }

    public void backToMenu(ActionEvent actionEvent) {

        try {

            url = ClassLoader.getSystemResource("MainView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();
            ViewController viewController = fxmlLoader.getController();

            URL url2 = ClassLoader.getSystemResource(lastView);
            AnchorPane view = (AnchorPane) FXMLLoader.load(url2);
            viewController.mainPane.setCenter(view);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getSeriesData(String id) throws IOException{
        System.out.println("single series id = " + id);
        this.id = id;

        String link = "http://localhost:8080/tvseries/" + id;
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

        System.out.println(stringBuilder.toString());

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        if (jsonObject.get("title").toString() != "null") {
            single_series_title.setText(jsonObject.getString("title"));
        } else single_series_title.setText("Nie znaleziono tytulu");

        if (jsonObject.get("summary").toString() != "null") {
            Text text = new Text(jsonObject.getString("summary"));
            single_series_summary.getChildren().add(text);
        } else {
            Text text = new Text("Brak opisu serialu.");
            single_series_summary.getChildren().add(text);
        }

        if (jsonObject.get("start_year").toString() != "null") {
            single_series_start_year.setText("Rok rozpoczęcia: " + jsonObject.get("start_year").toString());
        } else single_series_start_year.setText("Brak informacji o roku poczatkowym");

        if (jsonObject.get("end_year").toString() != "null") {
            single_series_end_year.setText("Rok zakończenia: " + jsonObject.get("end_year").toString());
        } else single_series_end_year.setText("Brak informacji o roku końcowym");


        System.out.println(jsonObject.get("poster").toString());

        if (jsonObject.get("poster").toString() != "null"){
            bytes = Base64.getDecoder().decode(jsonObject.get("poster").toString());

            BufferedImage bufferedImage;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try{
                bufferedImage = ImageIO.read(byteArrayInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            WritableImage writableImage = null;
            if(bufferedImage != null) {
                writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                PixelWriter pixelWriter = writableImage.getPixelWriter();
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        pixelWriter.setArgb(x, y, bufferedImage.getRGB(x, y));
                    }
                }
            }

            poster = writableImage;

            single_series_poster.setImage(poster); }
        else {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/nopicture.jpg");
            poster = new Image(fileInputStream);
        }

        System.out.println("przed arraylista");

        ArrayList<JsonObject> allSeasons = getAllSeasonsBySeriesId(Integer.valueOf(id));
        HashMap<Integer, Integer> seassonNumberId = new HashMap<>();

        System.out.println("po arrayliscie ");
        //System.out.println(allSeasons.get(1).toString());

        for (int i = 0; i < allSeasons.size(); i++) {

            JsonObject tvSeasson = allSeasons.get(i);
            Integer id_tvseasson = tvSeasson.get("id_tvseassons").getAsInt();
            Integer seassonNumber = tvSeasson.get("seasson_number").getAsInt();

            seassonNumberId.put(seassonNumber, id_tvseasson);
            System.out.println(seassonNumberId.get(seassonNumber));
        }

        /*
        while (allSeasons.iterator().hasNext()) {
            JsonObject tvSeasson = allSeasons.iterator().next();
            Integer id_tvseasson = tvSeasson.get("id_tvseassons").getAsInt();
            Integer seassonNumber = tvSeasson.get("seasson_number").getAsInt();

            seassonNumberId.put(seassonNumber, id_tvseasson);
            System.out.println(seassonNumberId.get(seassonNumber));
        }*/


       /* for (int i = 1; i <= seassonNumberId.size(); i++) {

            //Integer seasonNumberInt = i+1;
            System.out.println(seassonNumberId.containsKey(i) + " drugi for");


        } */

        for (Map.Entry<Integer, Integer> entry :
                seassonNumberId.entrySet()) {

            System.out.println(entry.getKey() + " / " + entry.getValue());
            Hyperlink seasonNumber = new Hyperlink();

            //Label seasonNumber = new Label();
            seasonNumber.setText(entry.getKey().toString());
            seasonNumber.setPrefHeight(27);
            seasonNumber.setPrefWidth(20);
            seasonNumber.setFont(new Font("System", 18));
            Insets padding = new Insets(5,5,5,5);
            seasonNumber.setPadding(padding);
            seasonNumber.setId(entry.getValue().toString());
            //seasonNumber.set
            //seasonNumber.setStyle("-fx-background-color: blue;");
            seasonNumber.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    System.out.println("kliknieto sezon " + seasonNumber.getId());
                    try {
                        URL url = ClassLoader.getSystemResource("SingleSeassonView.fxml");
                        FXMLLoader fxmlLoader = new FXMLLoader(url);
                        Parent parent = fxmlLoader.load();

                        SingleSeassonController singleSeassonController = fxmlLoader.getController();
                        singleSeassonController.setLastView(lastView);
                        singleSeassonController.getSeriesData(id);
                        singleSeassonController.getSeassonData(entry.getValue().toString());

                        Scene scene = new Scene(parent);
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            series_seasons_hbox.getChildren().add(seasonNumber);

        }


    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

    public ArrayList<JsonObject> getAllSeasonsBySeriesId(Integer id_tvseries) throws IOException {

        ArrayList<JsonObject> seassons = new ArrayList<>();

        String link = "http://localhost:8080/tvseassons/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray seassonsJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(id_tvseries + " / " + seassonsJsonArray);

        for (int i = 0; i < seassonsJsonArray.size(); i++) {
            JsonObject seassonsObj = seassonsJsonArray.get(i).getAsJsonObject();
            JsonObject tvseriesObj = (JsonObject) seassonsObj.get("id_tvseries");
            if (tvseriesObj.get("id_tvseries").toString().equals(String.valueOf(id_tvseries))) {
                seassons.add(seassonsObj);
            }
        }
        return seassons;

    }


}
