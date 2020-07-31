package controller;

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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.ResourceBundle;

public class SingleMovieController implements Initializable {


    URL url;
    String id;

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


    public void backtomenu(ActionEvent actionEvent) {

        try {
            url = ClassLoader.getSystemResource("MainView.fxml");
            Parent parent = FXMLLoader.load(url);
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getId() {
        return id;
    }

    public void setMovieData(String id) throws IOException {
        System.out.println("single movie id = " + id);

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

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        single_movie_title.setText(jsonObject.getString("title"));
        Text text = new Text(jsonObject.getString("summary"));
        single_movie_summary.getChildren().add(text);
        single_movie_release_date.setText(jsonObject.getString("release_date"));
        single_movie_runtime.setText(jsonObject.getString("runtime"));


        byte[] bytes = null;
        bytes = Base64.getDecoder().decode(jsonObject.getString("poster"));

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

        Image poster = writableImage;

        single_movie_poster.setImage(poster);

    }

}
