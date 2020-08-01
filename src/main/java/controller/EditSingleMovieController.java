package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class EditSingleMovieController {

    String id;
    byte[] bytes;
    Image poster;

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
    void canceledit(ActionEvent event) {
        try {


            URL url = ClassLoader.getSystemResource("SingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleMovieController singleMovieController = fxmlLoader.getController();
            singleMovieController.setMovieData(id);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);



            stage.show();


        } catch(Exception e) {
            System.out.println(e + " handlePicClicked method HomeController");
        }
    }

    @FXML
    void loadnewposter(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik graficzny nowego plakatu");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpeg"));

        try {
            File image = fileChooser.showOpenDialog(new Stage());

            if (fileChooser != null) {
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

            }
        } catch (Exception e) {
            System.out.println(e + " loadnewposter");
        }
    }

    @FXML
    void saveeditmovie(ActionEvent event) {




    }

    public void loadCurrentData(String id, String title, String release_date, String runtime, String summary, Image poster) {
        this.id = id;
        edit_movie_title.setText(title);
        edit_movie_release_date.setText(release_date);
        edit_movie_runtime.setText(runtime);
        edit_movie_summary.setText(summary);
        this.poster = poster;
        edit_movie_poster.setImage(poster);
    }
}
