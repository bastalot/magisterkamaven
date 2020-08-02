package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;

public class AddController {

    @FXML
    private TextField movie_title;

    @FXML
    private Label movieError;

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
    private String runtime = "";
    private byte[] bytes = null;

    @FXML
    void addmovie(ActionEvent event) throws IOException {

        System.out.println("Add Movie button clicked");

        title = movie_title.getText();
        System.out.println(title);
        if(title.length()<4) {
            System.err.println("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            movieError.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            return;
        } else {
            movieError.setText("");
        }

        summary = movie_summary.getText();
        release_date = movie_year.getText();
        runtime = movie_runtime.getText();

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

        if(bytes != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytes));
        } else {
            jsonObject.put("poster", null);
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
                    bytes = Files.readAllBytes(image.toPath());
                } catch (IOException e) {
                    System.err.println("Plik nie mogl zostac wczytany do byte[]");
                }
                movie_poster_name.setText(image.getName());
            }
        } catch (Exception e) {
            System.out.println(e + " zamkniete okno dialogowe");
        }


    }

}
