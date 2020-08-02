package controller;

import com.sun.deploy.net.protocol.ProtocolType;
import com.sun.security.ntlm.Client;
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
import jdk.nashorn.internal.runtime.Context;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

public class EditSingleMovieController {

    public Button edit_movie_delete_button;
    private String id;
    private String title = "";
    private String summary = "";
    private String release_date = "";
    private String runtime = "";
    private byte[] bytes = null;
    private Image poster;

    public String getLastView() {
        return lastView;
    }

    public void setLastView(String lastView) {
        this.lastView = lastView;
    }

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

    @FXML
    void backToSingleMovieView(ActionEvent event) {
        try {


            URL url = ClassLoader.getSystemResource("SingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleMovieController singleMovieController = fxmlLoader.getController();
            singleMovieController.getMovieData(id);
            singleMovieController.getLastView(lastView);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);



            stage.show();


        } catch(Exception e) {
            System.out.println(e + " handlePicClicked method HomeController");
        }
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

        StringWriter out = new StringWriter();
        jsonObject.writeJSONString(out);
        String jsonText = out.toString();


        try(OutputStream os = con.getOutputStream()) {
            //byte[] input = jsonText.getBytes("utf-8");
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

    public void loadInitialData(String id, String title, String release_date, String runtime, String summary, Image poster) {
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

        edit_movie_title.setText(title);
        edit_movie_release_date.setText(release_date);
        edit_movie_runtime.setText(runtime);
        edit_movie_summary.setText(summary);
        edit_movie_poster.setImage(poster);
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
            /*
            URL url = new URL("http://localhost:8080/movie/" + id);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestMethod("DELETE");
            con.connect(); */

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
