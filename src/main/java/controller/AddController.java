package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;

public class AddController {

    //series
    public TextField series_title;
    public TextField series_start_year;
    public TextField series_end_year;
    public TextArea series_summary;
    public Button add_series_poster;
    public Button add_series_button;
    public Label series_error_label;
    public Label series_poster_name_label;
    public Label series_added_label;

    //person
    public TextField person_name;
    public ListView person_role_list_view;
    public Button add_person_role;
    public Button add_person_button;
    public Label person_error_label;
    public Label person_role_added;
    public Label person_added_label;

    @FXML
    private TextField movie_title;

    @FXML
    private Label movie_error_label;

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
    private String end_year = "";
    private String runtime = "";
    private byte[] bytesMovie = null;
    private byte[] bytesSeries = null;

    @FXML
    void addmovie(ActionEvent event) throws IOException {

        System.out.println("Add Movie button clicked");

        title = movie_title.getText();
        System.out.println(title);
        if(title.length()<4) {
            System.err.println("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            movie_error_label.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            return;
        } else {
            movie_error_label.setText("");
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

        if(bytesMovie != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytesMovie));
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
                    Reader reader = new StringReader(response.toString());
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonReasponse = (JsonObject) jsonParser.parse(reader);
            System.out.println("id dodanego filmu: " + jsonReasponse.get("id_movie").toString());
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
                    bytesMovie = Files.readAllBytes(image.toPath());
                } catch (IOException e) {
                    System.err.println("Plik nie mogl zostac wczytany do byte[]");
                }
                movie_poster_name.setText(image.getName());
            }
        } catch (Exception e) {
            System.out.println(e + " zamkniete okno dialogowe");
        }


    }

    public void addseriesposter(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz plik graficzny plakatu");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG", "*.jpg"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPEG", "*.jpeg"));

        try {
            File image = fileChooser.showOpenDialog(new Stage());
            if (fileChooser != null) {
                try {
                    bytesSeries = Files.readAllBytes(image.toPath());
                } catch (IOException e) {
                    System.err.println("Plik nie mogl zostac wczytany do byte[]");
                }
                series_poster_name_label.setText(image.getName());
            }
        } catch (Exception e) {
            System.out.println(e + " zamkniete okno dialogowe");
        }
    }

    public void addseries(ActionEvent actionEvent) throws IOException {

        System.out.println("Add Series button clicked");
        Integer id_tvseries = null;

        title = series_title.getText();
        System.out.println(title);
        if(title.length()<4) {
            System.err.println("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            series_error_label.setText("Tytuł nie może być krótszy niż 4 znaki, wprowadź poprawną wartość");
            return;
        } else {
            series_error_label.setText("");
        }

        summary = series_summary.getText();
        release_date = series_start_year.getText();
        end_year = series_end_year.getText();
        runtime = movie_runtime.getText();

        URL url = new URL("http://localhost:8080/tvseries");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);


        JSONObject jsonObject = new JSONObject();

        jsonObject.put("title", title);

        /*if(summary.length()>1)
            jsonObject.put("summary", summary);
        else jsonObject.put("summary", null);*/

        if(release_date.length()>1)
            jsonObject.put("start_year", release_date);
        else jsonObject.put("start_year", null);

        if (end_year.length()>1)
            jsonObject.put("end_year", end_year);
        else jsonObject.put("end_year", null);

        /*if(runtime.length()>1)
            jsonObject.put("runtime", runtime);
        else jsonObject.put("runtime", null);
        */

        if(bytesSeries != null) {
            jsonObject.put("poster", Base64.getEncoder().encodeToString(bytesSeries));
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
                    Reader reader = new StringReader(response.toString());
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonResponse = (JsonObject) jsonParser.parse(reader);
            System.out.println("id dodanego serialu: " + jsonResponse.get("id_tvseries").toString());
            id_tvseries = Integer.valueOf(jsonResponse.get("id_tvseries").toString());
        }

        con.disconnect(); //????

        URL url1 = new URL("http://localhost:8080/tvseassons");
        HttpURLConnection con1 = (HttpURLConnection) url1.openConnection();
        con1.setRequestMethod("POST");
        con1.setRequestProperty("Content-Type", "application/json; utf-8");
        con1.setRequestProperty("Accept", "application/json");
        con1.setDoOutput(true);

        JSONObject jsonObject1 = new JSONObject();
        JSONObject jsonObjectInternal = new JSONObject();

        if(summary.length()>1)
            jsonObject1.put("summary", summary);
        else jsonObject1.put("summary", null);

        jsonObject1.put("seasson_number", 1);

        if (id_tvseries != null) {
            jsonObjectInternal.put("id_tvseries", id_tvseries);
            jsonObject1.put("id_tvseries", jsonObjectInternal);
        }
        StringWriter out1 = new StringWriter();
        jsonObject1.writeJSONString(out1);
        String jsonText1 = out1.toString();

        try(OutputStream os = con1.getOutputStream()) {
            byte[] input = jsonObject1.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        System.out.println(jsonText1);

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(con1.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
            System.out.println(response.toString());
        }
        con1.disconnect();

        System.out.println("Add Series button executed");

    }

    public void addpersonrole(ActionEvent actionEvent) {
    }

    public void addperson(ActionEvent actionEvent) {
    }




}
