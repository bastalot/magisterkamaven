package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.ActionEvent;
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
    public TextArea edit_series_summary;
    public Button edit_series_poster_button;
    public Button edit_series_delete_button;
    public Button edit_series_back_button;
    public Button save_edit_series_button;
    public Label series_error_label;
    public Label label_added_series;

    private String id;
    private String title = "";
    private String start_year = "";
    private String end_year = "";
    private String summary = "";
    private byte[] bytes = null;
    private Image poster;
    private String lastView;

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

            StringWriter out1 = new StringWriter();
            jsonObject1.writeJSONString(out1);
            String jsonText1 = out.toString();

            try(OutputStream os = con1.getOutputStream()) {
                byte[] input = jsonObject1.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            System.out.println("while1 " + jsonText1);

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


        label_added_series.setText("Edycja pomyślna");
    }

    public void loadInitialData(String id, String title, String start_year, String end_year, String summary, Image poster) {
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

        edit_series_title.setText(title);
        edit_series_start_year.setText(start_year);
        edit_series_end_year.setText(end_year);
        edit_series_summary.setText(summary);
        edit_series_poster.setImage(poster);
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



}
