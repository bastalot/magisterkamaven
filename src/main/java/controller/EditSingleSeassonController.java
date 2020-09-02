package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class EditSingleSeassonController {
    public ImageView edit_seasson_poster;
    public Button edit_seasson_delete_button;
    public Button edit_seasson_cancel_button;
    public Button edit_seasson_back_button;
    public Label edit_seasson_series_title;
    public Label edit_seassons_series_start_year;
    public Label edit_seassons_series_end_year;
    public Label edit_seassons_number_label;
    public TextArea edit_seasson_summary;
    public ListView<HBox> edit_seassons_episode_listview;

    String id_series;
    String id;
    String title;
    String startYear;
    String endYear;
    String seassonNumber;
    String summary;
    Image poster;
    String lastView;
    List<Integer> idEpisodestoDelete = new ArrayList<>();
    Boolean isLastSeasson = false;

    public void deleteSeasson(ActionEvent actionEvent) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia " + seassonNumber + " " + title);
        alert.setHeaderText(null);
        alert.setContentText("Czy na pewno chcesz usunąć " + seassonNumber + " " + title + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.out.println(id);

            URL url = new URL("http://localhost:8080/tvseassons/"+id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.setInstanceFollowRedirects(false);
            con.setRequestMethod("DELETE");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setUseCaches(false);

            System.out.println("response: " + con.getResponseMessage());
            con.disconnect();
            URL url1 = ClassLoader.getSystemResource("SingleSeriesController.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleSeriesController singleSeriesController = fxmlLoader.getController();
            singleSeriesController.getSeriesData(id_series);
            singleSeriesController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        }

    }

    public void cancelEdit(ActionEvent actionEvent) {

        try {
            URL url = ClassLoader.getSystemResource("SingleSeassonView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleSeassonController singleSeassonController = fxmlLoader.getController();
            singleSeassonController.getSeriesData(id_series);
            singleSeassonController.getSeassonData(id);
            singleSeassonController.setLastView(lastView);
            singleSeassonController.setThisLastSeason(isLastSeasson);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void saveEdit(ActionEvent actionEvent) {

        try {

            for (int i = 0; i < edit_seassons_episode_listview.getItems().size(); i++) {

                JsonObject tvEpisode = new JsonObject();
                Integer episodeNumber;
                String title;
                JsonObject id_tvseassons = getSeassonData();

                HBox element = new HBox();
                element = edit_seassons_episode_listview.getItems().get(i);
                Label label = (Label) element.getChildren().get(0);
                episodeNumber = Integer.valueOf(label.getId());
                TextField textField = (TextField) element.getChildren().get(1);
                if (textField != null)
                    title = textField.getText();
                else title = null;

                if (element.getId() != null) {
                    tvEpisode.addProperty("id_tvepisode", element.getId());
                }
                tvEpisode.addProperty("episode_number", episodeNumber);
                tvEpisode.addProperty("title", title);
                tvEpisode.add("id_tvseassons", id_tvseassons);

                if (element.getId() != null) {
                    URL url = new URL("http://localhost:8080/tvepisodes/"+element.getId());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("PUT");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setDoOutput(true);

                    try (OutputStream os = con.getOutputStream()) {
                        byte[] input = tvEpisode.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        System.out.println(response.toString());
                    }
                } else {

                    URL url = new URL("http://localhost:8080/tvepisodes");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setDoOutput(true);

                    try(OutputStream os = con.getOutputStream()) {
                        byte[] input = tvEpisode.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    try(BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        System.out.println(response.toString());
                    }
                    con.disconnect();
                }

            }

            for (int i = 0; i < idEpisodestoDelete.size(); i++) {

                URL url = new URL("http://localhost:8080/tvepisodes/" + idEpisodestoDelete.get(i));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("DELETE");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("charset", "utf-8");
                con.setUseCaches(false);

                System.out.println("response: " + con.getResponseMessage());
                con.disconnect();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = new URL("http://localhost:8080/tvseassons/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("PUT");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("summary", edit_seasson_summary.getText());

            try(OutputStream os = con.getOutputStream()) {
                byte[] input = jsonObject.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            URL url = ClassLoader.getSystemResource("SingleSeassonView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleSeassonController singleSeassonController = fxmlLoader.getController();
            singleSeassonController.getSeriesData(id_series);
            singleSeassonController.getSeassonData(id);
            singleSeassonController.setLastView(lastView);
            singleSeassonController.setThisLastSeason(isLastSeasson);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteLastEpisode(ActionEvent actionEvent) {

        HBox hBox = new HBox();
        hBox = edit_seassons_episode_listview.getItems().get(edit_seassons_episode_listview.getItems().size()-1);

        if (hBox.getId() != null) {
            idEpisodestoDelete.add(Integer.valueOf(hBox.getId()));
        }
        edit_seassons_episode_listview.getItems().remove(hBox);

    }

    public void addNewEpisode(ActionEvent actionEvent) {

        Integer newEpisodeNumber = edit_seassons_episode_listview.getItems().size()+1;

        HBox hBox = new HBox();
        Label label = new Label();
        label.setText("Odcinek " + newEpisodeNumber + " : ");
        label.setId(newEpisodeNumber.toString());
        hBox.getChildren().add(label);

        TextField textField = new TextField();
        hBox.getChildren().add(textField);

        edit_seassons_episode_listview.getItems().add(hBox);

    }

    public void loadInitialData(String id, String title, String startYear, String endYear, String seassonNumber,
                                String summary, Image poster, String id_series) {
        this.id = id;
        this.id_series = id_series;
        if (title != null)
            this.title = title;
        if (startYear != null)
            this.startYear = startYear;
        if (endYear != null)
            this.endYear = endYear;
        if (seassonNumber != null)
            this.seassonNumber = seassonNumber;
        if (summary != null)
            this.summary = summary;
        if (poster != null)
            this.poster = poster;


        edit_seasson_series_title.setText(title);
        edit_seassons_series_start_year.setText(startYear);
        edit_seassons_series_end_year.setText(endYear);
        edit_seassons_number_label.setText(seassonNumber);
        edit_seasson_summary.setText(summary);
        edit_seasson_poster.setImage(poster);

        try {
            getInitialSeassonEpisode();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getInitialSeassonEpisode() throws IOException {

        JsonArray allEpisodes = new JsonArray();

        String link = "http://localhost:8080/tvepisodes/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        allEpisodes = (JsonArray) jsonParser.parse(reader);

        JsonObject id_tvseasson = getSeassonData();
        HashMap<Integer, JsonObject> episodeNumberEpisode = new HashMap<>();

        for (int i = 0; i < allEpisodes.size(); i++) {

            JsonObject tvepisodes = new JsonObject();
            tvepisodes = allEpisodes.get(i).getAsJsonObject();

            if (allEpisodes.get(i).getAsJsonObject().get("id_tvseassons").equals(id_tvseasson)) {
                episodeNumberEpisode.put(tvepisodes.get("episode_number").getAsInt(), tvepisodes);
            }
        }

        for (Map.Entry<Integer, JsonObject> entry : episodeNumberEpisode.entrySet()) {

            JsonObject tvepisodes = entry.getValue();

            HBox hBox = new HBox();
            Label label = new Label();
            label.setText("Odcinek " + tvepisodes.get("episode_number").getAsString() + " : ");
            label.setId(tvepisodes.get("episode_number").getAsString());
            hBox.getChildren().add(label);

            TextField textField = new TextField();
            if (!String.valueOf(tvepisodes.get("title")).equals("null"))
                textField.setText(tvepisodes.get("title").getAsString());
            else textField.setText("");
            hBox.getChildren().add(textField);
            hBox.setId(tvepisodes.get("id_tvepisodes").getAsString());

            edit_seassons_episode_listview.getItems().add(hBox);

        }

    }

    public JsonObject getSeassonData() throws IOException {

        String link = "http://localhost:8080/tvseassons/" + id;
        HttpURLConnection httpURLConnection;
        StringBuilder stringBuilder = new StringBuilder();
        URL url = new URL(link);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        httpURLConnection.disconnect();
        JsonObject jsonObject = new JsonParser().parse(stringBuilder.toString()).getAsJsonObject();
        return jsonObject;

    }

    public String getLastView() {
        return lastView;
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

    public Boolean getLastSeasson() {
        return isLastSeasson;
    }

    public void setLastSeasson(Boolean lastSeasson) {
        isLastSeasson = lastSeasson;
    }
}
