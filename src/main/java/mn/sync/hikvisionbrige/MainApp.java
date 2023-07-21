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
import mn.sync.hikvisionbrige.holders.EmpHolder;
import mn.sync.hikvisionbrige.holders.InstHolder;
import mn.sync.hikvisionbrige.holders.UserHolder;
import mn.sync.hikvisionbrige.models.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
    private static DeviceHolder deviceHolder = DeviceHolder.getInstance();
    private static UserHolder userHolder = UserHolder.getInstance();
    private static InstHolder instHolder = InstHolder.getInstance();
    private static EmpHolder empHolder = EmpHolder.getInstance();

    public static void start(Stage stage) {

        //Create ComboBox
        ComboBox<Device> comboBox = new ComboBox<>();
        comboBox.setItems(Device.getDeviceList());
        comboBox.setPromptText("Select . . .");
        comboBox.setMaxWidth(295);
        comboBox.getStylesheets().add("CustomComboBox.css");
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                BASE_URL = "http://" + newValue.getIpAddress();
            }
            comboBox.setStyle("-fx-border-color: none;");
            deviceHolder.setDevice(newValue);
        });

        ComboBox<Employee> empComboBox = new ComboBox<>();
        empComboBox.setItems(Employee.getEmpList());
        empComboBox.setPromptText("Select . . .");
        empComboBox.setMaxWidth(295);
        empComboBox.getStylesheets().add("CustomComboBox.css");
        empComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            empComboBox.setStyle("-fx-border-color: none;");
            empHolder.setEmployee(newValue);
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

        FlowPane empFlowPane = new FlowPane();
        empFlowPane.setHgap(5);
        Label empComboLabel = new Label("Select a employee:");
        empComboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        empFlowPane.getChildren().add(empComboLabel);
        empFlowPane.getChildren().add(empComboBox);
        root.getChildren().add(empFlowPane);

        //Create Separator
        Separator sep = new Separator();
        sep.setHalignment(HPos.CENTER);
        root.getChildren().add(sep);

        //Create HBox, then add Buttons
        HBox hBox = new HBox();
        hBox.setSpacing(10);

        //Create ImageView
        ImageView imageView = new ImageView();

        //Create Button to sync data
        Button syncBtn = new Button("Sync time data");
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
            if(lastUploadRes.startsWith("Request failed")){
                ImplFunctions.functions.showAlert("Error", "",lastUploadRes, Alert.AlertType.ERROR);
                return;
            }
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
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json",requestBody,"application/json", "POST");
            if(responseBody.getContentType().startsWith("Request failed")){
                ImplFunctions.functions.showAlert("Error", "","Request failed with status code: " + responseBody.getBody(), Alert.AlertType.ERROR);
                return;
            }

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
            if(lastUploadRes.startsWith("Request failed")){
                ImplFunctions.functions.showAlert("Error", "",lastUploadRes, Alert.AlertType.ERROR);
                return;
            }
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
        Button newBtn = new Button("Add new employee");
        EventHandler<ActionEvent> addEmpEvent = e -> {
            if(BASE_URL.isEmpty()){
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

            if(empHolder.getEmployee() == null){
                empComboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

            String reqBody = "<CaptureFaceDataCond version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\"><captureInfrared>false</captureInfrared><dataType>binary</dataType></CaptureFaceDataCond>";
            DigestResponseData captureRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/CaptureFaceData",reqBody,"text/plain", "POST");
            if(captureRes.getContentType().startsWith("Request failed")){
                ImplFunctions.functions.showAlert("Error", "","Request failed with status code: " + captureRes.getBody(), Alert.AlertType.ERROR);
                return;
            }

            if("application/xml".equals(captureRes.getContentType())){
                JSONObject capBody = (JSONObject) captureRes.getBody();
                if(capBody.has("CaptureFaceData") && capBody.getJSONObject("CaptureFaceData").has("captureProgress") && capBody.getJSONObject("CaptureFaceData").getInt("captureProgress") == 0){
                    ImplFunctions.functions.showAlert("Warning", "", "Don't capture face data. Try again.", Alert.AlertType.WARNING);
                }else{
                    ImplFunctions.functions.showAlert("Error", "", "When capture face data, occurred error.\n\nResponse:\n" + captureRes.getBody(), Alert.AlertType.ERROR);
                }
            }else{
                //Create response binary text file
                String fileName = "tmp/CaptureFaceData_" + userHolder.getActiveUser().getEmpId() + "_" + System.currentTimeMillis() + "_" + ((int)(Math.random() * 99999) + 10000);
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
                    imageView.setImage(captureImage);
                    imageView.setFitWidth(120);
                    imageView.setFitHeight(180);
                    root.getChildren().add(imageView);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                sendUserData(fileName, imageView, root);
            }
        };
        newBtn.setOnAction(addEmpEvent);
        hBox.getChildren().add(newBtn);
        root.getChildren().add(hBox);

        //Set config in stage
        stage.setResizable(false);
        stage.setTitle(instHolder.getInst().getInstShortNameEng() + " - Face Recog Terminal");
        Scene scene = new Scene(root, 345, 360);
        stage.setScene(scene);
        stage.show();
    }

    public static JSONObject sentFaceRecogTerm(String fileName){
        Integer employeeNo = empHolder.getEmployee().getEmpId();
        String empName = empHolder.getEmployee().getEndUserNameEng();
        String gender = empHolder.getEmployee().getGender();

        String format = "yyyy-MM-dd'T'";
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String beginTime = currentDateTime.format(formatter) + "00:00:00";
        LocalDateTime futureDateTime = currentDateTime.plusYears(10);
        String endTime = futureDateTime.format(formatter) + "23:59:59";

        String requestBody = "{\"UserInfo\": {\"employeeNo\": \"" + employeeNo + "\", \"name\": \"" + empName + "\", \"userType\": \"normal\", \"gender\": \"" + gender + "\", \"localUIRight\":false, \"maxOpenDoorTime\":0, \"Valid\": {\"enable\": true, \"beginTime\": \"" + beginTime + "\", \"endTime\": \"" + endTime + "\", \"timeType\":\"local\"}, \"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\":1,\"planTemplateNo\":\"1\"}],\"userVerifyMode\":\"\"}}";
        DigestResponseData setUserInfoRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/UserInfo/SetUp?format=json",requestBody,"application/json", "PUT");
        if(setUserInfoRes.getContentType().startsWith("Request failed")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + setUserInfoRes.getBody() +  "\"}");
        }

        JSONObject setUserInfoResObj = new JSONObject(setUserInfoRes.getBody().toString());
        if(!setUserInfoResObj.getString("statusString").equals("OK")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + setUserInfoResObj.getString("statusString") + ": " + setUserInfoResObj.getString("subStatusCode") +  "\"}");
        }

        String checkFaceReqBody = "{\n" +
                "    \"searchResultPosition\": 0,\n" +
                "    \"maxResults\": 30,\n" +
                "    \"faceLibType\": \"blackFD\",\n" +
                "    \"FDID\": \"1\",\n" +
                "    \"FPID\": \"" + employeeNo + "\"\n" +
                "}";
        DigestResponseData checkFaceExist = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FDSearch?format=json",checkFaceReqBody,"application/json", "POST");
        if(checkFaceExist.getContentType().startsWith("Request failed")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + checkFaceExist.getBody() +  "\"}");
        }

        JSONObject checkFaceResObj = new JSONObject(checkFaceExist.getBody().toString());
        if(checkFaceResObj.getString("statusString").equals("OK")){
            if(checkFaceResObj.getString("responseStatusStrg").equals("OK")){
                return new JSONObject("{\"code\": \"warning\", \"msg\": \"Face data is already existed.\"}");
            }
        }else{
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkFaceResObj.getString("statusString") + ": " + checkFaceResObj.getString("subStatusCode") +  "\"}");
        }

        String faceDataRecord = "{\"faceLibType\":\"blackFD\",\"FDID\":\"1\",\"FPID\":\"" + employeeNo +"\"}";
        MultipartBody.Builder formDataBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        formDataBuilder.addFormDataPart("FaceDataRecord", faceDataRecord);
        try {
            formDataBuilder.addFormDataPart("asd",fileName, RequestBody.create(Files.readAllBytes(Path.of(fileName + ".jpg")), MediaType.parse("image/jpeg")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        DigestResponseData saveUserFaceRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json",formDataBuilder,"application/json", "POST");
        if(saveUserFaceRes.getContentType().startsWith("Request failed")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + saveUserFaceRes.getBody() +  "\"}");
        }

        JSONObject saveUserFaceResObj = new JSONObject(saveUserFaceRes.getBody().toString());
        if(!saveUserFaceResObj.getString("statusString").equals("OK")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + saveUserFaceResObj.getString("statusString") + ": " + saveUserFaceResObj.getString("subStatusCode") +  "\"}");
        }

        return new JSONObject("{\"code\": \"success\", \"msg\": \"\"}");
    }

    public static JSONObject sentERP(String fileName){
        String checkEmpDicRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic?device=" + deviceHolder.getDevice().getId() + "&personid=" + empHolder.getEmployee().getEmpId(),"GET","application/json","",true);
        if(checkEmpDicRes.startsWith("Request failed")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkEmpDicRes +  "\"}");
        }
        JSONArray jsonArray = new JSONArray(checkEmpDicRes);
        if(jsonArray.length() > 0){
            return new JSONObject("{\"code\": \"warning\", \"msg\": \"Employee is already existed.\"}");
        }

        String photoBase64 = ImplFunctions.functions.convertImageToBase64(fileName + ".jpg");
        String storeDevEmpReqBody = "{\"deviceid\": \"" + deviceHolder.getDevice().getId() + "\", \"personid\": \"" + empHolder.getEmployee().getEmpId() + "\", \"empid\": \"" + empHolder.getEmployee().getEmpId() + "\", \"photo\": \"" + photoBase64 + "\"}";
        String storeDevEmpRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic","POST","application/json",storeDevEmpReqBody,true);
        if(storeDevEmpRes.startsWith("Request failed")){
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + storeDevEmpRes +  "\"}");
        }

        return new JSONObject("{\"code\": \"success\", \"msg\": \"\"}");
    }

    public static void sendUserData(String fileName, ImageView imageView, VBox root){

        JSONObject frtSentStatus = sentFaceRecogTerm(fileName);
        if(frtSentStatus.getString("code").startsWith("success")){
            JSONObject erpSentStatus = sentERP(fileName);
            if(erpSentStatus.getString("code").startsWith("success")){
                ImplFunctions.functions.showAlert("Success", "","Successfully added new employee.", Alert.AlertType.INFORMATION);
            }else if(erpSentStatus.getString("code").startsWith("warning")){
                ImplFunctions.functions.showAlert("Warning", "",erpSentStatus.getString("msg"), Alert.AlertType.INFORMATION);
            }else{
                ImplFunctions.functions.showAlert("Error", "",erpSentStatus.getString("msg"), Alert.AlertType.ERROR);
            }
        }else{
            if(frtSentStatus.getString("code").startsWith("warning")){
                ImplFunctions.functions.showAlert("Warning", "",frtSentStatus.getString("msg"), Alert.AlertType.INFORMATION);
            }else{
                ImplFunctions.functions.showAlert("Error", "",frtSentStatus.getString("msg"), Alert.AlertType.ERROR);
            }
        }

        imageView.setImage(null);
        root.getChildren().remove(imageView);
        try {
            Files.delete(Path.of(fileName + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}