package mn.sync.hikvisionbrige;

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
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import mn.sync.hikvisionbrige.holders.DeviceHolder;
import mn.sync.hikvisionbrige.models.Device;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class MainApp {

    private static String BASE_URL = "";

    public static void start(Stage stage) {
        DeviceHolder deviceHolder = DeviceHolder.getInstance();

        //Create ComboBox
        ComboBox<Device> comboBox = new ComboBox<>();
        comboBox.setItems(Device.getDeviceList());
        comboBox.setPromptText("Select . . .");
        comboBox.setMaxWidth(295);
        comboBox.getStylesheets().add("custom-combobox.css");
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            BASE_URL = "http://" + newValue.getIpAddress();
            comboBox.setStyle("-fx-border-color: none;");
            deviceHolder.setDevice(newValue);
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

            String startDate = "";
            String endDate = "";

            LocalDateTime now = LocalDateTime.now();

            // Set the time zone offset to +07:00
            ZoneOffset zoneOffset = ZoneOffset.ofHours(7);

            // Create a DateTimeFormatter with the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

            // Format the current date and time to a string
            endDate = now.atOffset(zoneOffset).format(formatter);

            Device activeDevice = deviceHolder.getDevice();
            String lastUploadRes = ImplFunctions.functions.ErpApiService("deviceupload/getlastupload","POST","application/json","{\"deviceid\":" + activeDevice.getId(),true);
            System.out.println("lastUploadRes: " + lastUploadRes);
            if(lastUploadRes == null || (lastUploadRes != null && lastUploadRes.isEmpty())){
                // Subtract 1 month from the current date
                LocalDateTime oneMonthAgo = now.minusMonths(1);

                // Format the date and time to a string
                startDate = oneMonthAgo.atOffset(zoneOffset).format(formatter);
            }else{
                try {
                    JSONObject lastUpload = new JSONObject(lastUploadRes.toString());
                    String lastUploadDate = lastUpload.getString("uploaddate");
                    LocalDateTime dateTime = LocalDateTime.parse(lastUploadDate, formatter);
                    startDate = dateTime.atOffset(zoneOffset).format(formatter);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("startDate: " + startDate);
            System.out.println("endDate: " + endDate);

            String requestBody = "{\"AcsEventCond\":{\"searchID\":\"1\",\"searchResultPosition\":0,\"maxResults\":1000,\"major\":5,\"minor\":75,\"startTime\":\"" + startDate + "\",\"endTime\":\"" + endDate + "\",\"thermometryUnit\":\"celcius\",\"currTemperature\":1}}";
            String responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json",requestBody,"application/json");
            System.out.println(responseBody);

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