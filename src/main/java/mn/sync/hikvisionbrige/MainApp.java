package mn.sync.hikvisionbrige;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import mn.sync.hikvisionbrige.constants.Components;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import mn.sync.hikvisionbrige.holders.*;
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
import java.util.Base64;

public class MainApp extends Components {

    private static String BASE_URL = "";
    private static DeviceHolder deviceHolder = DeviceHolder.getInstance();
    private static UserHolder userHolder = UserHolder.getInstance();
    private static InstHolder instHolder = InstHolder.getInstance();
    private static EmpHolder empHolder = EmpHolder.getInstance();
    private static Permission permission = checkPermission("TRDeviceEmpDic");

    public static void start(Stage stage) {

        //Create root
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(15, 25, 10, 25));

        //Create tableView
        ObservableList<Object> logList = FXCollections.observableArrayList();
        JSONArray tabCols = new JSONArray();
        String[][] tabColsData = {{"Device Name", "Department", "Username", "Upload date", "Register date"}, {"deviceName", "depNameEng", "endUserNameEng", "uploadDate", "registerDate"}};
        for (int i = 0; i < 5; i++) {
            JSONObject jsonObject = new JSONObject();
            for (int j = 0; j < 2; j++) {
                if (j == 0) {
                    jsonObject.put("name", tabColsData[j][i]);
                } else {
                    jsonObject.put("key", tabColsData[j][i]);
                }
            }
            tabCols.put(jsonObject);
        }

        //Create ComboBox
        ComboBox<Device> comboBox = new ComboBox<>();
        comboBox.setItems(Device.getDeviceList());
        comboBox.setPromptText("Select . . .");
        comboBox.setMaxWidth(295);
        comboBox.getStylesheets().add("CustomComboBox.css");
        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
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

        //Create FlowPane, then add ComboBox
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5);
        Label comboLabel = new Label("Select a device:");
        comboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        flowPane.getChildren().add(comboLabel);
        flowPane.getChildren().add(comboBox);

        FlowPane empFlowPane = new FlowPane();
        empFlowPane.setHgap(5);
        Label empComboLabel = new Label("Select a employee:");
        empComboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        empFlowPane.getChildren().add(empComboLabel);
        empFlowPane.getChildren().add(empComboBox);

        //Create Separators
        Separator sepTop = new Separator();
        sepTop.setHalignment(HPos.CENTER);
        Separator sepDown = new Separator();
        sepDown.setHalignment(HPos.CENTER);

        //Create HBox, then add Buttons
        HBox hBoxBtnTop = new HBox();
        hBoxBtnTop.setSpacing(10);
        HBox hBoxBtnDown = new HBox();
        hBoxBtnTop.setSpacing(10);

        //Create Button to sync data
        Button syncBtn = new Button("Sync time data");
        EventHandler<ActionEvent> syncEvent = e -> {
            if (BASE_URL.isEmpty()) {
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
//            showLoading(stage, true);
            String lastUploadRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/getlastupload", "POST", "application/json", "{\"deviceid\":" + activeDevice.getId() + "}", true);
//            showLoading(stage, false);
            if (lastUploadRes.startsWith("Request failed")) {
                ImplFunctions.functions.showAlert("Error", "", lastUploadRes, Alert.AlertType.ERROR);
                return;
            }
            if (lastUploadRes.isEmpty()) {
                // Subtract 1 month from the current date
                LocalDateTime oneMonthAgo = now.minusMonths(1);

                // Format the date and time to a string
                startDate = oneMonthAgo.atOffset(zoneOffset).format(formatter);
            } else {
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
//            showLoading(stage, true);
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json", requestBody, "application/json", "POST");
//            showLoading(stage, false);
            if (responseBody.getContentType().startsWith("Request failed")) {
                ImplFunctions.functions.showAlert("Error", "", "Request failed with status code: " + responseBody.getBody(), Alert.AlertType.ERROR);
                return;
            }

            JSONArray sentArray = new JSONArray();
            try {
                JSONObject acsEvent = new JSONObject(responseBody.getBody().toString()).getJSONObject("AcsEvent");
                if (acsEvent.has("InfoList")) {
                    JSONArray responseJson = new JSONObject(responseBody.getBody().toString()).getJSONObject("AcsEvent").getJSONArray("InfoList");
                    for (int i = 0; i < responseJson.length(); i++) {
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

//            showLoading(stage, true);
            String uploadResponse = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/inserttimedata", "POST", "application/json", "{\"deviceid\":" + activeDevice.getId() + ", \"timedata\":" + sentArray + "}", true);
//            showLoading(stage, false);
            if (uploadResponse.startsWith("Request failed")) {
                ImplFunctions.functions.showAlert("Error", "", uploadResponse, Alert.AlertType.ERROR);
                return;
            }
            if (uploadResponse.isEmpty() || uploadResponse.isBlank()) {
                ImplFunctions.functions.showAlert("Error Message", "", "When upload time, occurred error.", Alert.AlertType.ERROR);
                BASE_URL = "";
                comboBox.valueProperty().set(null);
                comboBox.setPromptText("Select . . .");
                return;
            }
            ImplFunctions.functions.showAlert("Success Message", "", "Successfully synced time data.", Alert.AlertType.INFORMATION);
        };
        syncBtn.setOnAction(syncEvent);
        hBoxBtnTop.getChildren().add(syncBtn);

        //Create Button to sync employee data
        Button syncEmpData = new Button("Sync employee data");
        EventHandler syncEmpDataEvent = e -> {
            if (BASE_URL.isEmpty()) {
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

            Device activeDevice = deviceHolder.getDevice();
//            showLoading(stage, true);
            String otherDevEmpListRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/getotherdevemps", "POST", "application/json", "{\"deviceid\":" + activeDevice.getId() + "}", true);
//            showLoading(stage, false);
            if (otherDevEmpListRes.startsWith("Request failed")) {
                ImplFunctions.functions.showAlert("Error", "", otherDevEmpListRes, Alert.AlertType.ERROR);
                return;
            }

            JSONArray otherDevEmpList = new JSONArray(otherDevEmpListRes);
//            showLoading(stage, true);
            Boolean isAllSent = true;
            JSONObject sentStatus = null;
            for (int i = 0; i < otherDevEmpList.length(); i++) {
                JSONObject jsonObject = otherDevEmpList.getJSONObject(i);
                sentStatus = syncUserData(stage, jsonObject);
                if (!sentStatus.getBoolean("valid")) {
                    isAllSent = false;
                    break;
                }
            }
//            showLoading(stage, false);

            if (!isAllSent) {
                ImplFunctions.functions.showAlert("Error", "", sentStatus.getString("info"), Alert.AlertType.ERROR);
            } else {
                ImplFunctions.functions.showAlert("Success", "", "Successfully sync employees data.", Alert.AlertType.INFORMATION);
            }
        };
        syncEmpData.setOnAction(syncEmpDataEvent);
        hBoxBtnTop.getChildren().add(syncEmpData);

        //Create Button to add new data
        Button newBtn = new Button("Add new employee");
        EventHandler<ActionEvent> addEmpEvent = e -> {
            if (BASE_URL.isEmpty()) {
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

            if (empHolder.getEmployee() == null) {
                empComboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                return;
            }

//            showLoading(stage, true);
            String reqBody = "<CaptureFaceDataCond version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\"><captureInfrared>false</captureInfrared><dataType>binary</dataType></CaptureFaceDataCond>";
            DigestResponseData captureRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/CaptureFaceData", reqBody, "text/plain", "POST");
//            showLoading(stage, false);
            if (captureRes.getContentType().startsWith("Request failed")) {
                ImplFunctions.functions.showAlert("Error", "", "Request failed with status code: " + captureRes.getBody(), Alert.AlertType.ERROR);
                return;
            }

            if ("application/xml".equals(captureRes.getContentType())) {
                JSONObject capBody = (JSONObject) captureRes.getBody();
                if (capBody.has("CaptureFaceData") && capBody.getJSONObject("CaptureFaceData").has("captureProgress") && capBody.getJSONObject("CaptureFaceData").getInt("captureProgress") == 0) {
                    ImplFunctions.functions.showAlert("Warning", "", "Don't capture face data. Try again.", Alert.AlertType.WARNING);
                } else {
                    ImplFunctions.functions.showAlert("Error", "", "When capture face data, occurred error.\n\nResponse:\n" + captureRes.getBody(), Alert.AlertType.ERROR);
                }
            } else {
                //Create response binary text file
                String fileName = "tmp/CaptureFaceData_" + userHolder.getActiveUser().getEmpId() + "_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 99999) + 10000);
                Path sourcePath = Paths.get(fileName + ".txt");
                try {
                    Files.write(sourcePath, (byte[]) captureRes.getBody());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                Path destinationPath = Paths.get(fileName + ".jpg");
                try (var inputStream = Files.newInputStream(sourcePath); var outputStream = Files.newOutputStream(destinationPath, StandardOpenOption.CREATE)) {

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

                sendUserData(stage, fileName, root);
            }
        };
        newBtn.setOnAction(addEmpEvent);
        hBoxBtnDown.getChildren().add(newBtn);

        //Add children to root
        if (permission.getUpdate()) {
            root.getChildren().add(getSeparatorWithLabel("Device section"));
            root.getChildren().add(flowPane);
            root.getChildren().add(sepTop);
            root.getChildren().add(hBoxBtnTop);
            root.getChildren().add(getSeparatorWithLabel("Employee section"));
            root.getChildren().add(empFlowPane);
            root.getChildren().add(sepDown);
            root.getChildren().add(hBoxBtnDown);
            root.getChildren().add(Components.getTableWithScroll(logList, tabCols));
        } else if (permission.getRead()) {
            root.getChildren().add(getSeparatorWithLabel("Device section"));
            root.getChildren().add(flowPane);
            root.getChildren().add(sepTop);
            root.getChildren().add(hBoxBtnTop);
            root.getChildren().add(Components.getTableWithScroll(logList, tabCols));
        } else {
            Label permDenied = new Label("Permission denied.");
            permDenied.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
            permDenied.setTextFill(Color.web("#F00"));
            HBox contain = new HBox();
            contain.setPadding(new Insets(10));
            contain.getChildren().add(permDenied);
            root.getChildren().add(contain);
        }

        //Set config in stage
        stage.setResizable(false);
        stage.setTitle(instHolder.getInst().getInstShortNameEng() + " - Face Recog Terminal");
        Scene scene = new Scene(root, 345, permission.getUpdate() ? 480 : 350);
        stage.setScene(scene);
        stage.show();
    }

    public static Permission checkPermission(String perm) {
        String checkPermRes = ImplFunctions.functions.ErpApiService("/checkpermemp?perm=" + perm + "&personid=" + userHolder.getActiveUser().getEmpId(), "GET", "application/json", "", true);
        if (checkPermRes.startsWith("Request failed")) {
            return null;
        }

        JSONObject resObj = new JSONObject(checkPermRes);
        Permission newPerm = new Permission(resObj.getString("permid"), resObj.getString("rolename"), resObj.getBoolean("read"), resObj.getBoolean("update"), resObj.getBoolean("change"), resObj.getBoolean("create"), resObj.getBoolean("delete"));
        PermissionHolder.getInstance().setPermission(newPerm);
        return newPerm;
    }

    public static JSONObject sendEmpDataFaceRecogTerm(Stage stage, Object data) {
        Integer employeeNo;
        String empName;
        String gender;
        JSONObject employee;
        JSONObject empPhoto = null;
        if (data instanceof JSONObject) {
            employeeNo = ((JSONObject) data).getInt("empid");
            employee = ((JSONObject) data).getJSONObject("employee");
            empPhoto = ((JSONObject) data).getJSONObject("empphoto");
            empName = employee.getString("empfnameeng").toUpperCase() + " " + employee.getString("emplnameeng");
            gender = employee.getInt("gender") == 1 ? "male" : employee.getInt("gender") == 2 ? "female" : "unknown";
        } else {
            employeeNo = empHolder.getEmployee().getEmpId();
            empName = empHolder.getEmployee().getEndUserNameEng();
            gender = empHolder.getEmployee().getGender();
        }

        String format = "yyyy-MM-dd'T'";
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        String beginTime = currentDateTime.format(formatter) + "00:00:00";
        LocalDateTime futureDateTime = currentDateTime.plusYears(10);
        String endTime = futureDateTime.format(formatter) + "23:59:59";

//        showLoading(stage, true);
        String requestBody = "{\"UserInfo\": {\"employeeNo\": \"" + employeeNo + "\", \"name\": \"" + empName + "\", \"userType\": \"normal\", \"gender\": \"" + gender + "\", \"localUIRight\":false, \"maxOpenDoorTime\":0, \"Valid\": {\"enable\": true, \"beginTime\": \"" + beginTime + "\", \"endTime\": \"" + endTime + "\", \"timeType\":\"local\"}, \"doorRight\":\"1\",\"RightPlan\":[{\"doorNo\":1,\"planTemplateNo\":\"1\"}],\"userVerifyMode\":\"\"}}";
        DigestResponseData setUserInfoRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/UserInfo/SetUp?format=json", requestBody, "application/json", "PUT");
//        showLoading(stage, false);
        if (setUserInfoRes.getContentType().startsWith("Request failed")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + setUserInfoRes.getBody() + "\"}");
        }

        JSONObject setUserInfoResObj = new JSONObject(setUserInfoRes.getBody().toString());
        if (!setUserInfoResObj.getString("statusString").equals("OK")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + setUserInfoResObj.getString("statusString") + ": " + setUserInfoResObj.getString("subStatusCode") + "\"}");
        }

//        showLoading(stage, true);
        String checkFaceReqBody = "{\n" + "    \"searchResultPosition\": 0,\n" + "    \"maxResults\": 30,\n" + "    \"faceLibType\": \"blackFD\",\n" + "    \"FDID\": \"1\",\n" + "    \"FPID\": \"" + employeeNo + "\"\n" + "}";
        DigestResponseData checkFaceExist = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FDSearch?format=json", checkFaceReqBody, "application/json", "POST");
//        showLoading(stage, false);
        if (checkFaceExist.getContentType().startsWith("Request failed")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + checkFaceExist.getBody() + "\"}");
        }

        JSONObject checkFaceResObj = new JSONObject(checkFaceExist.getBody().toString());
        if (checkFaceResObj.getString("statusString").equals("OK")) {
            if (checkFaceResObj.getString("responseStatusStrg").equals("OK")) {
                return new JSONObject("{\"code\": \"warning\", \"msg\": \"Face data is already existed.\"}");
            }
        } else {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkFaceResObj.getString("statusString") + ": " + checkFaceResObj.getString("subStatusCode") + "\"}");
        }

        String faceDataRecord = "{\"faceLibType\":\"blackFD\",\"FDID\":\"1\",\"FPID\":\"" + employeeNo + "\"}";
        MultipartBody.Builder formDataBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        formDataBuilder.addFormDataPart("FaceDataRecord", faceDataRecord);
        String fileName;
        byte[] photoBytes;
        if (data instanceof JSONObject) {
            fileName = "tmp/CaptureFaceData_" + employeeNo + "_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 99999) + 10000);
            String empPhotoBase64 = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/showdevempphoto", "POST", "application/json", "{\"photoid\": " + empPhoto.getInt("id") + "}", true);
            if (empPhotoBase64.equalsIgnoreCase("unknown")) {
                return new JSONObject("{\"code\": \"error\", \"msg\": \"Not found image\"}");
            } else {
                photoBytes = Base64.getDecoder().decode(empPhotoBase64);
            }
        } else {
            fileName = String.valueOf(data);
            try {
                photoBytes = Files.readAllBytes(Path.of(fileName + ".jpg"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        formDataBuilder.addFormDataPart("asd", fileName, RequestBody.create(photoBytes, MediaType.parse("image/jpeg")));

//        showLoading(stage, true);
        DigestResponseData saveUserFaceRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json", formDataBuilder, "application/json", "POST");
//        showLoading(stage, false);
        if (saveUserFaceRes.getContentType().startsWith("Request failed")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + saveUserFaceRes.getBody() + "\"}");
        }

        JSONObject saveUserFaceResObj = new JSONObject(saveUserFaceRes.getBody().toString());
        if (!saveUserFaceResObj.getString("statusString").equals("OK")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + saveUserFaceResObj.getString("statusString") + ": " + saveUserFaceResObj.getString("subStatusCode") + "\"}");
        }

        return new JSONObject("{\"code\": \"success\", \"msg\": \"\"}");
    }

    public static JSONObject sendEmpDataERP(Stage stage, Object data) {

        Integer personId;
        JSONObject empPhoto = null;
        Integer deviceId = deviceHolder.getDevice().getId();
        if (data instanceof JSONObject) {
            personId = ((JSONObject) data).getInt("empid");
            empPhoto = ((JSONObject) data).getJSONObject("empphoto");
        } else {
            personId = empHolder.getEmployee().getEmpId();
        }
//        showLoading(stage, true);
        String checkEmpDicRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic?deviceid=" + deviceId + "&personid=" + personId, "GET", "application/json", "", true);
//        showLoading(stage, false);
        if (checkEmpDicRes.startsWith("Request failed")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkEmpDicRes + "\"}");
        }
        JSONArray jsonArray = new JSONArray(checkEmpDicRes);
        if (jsonArray.length() > 0) {
            return new JSONObject("{\"code\": \"warning\", \"msg\": \"Employee is already existed.\"}");
        }

        String photoBase64;
        if (data instanceof JSONObject) {
            photoBase64 = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/showdevempphoto", "POST", "application/json", "{\"photoid\": " + empPhoto.getInt("id") + "}", true);
            if (photoBase64.equalsIgnoreCase("unknown")) {
                return new JSONObject("{\"code\": \"error\", \"msg\": \"Not found image\"}");
            }
        } else {
            photoBase64 = ImplFunctions.functions.convertImageToBase64(data + ".jpg");
        }
//        showLoading(stage, true);
        String storeDevEmpReqBody = "{\"deviceid\": \"" + deviceId + "\", \"personid\": \"" + personId + "\", \"empid\": \"" + personId + "\", \"photo\": \"" + photoBase64 + "\"}";
        String storeDevEmpRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic", "POST", "application/json", storeDevEmpReqBody, true);
//        showLoading(stage, false);
        if (storeDevEmpRes.startsWith("Request failed")) {
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + storeDevEmpRes + "\"}");
        }

        return new JSONObject("{\"code\": \"success\", \"msg\": \"\"}");
    }

    public static void sendUserData(Stage stage, String fileName, VBox root) {

        JSONObject frtSentStatus = sendEmpDataFaceRecogTerm(stage, fileName);
        if (frtSentStatus.getString("code").startsWith("success")) {
            JSONObject erpSentStatus = sendEmpDataERP(stage, fileName);
            if (erpSentStatus.getString("code").startsWith("success")) {
                ImplFunctions.functions.showAlert("Success", "", "Successfully added new employee.", Alert.AlertType.INFORMATION);
            } else if (erpSentStatus.getString("code").startsWith("warning")) {
                ImplFunctions.functions.showAlert("Warning", "", erpSentStatus.getString("msg"), Alert.AlertType.INFORMATION);
            } else {
                ImplFunctions.functions.showAlert("Error", "", erpSentStatus.getString("msg"), Alert.AlertType.ERROR);
            }
        } else {
            if (frtSentStatus.getString("code").startsWith("warning")) {
                ImplFunctions.functions.showAlert("Warning", "", frtSentStatus.getString("msg"), Alert.AlertType.INFORMATION);
            } else {
                ImplFunctions.functions.showAlert("Error", "", frtSentStatus.getString("msg"), Alert.AlertType.ERROR);
            }
        }

        try {
            Files.delete(Path.of(fileName + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject syncUserData(Stage stage, JSONObject jsonObject) {

        JSONObject response = new JSONObject();
        JSONObject employee = jsonObject.getJSONObject("employee");
        if (employee.getInt("status") == 2) {
            Boolean deleteUserFRTStatus = deleteEmpDataFaceRecogTerm(stage, jsonObject);
            if (deleteUserFRTStatus) {
                Boolean deleteUserERPStatus = deleteEmpDataERP(stage, jsonObject);
                response.put("valid", deleteUserERPStatus);
                if (deleteUserERPStatus) {
                    response.put("info", "Success.");
                } else {
                    response.put("info", "When delete face data from Face Recognition Terminal, occurred error.");
                }
                return response;
            }
            response.put("valid", false);
            response.put("info", "When delete face data from Face Recognition Terminal, occurred error.");
            return response;
        } else {
            JSONObject frtSentStatus = sendEmpDataFaceRecogTerm(stage, jsonObject);
            if (frtSentStatus.getString("code").startsWith("success")) {
                JSONObject erpSentStatus = sendEmpDataERP(stage, jsonObject);
                if (erpSentStatus.getString("code").startsWith("success") || (erpSentStatus.getString("code").startsWith("error") && erpSentStatus.getString("msg").startsWith("Not found image"))) {
                    response.put("valid", true);
                    response.put("info", "Success.");
                    return response;
                } else {
                    response.put("valid", false);
                    response.put("info", erpSentStatus.getString("msg"));
                    return response;
                }
            } else if (frtSentStatus.getString("code").startsWith("error") && frtSentStatus.getString("msg").startsWith("Not found image")) {
                response.put("valid", true);
                response.put("info", "Success.");
                return response;
            } else {
                response.put("valid", false);
                response.put("info", frtSentStatus.getString("msg"));
                return response;
            }
        }
    }

    public static boolean deleteEmpDataFaceRecogTerm(Stage stage, JSONObject jsonObject) {
        Integer employeeNo = jsonObject.getInt("personid");
        String delUserBody = "{\"UserInfoDelCond\":{\"EmployeeNoList\":[{\"employeeNo\":\"" + employeeNo + "\"}]}}";
        DigestResponseData deleteUserStatus = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/UserInfo/Delete?format=json", delUserBody, "application/json", "PUT");
        if (deleteUserStatus.getContentType().startsWith("Request failed")) {
            return false;
        }
        return true;
    }

    public static boolean deleteEmpDataERP(Stage stage, JSONObject jsonObject) {
        Integer dicId = jsonObject.getInt("dicid");
        String delEmpDicBody = "{\"dicid\":" + dicId + "}";
        String delEmpDicRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/delempdic", "POST", "application/json", delEmpDicBody, true);
        if (delEmpDicRes.startsWith("Request failed")) {
            return false;
        }
        return true;
    }

    public static void showLoading(Stage stage, Boolean loading) {
        getSpinningLoader(stage, loading);
    }

    @Override
    public void draw() {
        System.out.println("Drawing...");
    }
}