package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HomeController implements Initializable {


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



    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("przed");
        try {
            movie1.setImage(getMovieImage(10));
            movie1.setId("10");
            movie2.setImage(getMovieImage(11));
            movie2.setId("11");
            movie3.setImage(getMovieImage(13));
            movie3.setId("13");
            movie4.setImage(getMovieImage(14));
            movie4.setId("14");
            movie5.setImage(getMovieImage(16));
            movie5.setId("16");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("po");
    }


    public Image getMovieImage(Integer id) throws IOException {

        byte[] bytes = null;

        String link = "http://localhost:8080/movie/" + id.toString() + "/poster";

        HttpURLConnection httpURLConnection;
        StringBuilder result = new StringBuilder();

        URL url = new URL(link);
        httpURLConnection = (HttpURLConnection) url.openConnection();
        InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        while((line = reader.readLine()) != null) {
            result.append(line);
        }
        httpURLConnection.disconnect();
            //System.out.println(result.toString());
        bytes = Base64.getDecoder().decode(result.toString());
            //System.out.println(bytes);

        BufferedImage bufferedImage;
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try{
            bufferedImage = ImageIO.read(bais);
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
        return poster;


    }



    public void handlePicClicked(MouseEvent mouseEvent) {
        ImageView img = (ImageView) mouseEvent.getSource();
        String id = img.getId().toString();
        System.out.println(id);
    }


}
