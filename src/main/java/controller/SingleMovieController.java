package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
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
import java.util.ResourceBundle;

public class SingleMovieController implements Initializable {


    URL url;
    String id;
    Image poster;
    byte[] bytes = null;
    String lastView;

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

        try {/*
            url = ClassLoader.getSystemResource("MainView.fxml");
            Parent parent = FXMLLoader.load(url);
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();*/

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


            EditSingleMovieController editSingleMovieController = fxmlLoader.getController();
            editSingleMovieController.loadInitialData(id, single_movie_title.getText(),
                    single_movie_release_date.getText(), single_movie_runtime.getText(),
                    summary, poster);
            editSingleMovieController.setLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);

            stage.show();

        } catch (Exception e){
            System.out.println(e + " editmoviebutton");
        }
    }

    public void getLastView(String lastView) {
        this.lastView = lastView;
    }
}
