package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

public class EditSingleMovieController {

    public Button edit_movie_delete_button;
    public TextField edit_movie_genres;
    private String id;
    private String title = "";
    private String summary = "";
    private String release_date = "";
    private String runtime = "";
    private String initialgenres = "";
    private String genres = "";
    private byte[] bytes = null;
    private Image poster;
    private JsonObject id_movie;

    private String lastView;

    @FXML
    private ImageView edit_movie_poster;
    @FXML
    private Button edit_movie_poster_button;
    @FXML
    private Button edit_movie_back_button;
    @FXML
    private TextField edit_movie_title;
    @FXML
    private Label movieError;
    @FXML
    private TextField edit_movie_release_date;
    @FXML
    private TextField edit_movie_runtime;
    @FXML
    private TextArea edit_movie_summary;
    @FXML
    private Button save_edit_movie_button;
    @FXML
    private Label label_added_movie;


    public String getLastView() {
        return lastView;
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

    @FXML
    void getNewPosterFromFile(ActionEvent event) {
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

                edit_movie_poster.setImage(poster);

            } else bytes = null;

        } catch (Exception e) {
            System.out.println(e + " loadnewposter");
        }
    }

    @FXML
    void backToSingleMovieView(ActionEvent event) {
        try {

            URL url = ClassLoader.getSystemResource("SingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleMovieController singleMovieController = fxmlLoader.getController();
            singleMovieController.getMovieData(id);
            singleMovieController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch(Exception e) {
            System.out.println(e + " handlePicClicked method HomeController");
        }
    }

    @FXML
    void setMovieData(ActionEvent event) throws IOException {

        System.out.println("save edit button clicked");

        title = edit_movie_title.getText();
        if(title.length()<4) {
            movieError.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
        } else {
            movieError.setText("");
        }

        summary = edit_movie_summary.getText();
        release_date = edit_movie_release_date.getText();
        runtime = edit_movie_runtime.getText();
        genres = edit_movie_genres.getText();

        URL url = new URL("http://localhost:8080/movie/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("id_movie", id);
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

        if(bytes != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytes));
        } else {
            //jsonObject.put("poster", Base64.getEncoder().encodeToString());
        }


        if (genres.length()>1) {
            manageGenres();
        }

        StringWriter out = new StringWriter();
        jsonObject.writeJSONString(out);
        String jsonText = out.toString();


        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonObject.toString().getBytes("utf-8");
            os.write(input, 0, input.length);

        }

        System.out.println(jsonText);

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }
        label_added_movie.setText("Edycja pomyślna");
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
                Boolean isInMoviesGenres = false;

                JsonArray movieGenresArray = getAllMovieGenres();
                for (int j = 0; j < movieGenresArray.size(); j++) {
                    JsonObject movieGenres = movieGenresArray.get(j).getAsJsonObject();
                    JsonObject id_movie = movieGenres.get("id_movie").getAsJsonObject();
                    JsonObject id_genre = movieGenres.get("id_genre").getAsJsonObject();

                    if (id_movie.get("id_movie").getAsString().equals(id) && id_genre.get("genre_name").getAsString().equals(parts[i])) {
                        isInMoviesGenres = true;
                        break;
                    }
                }
                if (!isInMoviesGenres) {
                    finalGenres += parts[i] + ", ";
                }
            }


            for (int i = 0; i < partsInitial.length; i++) {
                if (partsInitial[i].length() > 1) {
                    while (Objects.equals(partsInitial[i].charAt(0), ' ')) {
                        partsInitial[i] = partsInitial[i].substring(1);
                    }
                }


                JsonArray movieGenresArray = getAllMovieGenres();
                if (!partsSet.contains(partsInitial[i])) {

                        for (int j = 0; j < movieGenresArray.size(); j++) {
                            JsonObject movieGenres = movieGenresArray.get(j).getAsJsonObject();
                            JsonObject id_movie = movieGenres.get("id_movie").getAsJsonObject();
                            JsonObject id_genre = movieGenres.get("id_genre").getAsJsonObject();

                            if (id_movie.get("id_movie").getAsString().equals(id) && id_genre.get("genre_name").getAsString().equals(partsInitial[i])) {

                                URL url = new URL("http://localhost:8080/moviegenres/" + movieGenres.get("id_moviegenres"));
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

    public JsonArray getAllMovieGenres() throws IOException{
        JsonArray movieGenres = new JsonArray();

        String link = "http://localhost:8080/moviegenres/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        movieGenres = (JsonArray) jsonParser.parse(reader);


        return movieGenres;
    }

    public void loadInitialData(String id, String title, String release_date, String runtime, String summary, Image poster, String genres) {
        this.id = id;
        if (title != null)
        this.title = title;
        if (release_date != null)
        this.release_date = release_date;
        if (runtime != null)
        this.runtime = runtime;
        if (summary != null)
        this.summary = summary;
        if (poster != null)
        this.poster = poster;
        if (genres != null)
        this.initialgenres = genres;



        edit_movie_title.setText(title);
        edit_movie_release_date.setText(release_date);
        edit_movie_runtime.setText(runtime);
        edit_movie_summary.setText(summary);
        edit_movie_poster.setImage(poster);
        edit_movie_genres.setText(genres);
        try {
            String link = "http://localhost:8080/movie/" + id;
            HttpURLConnection httpURLConnection;
            StringBuilder stringBuilder = new StringBuilder();
            URL url = new URL(link);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());

            Reader reader = new InputStreamReader(inputStream, "utf-8");
            JsonParser jsonParser= new JsonParser();
            id_movie = (JsonObject) jsonParser.parse(reader);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void deleteMovie(ActionEvent actionEvent) throws IOException {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie usunięcia filmu " + title);
        alert.setHeaderText(null);
        alert.setContentText("Czy na pewno chcesz całkowicie usunąć film " + title + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {

            System.out.println(id);

            URL url = new URL("http://localhost:8080/movie/" + id);
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

}
