package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SingleSeassonController {

    String id;
    String seriesid;
    Image poster;
    byte[] bytes = null;
    URL url;
    String lastView;

    public ImageView seassons_series_poster;
    public Button seassons_edit_button;
    public Button seassons_back_button;
    public Label seassons_series_title;
    public Label seassons_series_start_year;
    public Label seassons_series_end_year;
    public Label seassons_number_label;
    public TextFlow seassons_summary_textflow;
    public ListView seassons_episode_listview;



    public void editSeasson(ActionEvent actionEvent) {
    }

    public void backToMenu(ActionEvent actionEvent) {

        try {
            url = ClassLoader.getSystemResource("SingleSeriesView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();
            SingleSeriesController singleSeriesController = fxmlLoader.getController();
            singleSeriesController.getSeriesData(seriesid);
            singleSeriesController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getSeassonData(String id) throws IOException {
        System.out.println("single seasson id = " + id);
        this.id = id;

        String link = "http://localhost:8080/tvseassons/" + id;
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

        if (jsonObject.get("seasson_number").toString() != "null") {
            seassons_number_label.setText("Sezon " + jsonObject.get("seasson_number").toString());
        }
        if (jsonObject.get("summary").toString() != "null") {
            Text text = new Text(jsonObject.getString("summary"));
            seassons_summary_textflow.getChildren().add(text);
        } else {
            Text text = new Text("Brak opisu sezonu");
            seassons_summary_textflow.getChildren().add(text);
        }


        ArrayList<JsonObject> allEpisodes = getAllEpisodesBySeassonId(Integer.valueOf(id));
        HashMap<Integer, String> episodeNumberTitle = new HashMap<>();
        //HashMap<Integer, String> episodeIdTitle = new HashMap<>();

        for (int i = 0; i < allEpisodes.size(); i++) {
            JsonObject tvEpisodes = allEpisodes.get(i);
            //Integer id_tvepisode = tvEpisodes.get("id_tvepisodes").getAsInt();
            Integer episodeNumber = tvEpisodes.get("episode_number").getAsInt();
            String episodeTitle = tvEpisodes.get("title").getAsString();

            episodeNumberTitle.put(episodeNumber, episodeTitle);
            //episodeIdTitle.put(id_tvepisode, episodeTitle);
            System.out.println(episodeNumberTitle.get(episodeNumber));

        }

        for (Map.Entry<Integer, String> entry :
                episodeNumberTitle.entrySet()) {
            System.out.println(entry.getKey() + " / " + entry.getValue());
            Label episodeNumber = new Label();

            if (entry.getValue() != null)
            episodeNumber.setText("Odcinek " + entry.getKey().toString() + " : " + entry.getValue());
            else episodeNumber.setText("Odcinek " + entry.getKey().toString());
            episodeNumber.setPrefHeight(30);
            episodeNumber.setPrefWidth(480);
            episodeNumber.setFont(new Font("System", 18));
            Insets padding = new Insets(5,5,5,5);
            episodeNumber.setPadding(padding);
            //episodeNumber.setId(entry.getValue().toString());

            seassons_episode_listview.getItems().add(episodeNumber);
        }


    }

    public void getSeriesData(String id) throws IOException {
        System.out.println("single series id = " + id);
        this.seriesid = id;

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
            seassons_series_title.setText(jsonObject.getString("title"));
        } else seassons_series_title.setText("Nie znaleziono tytulu");

        if (jsonObject.get("start_year").toString() != "null") {
            seassons_series_start_year.setText("Rok rozpoczęcia: " + jsonObject.get("start_year").toString());
        } else seassons_series_start_year.setText("Brak informacji o roku poczatkowym");

        if (jsonObject.get("end_year").toString() != "null") {
            seassons_series_end_year.setText("Rok zakończenia: " + jsonObject.get("end_year").toString());
        } else seassons_series_end_year.setText("Brak informacji o roku końcowym");


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

            seassons_series_poster.setImage(poster); }
        else {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/nopicture.jpg");
            poster = new Image(fileInputStream);
        }
    }

    public ArrayList<JsonObject> getAllEpisodesBySeassonId(Integer id_tvseasson) throws IOException {

        ArrayList<JsonObject> episodes = new ArrayList<>();
        String link = "http://localhost:8080/tvepisodes/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray episodesJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(id_tvseasson + " / " + episodesJsonArray);

        for (int i = 0; i < episodesJsonArray.size(); i++) {
            JsonObject episodesObj = episodesJsonArray.get(i).getAsJsonObject();
            JsonObject seassonsObj = (JsonObject) episodesObj.get("id_tvseassons");
            if (seassonsObj.get("id_tvseassons").toString().equals(String.valueOf(id_tvseasson))) {
                episodes.add(episodesObj);
            }
        }

        return episodes;
    }


    public String getLastView() {
        return lastView;
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }
}
