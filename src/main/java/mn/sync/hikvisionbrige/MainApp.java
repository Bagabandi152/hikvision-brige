package mn.sync.hikvisionbrige;

import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import mn.sync.hikvisionbrige.constants.FinalVariables;
import mn.sync.hikvisionbrige.models.Device;
import okhttp3.*;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Objects;


public class MainApp {

    private static String BASE_URL = "";

    public static void start(Stage stage) {

        //Create ComboBox
        ComboBox<Device> comboBox = new ComboBox<>();
        comboBox.setItems(Device.getDeviceList());
        comboBox.setPromptText("Select . . .");
        comboBox.setMaxWidth(295);
        comboBox.getStylesheets().add("custom-combobox.css");
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            BASE_URL = "http://" + newValue.getIpAddress();
            comboBox.setStyle("-fx-border-color: none;");
        });

        //Create root
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(15, 25, 10, 25));

        //Create FlowPane, then add ComboBox
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5);
        Label comboLabel = new Label("Select a device:");
        comboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        flowPane.getChildren().add(comboLabel);
        flowPane.getChildren().add(comboBox);
        root.getChildren().add(flowPane);

        //Create Separator
        Separator sep = new Separator();
        sep.setHalignment(HPos.CENTER);
        root.getChildren().add(sep);

        //Create HBox, then add Buttons
        HBox hBox = new HBox();
        hBox.setSpacing(10);

        //Create Button to sync data
        Button syncBtn = new Button("Sync");
        EventHandler<ActionEvent> syncEvent = e -> {
            if(BASE_URL.isEmpty()){
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

            // Set Digest authentication credentials
            Authenticator.setDefault(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FinalVariables.USER_NAME, FinalVariables.PASS_WORD.toCharArray());
                }
            });

            // Create OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .authenticator(new DigestAuthenticator(new Credentials(FinalVariables.USER_NAME, FinalVariables.PASS_WORD)))
                    .build();

            // Define the request body
            MediaType mediaType = MediaType.parse("application/json");
            String requestBody = "{\"AcsEventCond\":{\"searchID\":\"1\",\"searchResultPosition\":0,\"maxResults\":1000,\"major\":5,\"minor\":75,\"startTime\":\"2023-07-01T00:00:00+07:00\",\"endTime\":\"2023-07-31T00:00:00+07:00\",\"thermometryUnit\":\"celcius\",\"currTemperature\":1}}";

            // Create the request
            RequestBody body = RequestBody.create(requestBody, mediaType);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json")
                    .post(body)
                    .build();

            try {
                // Send the request
                Response response = client.newCall(request).execute();

                // Check if the request was successful
                if (response.isSuccessful()) {
                    // Read the response body
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    System.out.println(responseBody);
                } else {
                    System.out.println("Request failed with status code: " + response.code());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            BASE_URL = "";
        };
        syncBtn.setOnAction(syncEvent);
        hBox.getChildren().add(syncBtn);

        //Create Button to add new data
        Button newBtn = new Button("New");
        hBox.getChildren().add(newBtn);
        root.getChildren().add(hBox);

        //Set config in stage
        stage.setResizable(false);
        stage.setTitle("FACE RECOGNITION TERMINAL");
        Scene scene = new Scene(root, 345, 300);
        stage.setScene(scene);
        stage.show();
    }
}