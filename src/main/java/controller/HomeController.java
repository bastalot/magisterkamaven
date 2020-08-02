package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HomeController implements Initializable {

    URL url;
    String id;

    @FXML
    private ImageView movie1;

    @FXML
    private ImageView movie2;

    @FXML
    private ImageView movie3;

    @FXML
    private ImageView movie4;

    @FXML
    private ImageView movie5;


  /*  public HomeController(ViewController viewController) {
        this.viewController = viewController;
    }*/


    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("przed");
        try {
            movie1.setId("10");
            movie1.setImage(getMovieImage(10));
        } catch (Exception e) {
            System.out.println(e + " movie1");
        }
        try {
            movie2.setId("11");
            movie2.setImage(getMovieImage(11));
        } catch (Exception e) {
            System.out.println(e + " movie2");
        }
        try{
            movie3.setId("17");
            movie3.setImage(getMovieImage(17));
         } catch (Exception e) {
                System.out.println(e + " movie3");
        }
        try {
            movie4.setId("14");
            movie4.setImage(getMovieImage(14));
        } catch (Exception e) {
            System.out.println(e + " movie4");
        }
        try{
            movie5.setId("16");
            movie5.setImage(getMovieImage(16));
        } catch (Exception e) {
            System.out.println(e + " movie5");
        }
        System.out.println("po");
    }


    public Image getMovieImage(Integer id) throws IOException {

        byte[] bytes = null;

        String link = "http://localhost:8080/movie/" + id.toString();
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
            //System.out.println(stringBuilder.toString());

        JSONObject jsonObject = new JSONObject(stringBuilder.toString());

        if (jsonObject.get("poster").toString() != "null") {
            bytes = Base64.getDecoder().decode(jsonObject.get("poster").toString());
        }
            //bytes = Base64.getDecoder().decode(stringBuilder.toString());
            //System.out.println(bytes);


            BufferedImage bufferedImage;
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            try {
                bufferedImage = ImageIO.read(byteArrayInputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            WritableImage writableImage = null;
            if (bufferedImage != null) {
                writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
                PixelWriter pixelWriter = writableImage.getPixelWriter();
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    for (int y = 0; y < bufferedImage.getHeight(); y++) {
                        pixelWriter.setArgb(x, y, bufferedImage.getRGB(x, y));
                    }
                }
            }

            Image poster = writableImage;

        return poster;


    }



    public void handlePicClicked(MouseEvent mouseEvent) {
        ImageView img = (ImageView) mouseEvent.getSource();
        id = img.getId();
        System.out.println(id);


        try {


            url = ClassLoader.getSystemResource("SingleMovieView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            Parent parent = fxmlLoader.load();

            SingleMovieController singleMovieController = fxmlLoader.getController();
            singleMovieController.getMovieData(id);
            singleMovieController.getLastView("HomeView.fxml");


            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);



            stage.show();


        } catch(Exception e) {
            System.out.println(e + " handlePicClicked method HomeController");
            System.err.println(e);
        }
        System.out.println(id);

    }

}
