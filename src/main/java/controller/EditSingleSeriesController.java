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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class EditSingleSeriesController {
    public ImageView edit_series_poster;
    public TextField edit_series_title;
    public TextField edit_series_start_year;
    public TextField edit_series_end_year;
    public TextField edit_series_genres;
    public TextArea edit_series_summary;
    public Button edit_series_poster_button;
    public Button edit_series_delete_button;
    public Button edit_series_back_button;
    public Button save_edit_series_button;
    public Label series_error_label;
    public Label label_added_series;
    public HBox series_seasons_hbox;

    private String id;
    private String title = "";
    private String start_year = "";
    private String end_year = "";
    private String summary = "";
    private byte[] bytes = null;
    private Image poster;
    private String lastView;
    private String initialgenres = "";
    private String genres = "";
    private JsonObject id_tvseries;
    private List<Integer> idSeassontoDelete = new ArrayList<>();


    public String getLastView() {
        return lastView;
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

    public void getNewPosterFromFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik graficzny nowego plakatu");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpeg"));

        try {
            File image = fileChooser.showOpenDialog(new Stage());

            if (image != null) {
                try {
                    bytes = Files.readAllBytes(image.toPath());
                } catch (IOException e) {
                    System.err.println("Plik nie mogl zostac wczytany do byte[]");
                }

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

                edit_series_poster.setImage(poster);

            } else bytes = null;

        } catch (Exception e) {
            System.out.println(e + " EditSingleSeriesController loadnewposter");
        }
    }

    public void backToSingleSeriesView(ActionEvent actionEvent) {
        try {

            URL url = ClassLoader.getSystemResource("SingleSeriesView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleSeriesController singleSeriesController = fxmlLoader.getController();
            singleSeriesController.getSeriesData(id);
            singleSeriesController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch(Exception e) {
            System.out.println(e + " handlePicClicked method HomeController");
        }
    }

    public void setSeriesData(ActionEvent actionEvent) throws IOException, ParseException {
        System.out.println("save edit series button clicked");

        title = edit_series_title.getText();
        if(title.length()<4) {
            series_error_label.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
        } else {
            series_error_label.setText("");
        }

        summary = edit_series_summary.getText();
        start_year = edit_series_start_year.getText();
        end_year = edit_series_end_year.getText();
        genres = edit_series_genres.getText();

        URL url = new URL("http://localhost:8080/tvseries/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id_tvseries", id);
        jsonObject.put("title", title);

        /*
        if(summary.length()>1)
            jsonObject.put("summary", summary);
        else jsonObject.put("summary", null); */

        if(start_year.length()>1)
            jsonObject.put("start_year", start_year);
        else jsonObject.put("start_year", null);

        if(end_year.length()>1)
            jsonObject.put("end_year", end_year);
        else jsonObject.put("end_year", null);

        if(bytes != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytes));
        } else {
            //jsonObject.put("poster", Base64.getEncoder().encodeToString());
        }

        if(summary.length()>1)
            jsonObject.put("summary", summary);
        else jsonObject.put("summary", null);

        if (genres.length()>1) {
            manageGenres();
        }

        //StringWriter out = new StringWriter();
        //jsonObject.writeJSONString(out);
        //String jsonText = out.toString();
        System.out.println(jsonObject.toString());


        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonObject.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        //System.out.println(jsonText);

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


        System.out.println("przed arraylist");

        ArrayList<JsonObject> seasonsBySeries = getAllSeassonsBySeriesId(Integer.valueOf(id));

        System.out.println("po arrayliscie przed for");
        //while(seasonsBySeries.iterator().hasNext()){
        for (int i=0; i < seasonsBySeries.size(); i++){


            JSONObject jsonObject1 = new JSONObject();
            JsonObject tvSeasson = seasonsBySeries.get(i);
            Integer id_tvseasson = tvSeasson.get("id_tvseassons").getAsInt();

            URL url1 = new URL("http://localhost:8080/tvseassons/" + id_tvseasson);
            HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
            con1.setRequestMethod("PUT");
            con1.setRequestProperty("Content-Type", "application/json; utf-8");
            con1.setRequestProperty("Accept", "application/json");
            con1.setDoOutput(true);

            if (tvSeasson.get("seasson_number").getAsInt() == 1) {
                jsonObject1.put("id_tvseassons", id_tvseasson);
                if (summary.length()>1)
                    jsonObject1.put("summary", summary);
                else jsonObject1.put("summary", null);
            }

            //StringWriter out1 = new StringWriter();
            //jsonObject1.writeJSONString(out1);
            //String jsonText1 = out.toString();

            try(OutputStream os = con1.getOutputStream()) {
                byte[] input = jsonObject1.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            //System.out.println("while1 " + jsonText1);

            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con1.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("while response 2 " + response.toString());
            }
            con1.disconnect();
        }

        //for (int i=0; i < seasonsBySeries.entrySet().size(); i++){        }

        saveSeassons();
        label_added_series.setText("Edycja pomyślna");
    }

    public void manageGenres() throws IOException {

        while (Objects.equals(genres.charAt(genres.length()-1), ' ') || Objects.equals(genres.charAt(genres.length()-1), ',')) {
            genres = genres.substring(0, genres.length() - 1);
        }

        String partsInitial[] = initialgenres.split(",");
        String parts[] = genres.split(",");
        String finalGenres = "";
        Set<String> partsSet = new HashSet<>();

        if (!initialgenres.equals(genres)) {

            for (int i = 0; i < parts.length; i++) {

                while (Objects.equals(parts[i].charAt(0), ' ')) {
                    parts[i] = parts[i].substring(1);
                }

                partsSet.add(parts[i]);
                Boolean isInTVSeriesGenres = false;

                JsonArray tvSeriesGenresArray = getAllTVSeriesGenres();
                for (int j = 0; j < tvSeriesGenresArray.size(); j++) {
                    JsonObject tvSeriesGenres = tvSeriesGenresArray.get(j).getAsJsonObject();
                    JsonObject id_tvseries = tvSeriesGenres.get("id_tvseries").getAsJsonObject();
                    JsonObject id_genre = tvSeriesGenres.get("id_genre").getAsJsonObject();

                    if (id_tvseries.get("id_tvseries").getAsString().equals(id) && id_genre.get("genre_name").getAsString().equals(parts[i])) {
                        isInTVSeriesGenres = true;
                        break;
                    }
                }
                if (!isInTVSeriesGenres) {
                    finalGenres += parts[i] + ", ";
                }
            }


            for (int i = 0; i < partsInitial.length; i++) {
                if (partsInitial[i].length() > 1) {
                    while (Objects.equals(partsInitial[i].charAt(0), ' ')) {
                        partsInitial[i] = partsInitial[i].substring(1);
                    }
                }


                JsonArray tvSeriesGenresArray = getAllTVSeriesGenres();
                if (!partsSet.contains(partsInitial[i])) {

                    for (int j = 0; j < tvSeriesGenresArray.size(); j++) {
                        JsonObject tvSeriesGenres = tvSeriesGenresArray.get(j).getAsJsonObject();
                        JsonObject id_tvseries = tvSeriesGenres.get("id_tvseries").getAsJsonObject();
                        JsonObject id_genre = tvSeriesGenres.get("id_genre").getAsJsonObject();

                        if (id_tvseries.get("id_tvseries").getAsString().equals(id) && id_genre.get("genre_name").getAsString().equals(partsInitial[i])) {

                            URL url = new URL("http://localhost:8080/tvseriesgenres/" + tvSeriesGenres.get("id_tvseriesgenres"));
                            HttpURLConnection con = (HttpURLConnection) url.openConnection();
                            con.setDoInput(true);
                            con.setInstanceFollowRedirects(false);
                            con.setRequestMethod("DELETE");
                            con.setRequestProperty("Content_Type", "application/x-www-form-urlencoded");
                            con.setRequestProperty("charset", "utf-8");
                            con.setUseCaches(false);

                            System.out.println("response: " + con.getResponseMessage());
                            con.disconnect();
                        }
                    }
                }

            }
        }
        System.out.println("finalgenres " + finalGenres);
        if (finalGenres.length() > 1) {
            saveGenres(finalGenres);
        }

    }

    public void saveGenres(String genres) throws IOException {

        genres = genres.trim();

        /*while (genres.charAt((genres.length() - 1)) == ' ' || genres.charAt((genres.length() - 1)) == ',') {
            genres = genres.substring(0, genres.length() - 1);
        }*/

        System.out.println(genres.length());

        while (Objects.equals(genres.charAt(genres.length()-1), ' ') || Objects.equals(genres.charAt(genres.length()-1), ',')) {
            genres = genres.substring(0, genres.length()-1);
        }

        String parts[] = genres.split(",");
        Map<String, JsonObject> currentGenres = new HashMap<>();
        currentGenres = getAllGenres();

        for (int i = 0; i < parts.length; i++) {

            URL url = new URL("http://localhost:8080/genre");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            if (parts.length>1) {
                while (Objects.equals(parts[i].charAt(0), ' ')){
                    parts[i] = parts[i].substring(1);
                }
            }

            Boolean hasSameGenre = false;
            JsonObject id_genre = null;

            for (Map.Entry<String, JsonObject> entry : currentGenres.entrySet()) {
                if (parts[i].toLowerCase().equals(entry.getKey().toLowerCase())) {
                    id_genre = entry.getValue();
                    hasSameGenre = true;
                    break;
                }
            }

            if (!hasSameGenre) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("genre_name", parts[i]);

                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonObject.toString().getBytes("utf-8");
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
                    Reader reader = new StringReader(response.toString());
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jsonResponse = (JsonObject) jsonParser.parse(reader);
                    System.out.println("id dodanego gatunku to " + jsonResponse.get("id_genre").toString());
                    id_genre = jsonResponse;
                }

                URL url1 = new URL("http://localhost:8080/moviegenres/");
                HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                con1.setRequestMethod("POST");
                con1.setRequestProperty("Content-Type", "application/json; utf-8");
                con1.setRequestProperty("Accept", "application/json");
                con1.setDoOutput(true);

                JSONObject tvSeriesGenresJsonObj = new JSONObject();
                tvSeriesGenresJsonObj.put("id_genre", id_genre);
                tvSeriesGenresJsonObj.put("id_tvseries", id_tvseries);

                try (OutputStream os = con1.getOutputStream()) {
                    byte[] input = tvSeriesGenresJsonObj.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(con1.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
                con1.disconnect();

            } else {

                URL url1 = new URL("http://localhost:8080/tvseriesgenres/");
                HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                con1.setRequestMethod("POST");
                con1.setRequestProperty("Content-Type", "application/json; utf-8");
                con1.setRequestProperty("Accept", "application/json");
                con1.setDoOutput(true);

                JSONObject tvSeriesGenresJsonObj = new JSONObject();
                tvSeriesGenresJsonObj.put("id_genre", id_genre);
                tvSeriesGenresJsonObj.put("id_tvseries", id_tvseries);

                try (OutputStream os = con1.getOutputStream()) {
                    byte[] input = tvSeriesGenresJsonObj.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                System.out.println();

                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(con1.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println(response.toString());
                }
                con1.disconnect();
            }
            con.disconnect();
            System.out.println("\"" + parts[i] + "\"");
        }

    }

    public Map<String, JsonObject> getAllGenres() throws IOException {

        Map<String, JsonObject> genres = new HashMap<>();

        String link = "http://localhost:8080/genre/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray genresJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(genresJsonArray);

        for (int i = 0; i < genresJsonArray.size(); i++) {
            JsonObject genreObj = genresJsonArray.get(i).getAsJsonObject();
            genres.put(genreObj.get("genre_name").getAsString(), genreObj);
        }

        return genres;
    }

    public JsonArray getAllTVSeriesGenres() throws IOException{
        JsonArray tvSeriesGenres = new JsonArray();

        String link = "http://localhost:8080/tvseriesgenres/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        tvSeriesGenres = (JsonArray) jsonParser.parse(reader);


        return tvSeriesGenres;
    }

    public void loadInitialData(String id, String title, String start_year, String end_year, String summary, Image poster, String genres) {
        this.id = id;
        if (title != null)
            this.title = title;
        if (start_year != null)
            this.start_year = start_year;
        if (end_year != null)
            this.end_year = end_year;
        if (summary != null)
            this.summary = summary;
        if (poster != null)
            this.poster = poster;
        if (genres != null)
            this.initialgenres = genres;

        edit_series_title.setText(title);
        edit_series_start_year.setText(start_year);
        edit_series_end_year.setText(end_year);
        edit_series_summary.setText(summary);
        edit_series_poster.setImage(poster);
        edit_series_genres.setText(genres);

        try {
            String link = "http://localhost:8080/tvseries/" + id;
            HttpURLConnection httpURLConnection;
            StringBuilder stringBuilder = new StringBuilder();
            URL url = new URL(link);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

            Reader reader = new InputStreamReader(inputStream, "utf-8");
            JsonParser jsonParser= new JsonParser();
            id_tvseries = (JsonObject) jsonParser.parse(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }
        getInitialSeassons();

    }

    public void deleteSeries(ActionEvent actionEvent) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia serialu " + title);
        alert.setHeaderText(null);
        alert.setContentText("Czy na pewno chcesz całkowicie usunąć serial " + title + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {

            System.out.println(id);

            URL url = new URL("http://localhost:8080/tvseries/" + id);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoInput(true);
            httpCon.setInstanceFollowRedirects(false);
            httpCon.setRequestMethod("DELETE");
            httpCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpCon.setRequestProperty("charset", "utf-8");
            httpCon.setUseCaches(false);


            System.out.println("response: " + httpCon.getResponseMessage());
            httpCon.disconnect();

            URL url1 = ClassLoader.getSystemResource("MainView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url1);
            Parent parent = fxmlLoader.load();
            ViewController viewController = fxmlLoader.getController();

            URL url2 = ClassLoader.getSystemResource(lastView);
            AnchorPane view = (AnchorPane) FXMLLoader.load(url2);
            viewController.mainPane.setCenter(view);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } else {

        }


    }

    public ArrayList<JsonObject> getAllSeassonsBySeriesId(Integer id_tvseries) throws IOException, ParseException {

        ArrayList<JsonObject> series = new ArrayList<>();

        String link = "http://localhost:8080/tvseassons/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray seassonsJsonArray = (JsonArray) jsonParser.parse(reader);

        System.out.println(id_tvseries + " / " + seassonsJsonArray);

        //while (seassonsJsonArray.iterator().hasNext()) {
        for (int i = 0; i < seassonsJsonArray.size(); i++){

            JsonObject seassonsObj = seassonsJsonArray.get(i).getAsJsonObject();
            JsonObject tvseriesObj = (JsonObject) seassonsObj.get("id_tvseries");
            if (tvseriesObj.get("id_tvseries").toString().equals(String.valueOf(id_tvseries))){
                series.add(seassonsObj);
                        //put(seassonsObj.get("id_tvseassons").getAsInt(), seassonsObj);
            }
        }
        return series;
    }

    public void addNewSeasson(ActionEvent actionEvent) {

        Integer newSeassonNumber = series_seasons_hbox.getChildren().size()+1;

        Hyperlink seassonNumber = new Hyperlink();
        seassonNumber.setText(newSeassonNumber.toString());
        seassonNumber.setPrefHeight(27);
        seassonNumber.setPrefWidth(35);
        seassonNumber.setFont(new Font("System", 18));
        seassonNumber.setPadding(new Insets(5,5,5,5));

        series_seasons_hbox.getChildren().add(seassonNumber);

    }

    public void deleteLastSeasson(ActionEvent actionEvent) {

        Hyperlink hyperlink = new Hyperlink();
        hyperlink = (Hyperlink) series_seasons_hbox.getChildren().get(series_seasons_hbox.getChildren().size()-1);

        if (hyperlink.getId() != null) {
            idSeassontoDelete.add(Integer.valueOf(hyperlink.getId()));
        }
        series_seasons_hbox.getChildren().remove(hyperlink);

    }

    public void saveSeassons() {

        try {
            for (int i = 0; i < series_seasons_hbox.getChildren().size(); i++) {

                JsonObject tvSeasson = new JsonObject();
                Integer seassonNumber;
                String summary;
                JsonObject id_tvseries = getSeriesData();

                Hyperlink hyperlink = new Hyperlink();
                hyperlink = (Hyperlink) series_seasons_hbox.getChildren().get(i);

                seassonNumber = Integer.valueOf(hyperlink.getText());
                summary = "opis sezonu " + seassonNumber;

                if (hyperlink.getId() != null) {
                    tvSeasson.addProperty("id_tvseassons", hyperlink.getId());
                }
                tvSeasson.addProperty("seasson_number", seassonNumber);
                tvSeasson.addProperty("summary", summary);
                tvSeasson.add("id_tvseries", id_tvseries);


                if (hyperlink.getId() != null)  {
                    URL url = new URL("http://localhost:8080/tvseassons/"+hyperlink.getId());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("PUT");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setDoOutput(true);

                    try (OutputStream os = con.getOutputStream()) {
                        byte[] input = tvSeasson.toString().getBytes("utf-8");
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

                    URL url = new URL("http://localhost:8080/tvseassons");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/json; utf-8");
                    con.setRequestProperty("Accept", "application/json");
                    con.setDoOutput(true);

                    try(OutputStream os = con.getOutputStream()) {
                        byte[] input = tvSeasson.toString().getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(con.getInputStream(), "utf-8"))){
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

            for (int i = 0; i < idSeassontoDelete.size(); i++) {

                URL url = new URL("http://localhost:8080/tvseassons/"+idSeassontoDelete.get(i));
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

    }

    public JsonObject getSeriesData() throws IOException {

        String link = "http://localhost:8080/tvseries/"+id;
        HttpURLConnection con;
        StringBuilder stringBuilder = new StringBuilder();
        URL url = new URL(link);

        con = (HttpURLConnection) url.openConnection();
        InputStream inputStream = new BufferedInputStream(con.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        JsonParser jsonParser = new JsonParser();
        JsonObject series = (JsonObject) jsonParser.parse(reader);

        return series;


    }

    public void getInitialSeassons() {
        try {
            ArrayList<JsonObject> allSeassons = getAllSeassonsBySeriesId(Integer.valueOf(id));
            HashMap<Integer, Integer> seassonNumberId = new HashMap<>();

            System.out.println(allSeassons.size());

            for (int i = 0; i < allSeassons.size(); i++) {
                JsonObject tvSeasson = allSeassons.get(i);
                Integer id_tvseasson = tvSeasson.get("id_tvseassons").getAsInt();
                Integer seassonNumber = tvSeasson.get("seasson_number").getAsInt();

                seassonNumberId.put(seassonNumber, id_tvseasson);
            }

            System.out.println(seassonNumberId.size());

            for (Map.Entry<Integer, Integer> entry: seassonNumberId.entrySet()) {

                Hyperlink seassonNumber = new Hyperlink();
                seassonNumber.setText(entry.getKey().toString());
                seassonNumber.setPrefHeight(27);
                seassonNumber.setPrefWidth(35);
                seassonNumber.setFont(new Font("System", 18));
                seassonNumber.setPadding(new Insets(5,5,5,5));
                seassonNumber.setId(entry.getValue().toString());

                series_seasons_hbox.getChildren().add(seassonNumber);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
