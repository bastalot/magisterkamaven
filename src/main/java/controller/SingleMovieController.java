package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SingleMovieController implements Initializable {


    public Label movie_genres_label;
    public VBox movie_peoples_vbox;

    URL url;
    String id;
    Image poster;
    byte[] bytes = null;
    String lastView;
    JsonObject id_movie;

    @FXML
    private ImageView single_movie_poster;
    @FXML
    private Button single_movie_edit_button;
    @FXML
    private Label single_movie_title;
    @FXML
    private Button single_movie_back_button;
    @FXML
    private Label single_movie_release_date;
    @FXML
    private Label single_movie_runtime;
    @FXML
    private TextFlow single_movie_summary;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

    public void getMovieData(String id) throws IOException {
        System.out.println("single movie id = " + id);
        this.id = id;

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

        System.out.println(stringBuilder.toString());

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());
        JsonParser jsonParser = new JsonParser();
        id_movie = (JsonObject) jsonParser.parse(jsonObject.toString());

        if (jsonObject.get("title").toString() != "null") {
            single_movie_title.setText(jsonObject.getString("title"));
        } else single_movie_title.setText("Nie znaleziono tytulu");

        if (jsonObject.get("summary").toString() != "null") {
            Text text = new Text(jsonObject.getString("summary"));
            single_movie_summary.getChildren().add(text);
        } else {
            Text text = new Text("Brak opisu filmu.");
            single_movie_summary.getChildren().add(text);
        }

        if (jsonObject.get("release_date").toString() != "null") {
            single_movie_release_date.setText(jsonObject.get("release_date").toString());
        } else single_movie_release_date.setText("Brak informacji o roku");

        if (jsonObject.get("runtime").toString() != "null"){
            single_movie_runtime.setText(jsonObject.get("runtime").toString());
        } else single_movie_runtime.setText("Brak informacji o czasie trwania");


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

        single_movie_poster.setImage(poster); }
        else {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/nopicture.jpg");
            poster = new Image(fileInputStream);
        }

        movie_genres_label.setText(getMovieGenres(id));
        getMoviePeople();

    }

    public void editMovie(ActionEvent actionEvent) {

        try{
            url = ClassLoader.getSystemResource("EditSingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();


            ObservableList<Node> textFlowList = single_movie_summary.getChildren();
            StringBuilder stringBuilder = new StringBuilder();
            for (Node node : textFlowList) {
                stringBuilder.append((((Text)node).getText()));
            }
            String summary = stringBuilder.toString();

            String genres = movie_genres_label.getText();
            String parts[] = genres.split(":");
            if (parts.length>1) {
                genres = parts[1];
            } else genres = null;


            EditSingleMovieController editSingleMovieController = fxmlLoader.getController();
            editSingleMovieController.loadInitialData(id, single_movie_title.getText(),
                    single_movie_release_date.getText(), single_movie_runtime.getText(),
                    summary, poster, genres);
            editSingleMovieController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch (Exception e){
            System.out.println(e + " editmoviebutton");
        }
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

    public String getMovieGenres(String id) throws IOException {

        String genres = "Gatunki: ";

        String link = "http://localhost:8080/moviegenres/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        JsonArray genresJsonArray = (JsonArray) jsonParser.parse(reader);

        for (int i = 0; i < genresJsonArray.size(); i++) {
            JsonObject movieGenreObj = genresJsonArray.get(i).getAsJsonObject();
            JsonObject movieObj = movieGenreObj.getAsJsonObject("id_movie");
            JsonObject genreObj = movieGenreObj.getAsJsonObject("id_genre");

            if (movieObj.get("id_movie").getAsString().equals(id)){
                genres += genreObj.get("genre_name").getAsString() + ", ";
            }

        }


        return genres;
    }

    public void getMoviePeople() throws IOException {

        JsonArray allMoviePeople = new JsonArray();

        String link = "http://localhost:8080/moviepeople/all";
        URL url = new URL(link);
        InputStream inputStream = url.openStream();
        Reader reader = new InputStreamReader(inputStream, "utf-8");
        JsonParser jsonParser = new JsonParser();
        allMoviePeople = (JsonArray) jsonParser.parse(reader);

        for (int i = 0; i < allMoviePeople.size(); i++) {
            if (allMoviePeople.get(i).getAsJsonObject().get("id_movie").equals(id_movie)) {
                Label label = new Label();

                String personName = allMoviePeople.get(i).getAsJsonObject().get("id_person").getAsJsonObject().get("person_name").getAsString();
                String characterName = allMoviePeople.get(i).getAsJsonObject().get("character_name").getAsString();

                label.setText(personName + " jako " + characterName);
                label.setPadding(new Insets(5,5,5,5));
                label.setPrefWidth(500);
                if (i%2 == 0) {
                    label.setStyle("-fx-background-color:rgba(119,119,119,0.19);");
                }
                movie_peoples_vbox.getChildren().add(label);
            }
        }
    }

    public void editCast(ActionEvent actionEvent) {

        try{
            url = ClassLoader.getSystemResource("EditMoviePeopleView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            EditMoviePeopleController editMoviePeopleController = fxmlLoader.getController();
            editMoviePeopleController.loadInitialData(id, single_movie_title.getText(), poster);
            editMoviePeopleController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e){
            System.out.println(e + " editcastbutton");
        }

    }
}
