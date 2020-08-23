package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class AddController implements Initializable {

    //series
    public TextField series_title;
    public TextField series_start_year;
    public TextField series_end_year;
    public TextArea series_summary;
    public Button add_series_poster;
    public Button add_series_button;
    public Label series_error_label;
    public Label series_poster_name_label;
    public Label series_added_label;

    //person
    public TextField person_name;
    public Button add_person_button;
    public Label person_error_label;
    public Label person_role_added;
    public Label person_added_label;
    public TextField movie_genres;
    public TextField series_genres;

    @FXML
    private TextField movie_title;
    @FXML
    private Label movie_error_label;
    @FXML
    private TextField movie_year;
    @FXML
    private TextField movie_runtime;
    @FXML
    private TextArea movie_summary;
    @FXML
    private Button add_movie_poster;
    @FXML
    private Label movie_poster_name;
    @FXML
    private Button add_movie_button;
    @FXML
    private Label label_added_movie;


    private String title = "";
    private String summary = "";
    private String release_date = "";
    private String end_year = "";
    private String runtime = "";
    private String genres = "";
    private byte[] bytesMovie = null;
    private byte[] bytesSeries = null;
    private String personName = "";


    @FXML
    void addmovie(ActionEvent event) throws IOException {

        System.out.println("Add Movie button clicked");

        title = movie_title.getText();
        System.out.println(title);
        if(title.length()<4) {
            System.err.println("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            movie_error_label.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            return;
        } else {
            movie_error_label.setText("");
        }

        summary = movie_summary.getText();
        release_date = movie_year.getText();
        runtime = movie_runtime.getText();
        genres = movie_genres.getText();
        JsonObject id_movie = null;

        URL url = new URL("http://localhost:8080/movie");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("title", title);

        if(summary.length()>1)
        jsonObject.put("summary", summary);
        else jsonObject.put("summary", null);

        if(release_date.length()>1)
        jsonObject.put("release_date", release_date);
        else jsonObject.put("release_date", null);

        if(runtime.length()>1)
        jsonObject.put("runtime", runtime);
        else jsonObject.put("runtime", null);

        if(bytesMovie != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytesMovie));
        } else {
            jsonObject.put("poster", null);
        }

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
                    Reader reader = new StringReader(response.toString());
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonReasponse = (JsonObject) jsonParser.parse(reader);
            id_movie = jsonReasponse;
            System.out.println("id dodanego filmu: " + jsonReasponse.get("id_movie").toString());
        }

        if (genres.length()>1){
            addmoviegenres(genres, id_movie);
        }


        movie_title.clear();
        movie_runtime.clear();
        movie_summary.clear();
        movie_year.clear();
        movie_poster_name.setText("");
        movie_genres.clear();

        System.out.println("Add Movie button executed");
    }

    @FXML
    void addmovieposter(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik graficzny plakatu");
        //fileChooser.showOpenDialog(new Stage());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpeg"));
        //fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));


        try {
            File image = fileChooser.showOpenDialog(new Stage());

            if (fileChooser != null) {
                try {
                    bytesMovie = Files.readAllBytes(image.toPath());
                } catch (IOException e) {
                    System.err.println("Plik nie mogl zostac wczytany do byte[]");
                }
                movie_poster_name.setText(image.getName());
            }
        } catch (Exception e) {
            System.out.println(e + " zamkniete okno dialogowe");
        }


    }

    public void addseriesposter(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik graficzny plakatu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpeg"));

        try {
            File image = fileChooser.showOpenDialog(new Stage());
            if (fileChooser != null) {
                try {
                    bytesSeries = Files.readAllBytes(image.toPath());
                } catch (IOException e) {
                    System.err.println("Plik nie mogl zostac wczytany do byte[]");
                }
                series_poster_name_label.setText(image.getName());
            }
        } catch (Exception e) {
            System.out.println(e + " zamkniete okno dialogowe");
        }
    }

    public void addseries(ActionEvent actionEvent) throws IOException {

        System.out.println("Add Series button clicked");
        Integer id_tvseries = null;

        title = series_title.getText();
        System.out.println(title);
        if(title.length()<4) {
            System.err.println("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            series_error_label.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            return;
        } else {
            series_error_label.setText("");
        }

        summary = series_summary.getText();
        release_date = series_start_year.getText();
        end_year = series_end_year.getText();
        //runtime = movie_runtime.getText();
        genres = series_genres.getText();
        JsonObject id_tvseriesObj = null;

        URL url = new URL("http://localhost:8080/tvseries");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("title", title);

        if(summary.length()>1)
            jsonObject.put("summary", summary);
        else jsonObject.put("summary", null);

        if(release_date.length()>1)
            jsonObject.put("start_year", release_date);
        else jsonObject.put("start_year", null);

        if (end_year.length()>1)
            jsonObject.put("end_year", end_year);
        else jsonObject.put("end_year", null);

        /*if(runtime.length()>1)
            jsonObject.put("runtime", runtime);
        else jsonObject.put("runtime", null);
        */

        if(bytesSeries != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytesSeries));
        } else {
            jsonObject.put("poster", null);
        }

        //StringWriter out = new StringWriter();
        //jsonObject.writeJSONString(out);
        //String jsonText = out.toString();


        try(OutputStream os = con.getOutputStream()) {
            //byte[] input = jsonText.getBytes("utf-8");
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
                    Reader reader = new StringReader(response.toString());
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonResponse = (JsonObject) jsonParser.parse(reader);
            System.out.println("id dodanego serialu: " + jsonResponse.get("id_tvseries").toString());
            id_tvseries = Integer.valueOf(jsonResponse.get("id_tvseries").toString());
            id_tvseriesObj = jsonResponse;
        }

        con.disconnect(); //????

        URL url1 = new URL("http://localhost:8080/tvseassons");
        HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
        con1.setRequestMethod("POST");
        con1.setRequestProperty("Content-Type", "application/json; utf-8");
        con1.setRequestProperty("Accept", "application/json");
        con1.setDoOutput(true);

        JSONObject jsonObject1 = new JSONObject();
        JSONObject jsonObjectInternal = new JSONObject();

        if(summary.length()>1)
            jsonObject1.put("summary", summary);
        else jsonObject1.put("summary", null);

        jsonObject1.put("seasson_number", 1);

        if (id_tvseries != null) {
            jsonObjectInternal.put("id_tvseries", id_tvseries);
            jsonObject1.put("id_tvseries", jsonObjectInternal);
        }
        StringWriter out1 = new StringWriter();
        jsonObject1.writeJSONString(out1);
        String jsonText1 = out1.toString();

        try(OutputStream os = con1.getOutputStream()) {
            byte[] input = jsonObject1.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        System.out.println(jsonText1);

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con1.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
            System.out.println(response.toString());
        }
        con1.disconnect();

        if (genres.length()>1) {
            addseriesgenres(genres, id_tvseriesObj);
        }

        series_start_year.clear();
        series_end_year.clear();
        series_title.clear();
        series_summary.clear();
        series_poster_name_label.setText("");
        series_genres.clear();
        System.out.println("Add Series button executed");

    }

    public void addperson(ActionEvent actionEvent) throws IOException {

        System.out.println("add person button clicked");

        personName = person_name.getText();

        if (personName.length()<4) {
            person_error_label.setText("Imię i nazwisko nie może być krótsze niż 4 znaki, wprowadź poprawną wartość");
            return;
        } else
            person_error_label.setText("");

        URL url = new URL("http://localhost:8080/person");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setDoOutput(true);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("person_name", personName);

        StringWriter out = new StringWriter();
        jsonObject.writeJSONString(out);
        String jsonText = out.toString();

        try(OutputStream os = httpURLConnection.getOutputStream()) {
            byte[] input = jsonObject.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        System.out.println(jsonText);

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"))){
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
            Reader reader = new StringReader(response.toString());
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonResponse = (JsonObject) jsonParser.parse(reader);
            System.out.println(jsonResponse.get("id_person").toString() + " / " + jsonResponse.get("person_name").toString());
        }
        httpURLConnection.disconnect();
        person_name.clear();
        person_added_label.setText("Dodano");
        System.out.println("add person button executed");
    }

    public void addmoviegenres(String genres, JsonObject id_movie) throws IOException {

        String parts[] = genres.split(",");

        Map<String, JsonObject> currentGenres = new HashMap<>();
        currentGenres = getAllGenres();

        for (int i =0; i < parts.length; i++) {

            URL url = new URL("http://localhost:8080/genre");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            while (Objects.equals(parts[i].charAt(0), ' ')){
                parts[i] = parts[i].substring(1);
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

                JSONObject movieGenresJsonObj = new JSONObject();
                movieGenresJsonObj.put("id_genre", id_genre);
                movieGenresJsonObj.put("id_movie", id_movie);

                try (OutputStream os = con1.getOutputStream()) {
                    byte[] input = movieGenresJsonObj.toString().getBytes("utf-8");
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

                URL url1 = new URL("http://localhost:8080/moviegenres/");
                HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                con1.setRequestMethod("POST");
                con1.setRequestProperty("Content-Type", "application/json; utf-8");
                con1.setRequestProperty("Accept", "application/json");
                con1.setDoOutput(true);

                JSONObject movieGenresJsonObj = new JSONObject();
                movieGenresJsonObj.put("id_genre", id_genre);
                movieGenresJsonObj.put("id_movie", id_movie);

                try (OutputStream os = con1.getOutputStream()) {
                    byte[] input = movieGenresJsonObj.toString().getBytes("utf-8");
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
            }
            con.disconnect();
            System.out.println("\"" + parts[i] + "\"");
        }
    }

    public void addseriesgenres(String genres, JsonObject id_tvseries) throws IOException {

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

            while (Objects.equals(parts[i].charAt(0), ' ')){
                parts[i] = parts[i].substring(1);
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

                URL url1 = new URL("http://localhost:8080/tvseriesgenres/");
                HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
                con1.setRequestMethod("POST");
                con1.setRequestProperty("Content-Type", "application/json; utf-8");
                con1.setRequestProperty("Accept", "application/json");
                con1.setDoOutput(true);

                JSONObject tvseriesGenresJsonObj = new JSONObject();
                tvseriesGenresJsonObj.put("id_genre", id_genre);
                tvseriesGenresJsonObj.put("id_tvseries", id_tvseries);

                try (OutputStream os = con1.getOutputStream()) {
                    byte[] input = tvseriesGenresJsonObj.toString().getBytes("utf-8");
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

                JSONObject tvseriesGenresJsonObj = new JSONObject();
                tvseriesGenresJsonObj.put("id_genre", id_genre);
                tvseriesGenresJsonObj.put("id_tvseries", id_tvseries);

                try (OutputStream os = con1.getOutputStream()) {
                    byte[] input = tvseriesGenresJsonObj.toString().getBytes("utf-8");
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
            }
            con.disconnect();
            //con1.disconnect();

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void addPersonOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.toString().contains("code = ENTER")){
            ActionEvent actionEvent = new ActionEvent();
            try {
                addperson(actionEvent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
