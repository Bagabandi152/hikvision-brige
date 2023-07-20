package mn.sync.hikvisionbrige;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import mn.sync.hikvisionbrige.holders.DeviceHolder;
import mn.sync.hikvisionbrige.holders.InstHolder;
import mn.sync.hikvisionbrige.holders.UserHolder;
import mn.sync.hikvisionbrige.models.ActiveUser;
import mn.sync.hikvisionbrige.models.Device;
import mn.sync.hikvisionbrige.models.DigestResponseData;
import mn.sync.hikvisionbrige.models.InstShortInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


public class MainApp {

    private static String BASE_URL = "";

    public static void start(Stage stage) {
        DeviceHolder deviceHolder = DeviceHolder.getInstance();
        ActiveUser activeUser = UserHolder.getInstance().getActiveUser();
        InstShortInfo actInst = InstHolder.getInstance().getInst();

        //Create ComboBox
        ComboBox<Device> comboBox = new ComboBox<>();
        comboBox.setItems(Device.getDeviceList());
        comboBox.setPromptText("Select . . .");
        comboBox.setMaxWidth(295);
        comboBox.getStylesheets().add("custom-combobox.css");
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                BASE_URL = "http://" + newValue.getIpAddress();
            }
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
            String endDate;

            LocalDateTime now = LocalDateTime.now();

            // Set the time zone offset to +08:00
            ZoneOffset zoneOffset = ZoneOffset.ofHours(8);

            // Create a DateTimeFormatter with the desired format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

            // Format the current date and time to a string
            endDate = now.atOffset(zoneOffset).format(formatter);

            Device activeDevice = deviceHolder.getDevice();
            String lastUploadRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/getlastupload","POST","application/json","{\"deviceid\":" + activeDevice.getId() + "}",true);
            if(lastUploadRes == null || lastUploadRes.isEmpty()){
                // Subtract 1 month from the current date
                LocalDateTime oneMonthAgo = now.minusMonths(1);

                // Format the date and time to a string
                startDate = oneMonthAgo.atOffset(zoneOffset).format(formatter);
            }else{
                try {
                    JSONObject lastUpload = new JSONObject(lastUploadRes);
                    String lastUploadDate = lastUpload.getString("uploaddate");
                    DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(lastUploadDate, simpleFormatter);
                    startDate = dateTime.atOffset(zoneOffset).format(formatter);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("startDate: " + startDate);
            System.out.println("endDate: " + endDate);

            String requestBody = "{\"AcsEventCond\":{\"searchID\":\"1\",\"searchResultPosition\":0,\"maxResults\":1000,\"major\":5,\"minor\":75,\"startTime\":\"" + startDate + "\",\"endTime\":\"" + endDate + "\",\"thermometryUnit\":\"celcius\",\"currTemperature\":1}}";
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json",requestBody,"application/json");

            JSONArray sentArray = new JSONArray();
            try {
                JSONObject acsEvent = new JSONObject(responseBody.getBody().toString()).getJSONObject("AcsEvent");
                if(acsEvent.has("InfoList")){
                    JSONArray responseJson = new JSONObject(responseBody.getBody().toString()).getJSONObject("AcsEvent").getJSONArray("InfoList");
                    for(int i = 0; i < responseJson.length(); i++){
                        JSONObject jo = responseJson.getJSONObject(i);
                        JSONObject mapJo = new JSONObject();
                        mapJo.put("personid", jo.getInt("employeeNoString"));
                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(jo.getString("time"));
                        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        mapJo.put("time", offsetDateTime.format(dateFormat));
                        sentArray.put(mapJo);
                    }
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            String uploadResponse = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/inserttimedata","POST","application/json","{\"deviceid\":" + activeDevice.getId() + ", \"timedata\":" + sentArray + "}",true);
            if(uploadResponse == null || uploadResponse.isEmpty() || uploadResponse.isBlank()){
                ImplFunctions.functions.showAlert("Error Message","", "When upload time, occurred error.", Alert.AlertType.ERROR);
                BASE_URL = "";
                comboBox.valueProperty().set(null);
                comboBox.setPromptText("Select . . .");
                return;
            }
            ImplFunctions.functions.showAlert("Success Message","", "Successfully synced time data.", Alert.AlertType.INFORMATION);
        };
        syncBtn.setOnAction(syncEvent);
        hBox.getChildren().add(syncBtn);

        //Create Button to add new data
        Button newBtn = new Button("New");
        EventHandler<ActionEvent> addEmpEvent = e -> {
            if(BASE_URL.isEmpty()){
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

            String reqBody = "<CaptureFaceDataCond version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\"><captureInfrared>false</captureInfrared><dataType>binary</dataType></CaptureFaceDataCond>";
            DigestResponseData captureRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/CaptureFaceData",reqBody,"text/plain");

            //Create response binary text file
            String fileName = "tmp/CaptureFaceData_" + activeUser.getEmpId() + "_" + System.currentTimeMillis() + "_" + ((int)(Math.random() * 99999) + 10000);
            Path path = Paths.get(fileName + ".txt");
            try {
                Files.write(path, (byte[]) captureRes.getBody());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            Path sourcePath = Paths.get(fileName + ".txt");
            Path destinationPath = Paths.get(fileName + ".jpg");
            try (var inputStream = Files.newInputStream(sourcePath);
                 var outputStream = Files.newOutputStream(destinationPath, StandardOpenOption.CREATE)) {

                // Skip the first 14 lines
                int linesToSkip = 14;
                int newlineCount = 0;
                int nextByte;
                while (newlineCount < linesToSkip && (nextByte = inputStream.read()) != -1) {
                    if (nextByte == '\n') {
                        newlineCount++;
                    }
                }

                // Copy the remaining content after skipping
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                Files.delete(sourcePath);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            try {
                Image captureImage = new Image(new ByteArrayInputStream(Files.readAllBytes(destinationPath)));
                ImageView imageView = new ImageView(captureImage);
                imageView.setFitWidth(120);
                imageView.setFitHeight(180);
                root.getChildren().add(imageView);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        };
        newBtn.setOnAction(addEmpEvent);
        hBox.getChildren().add(newBtn);
        root.getChildren().add(hBox);

        //Set config in stage
        stage.setResizable(false);
        stage.setTitle(actInst.getInstShortNameEng() + "-Face Recog Terminal");
        Scene scene = new Scene(root, 345, 310);
        stage.setScene(scene);
        stage.show();
    }
}