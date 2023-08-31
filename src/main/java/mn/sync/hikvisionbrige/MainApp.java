package mn.sync.hikvisionbrige;

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
import javafx.util.StringConverter;
import mn.sync.hikvisionbrige.constants.Components;
import mn.sync.hikvisionbrige.constants.FxUtils;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import mn.sync.hikvisionbrige.holders.*;
import mn.sync.hikvisionbrige.models.*;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final DeviceHolder deviceHolder = DeviceHolder.getInstance();
    private static final UserHolder userHolder = UserHolder.getInstance();
    private static final InstHolder instHolder = InstHolder.getInstance();
    private static final EmpHolder empHolder = EmpHolder.getInstance();
    private static TableView<LogData> logTableView = null;
    private static Logger logger = LogManager.getLogger(MainApp.class);

    public static void start(Stage stage) {

        final Permission permission = checkPermission("TRDeviceEmpDic");

        //Create root
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(15, 25, 10, 25));

        //Create ComboBox
        ComboBox<InstShortInfo> instComboBox = new ComboBox<>();
        instComboBox.setItems(InstShortInfo.getInstList());
        instComboBox.setPromptText("Select . . .");
        instComboBox.setMaxWidth(295);
        instComboBox.getStylesheets().add("CustomComboBox.css");
        instComboBox.setValue(instHolder.getInst());
        instComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            instComboBox.setStyle("-fx-border-color: none;");
            instHolder.setInst(newValue);
        });
        FxUtils.autoCompleteComboBoxPlus(instComboBox, (typedText, itemToCompare) -> itemToCompare.getInstName().toLowerCase().contains(typedText.toLowerCase()) || itemToCompare.getInstEngName().equals(typedText));
        instComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(InstShortInfo object) {
                return object != null ? object.getInstName() : "";
            }

            @Override
            public InstShortInfo fromString(String string) {
                return instComboBox.getItems().stream().filter(object -> object.getInstName().equals(string)).findFirst().orElse(null);
            }

        });

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
        FxUtils.autoCompleteComboBoxPlus(comboBox, (typedText, itemToCompare) -> itemToCompare.getName().toLowerCase().contains(typedText.toLowerCase()) || itemToCompare.getIpAddress().equals(typedText));
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Device object) {
                return object != null ? object.getName() : "";
            }

            @Override
            public Device fromString(String string) {
                return comboBox.getItems().stream().filter(object -> object.getName().equals(string)).findFirst().orElse(null);
            }

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
        FxUtils.autoCompleteComboBoxPlus(empComboBox, (typedText, itemToCompare) -> itemToCompare.getEndUserNameEng().toLowerCase().contains(typedText.toLowerCase()) || itemToCompare.getEndUserName().equals(typedText));
        empComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Employee object) {
                return object != null ? object.getEndUserNameEng() : "";
            }

            @Override
            public Employee fromString(String string) {
                return empComboBox.getItems().stream().filter(object -> object.getEndUserNameEng().equals(string)).findFirst().orElse(null);
            }

        });

        //Create FlowPane, then add ComboBox
        FlowPane instFlowPane = new FlowPane();
        instFlowPane.setHgap(5);
        Label instComboLabel = new Label("Select a institution:");
        instComboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        instFlowPane.getChildren().add(instComboLabel);
        instFlowPane.getChildren().add(instComboBox);

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(5);
        Label comboLabel = new Label("Select a device:");
        comboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        flowPane.getChildren().add(comboLabel);
        flowPane.getChildren().add(comboBox);

        VBox vBox = new VBox();
        vBox.setSpacing(10);

        FlowPane empFlowPane = new FlowPane();
        empFlowPane.setHgap(5);
        Label empComboLabel = new Label("Select a employee:");
        empComboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
        empFlowPane.getChildren().add(empComboLabel);
        empFlowPane.getChildren().add(empComboBox);
        vBox.getChildren().add(empFlowPane);

        FlowPane empCardFlowPane = new FlowPane();
        empCardFlowPane.setHgap(5);
        Label empCardLabel = new Label("Input card number:");
        empCardLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));

        TextField empCardTF = new TextField();
        empCardTF.setPrefWidth(295);
        empCardTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                empCardTF.setStyle("-fx-border-color: none;");
            }
        });
        empCardFlowPane.getChildren().add(empCardLabel);
        empCardFlowPane.getChildren().add(empCardTF);
        vBox.getChildren().add(empCardFlowPane);

        //Create Separators
        Separator instSep = new Separator();
        instSep.setHalignment(HPos.CENTER);
        Separator sepTop = new Separator();
        sepTop.setHalignment(HPos.CENTER);
        Separator sepDown = new Separator();
        sepDown.setHalignment(HPos.CENTER);

        //Create HBox, then add Buttons
        HBox instBtn = new HBox();
        instBtn.setSpacing(10);
        HBox hBoxBtnTop = new HBox();
        hBoxBtnTop.setSpacing(10);
        HBox hBoxBtnDown = new HBox();
        hBoxBtnDown.setSpacing(10);

        //Create Button to change institution
        Button changeBtn = new Button("Change");
        EventHandler<ActionEvent> changeInst = e -> {
            if (instHolder.getInst() == null) {
                instComboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Institution field is required.");
                return;
            }

            //showLoading(stage, true);
            String response = ImplFunctions.functions.ErpApiService("/auth/changeinst", "POST", "application/json", "{\"instid\":" + instHolder.getInst().getInstId() + "}", true);
            //showLoading(stage, false);
            if (response.startsWith("Request failed")) {
                logger.error("When change institution, occurred error: " + response);
                ImplFunctions.functions.showAlert("Error", "", response, Alert.AlertType.ERROR);
                return;
            }

            if (response.equals("\"success\"")) {
                logger.info("Successfully changed institution.");
//                stage.close();
                MainApp.start(stage);
            }

        };
        changeBtn.setOnAction(changeInst);
        instBtn.getChildren().add(changeBtn);

        //Create Button to sync data
        Button syncBtn = new Button("Sync time data");
        EventHandler<ActionEvent> syncEvent = e -> {
            if (BASE_URL.isEmpty() || BASE_URL.equals("http://")) {
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Device field is required.");
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
                logger.error("Get last upload date: " + lastUploadRes);
                ImplFunctions.functions.showAlert("Error", "", lastUploadRes, Alert.AlertType.ERROR);
                return;
            }
            if (lastUploadRes.isEmpty() || lastUploadRes.equals("null")) {
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
                    logger.info("Last upload date is successfully formatted.");
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    logger.error("When format last upload date, occurred error.");
                }
            }
            System.out.println("startDate: " + startDate);
            System.out.println("endDate: " + endDate);

            JSONArray sentArray = getAcsEvents(startDate, endDate);

//            showLoading(stage, true);
            String uploadResponse = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/inserttimedata", "POST", "application/json", "{\"deviceid\":" + activeDevice.getId() + ", \"timedata\":" + sentArray + "}", true);
//            showLoading(stage, false);
            if (uploadResponse.startsWith("Request failed")) {
                logger.error("Insert time data error: " + uploadResponse);
                ImplFunctions.functions.showAlert("Error", "", uploadResponse, Alert.AlertType.ERROR);
                return;
            }
            if (uploadResponse.isEmpty() || uploadResponse.isBlank()) {
                logger.error("When insert time data to ERP, occurred error.");
                ImplFunctions.functions.showAlert("Error Message", "", "When upload time, occurred error.", Alert.AlertType.ERROR);
                BASE_URL = "";
                comboBox.valueProperty().set(null);
                comboBox.setPromptText("Select . . .");
                return;
            }
            logger.info("Successfully inserted time data to ERP.");
            logger.info(sentArray.length() + " rows time data inserted.");
            ImplFunctions.functions.showAlert("Success Message", "", "Successfully synced time data.", Alert.AlertType.INFORMATION);
        };
        syncBtn.setOnAction(syncEvent);
        hBoxBtnTop.getChildren().add(syncBtn);

        //Create Button to sync employee data
        Button syncEmpData = new Button("Sync employee data");
        EventHandler syncEmpDataEvent = e -> {
            if (BASE_URL.isEmpty() || BASE_URL.equals("http://")) {
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Device field is required.");
                return;
            }

            Device activeDevice = deviceHolder.getDevice();
//            showLoading(stage, true);
            String otherDevEmpListRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/getotherdevemps", "POST", "application/json", "{\"deviceid\":" + activeDevice.getId() + "}", true);
//            showLoading(stage, false);
            if (otherDevEmpListRes.startsWith("Request failed")) {
                logger.error("Get other device employees error: " + otherDevEmpListRes);
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
                logger.error("When insert employee data to ERP, occurred error.");
                logger.error(sentStatus.getString("info"));
                ImplFunctions.functions.showAlert("Error", "", sentStatus.getString("info"), Alert.AlertType.ERROR);
            } else {
                logger.info("Successfully sync employees data.");
                ImplFunctions.functions.showAlert("Success", "", "Successfully sync employees data.", Alert.AlertType.INFORMATION);
            }
        };
        syncEmpData.setOnAction(syncEmpDataEvent);
        hBoxBtnTop.getChildren().add(syncEmpData);

        //Create Button to add new data
        Button newFaceBtn = new Button("Add face data");
        EventHandler<ActionEvent> addEmpFaceEvent = e -> {
            if (BASE_URL.isEmpty() || BASE_URL.equals("http://")) {
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Device field is required.");
                return;
            }

            if (empHolder.getEmployee() == null) {
                empComboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Employee field is required.");
                return;
            }

//            showLoading(stage, true);
            String reqBody = "<CaptureFaceDataCond version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\"><captureInfrared>false</captureInfrared><dataType>binary</dataType></CaptureFaceDataCond>";
            DigestResponseData captureRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/CaptureFaceData", reqBody, "text/plain", "POST");
//            showLoading(stage, false);
            if (captureRes.getContentType().startsWith("Request failed")) {
                logger.error("Capture face data error: " + captureRes.getBody());
                ImplFunctions.functions.showAlert("Error", "", "Request failed with status code: " + captureRes.getBody(), Alert.AlertType.ERROR);
                return;
            }

            if ("application/xml".equals(captureRes.getContentType())) {
                JSONObject capBody = (JSONObject) captureRes.getBody();
                if (capBody.has("CaptureFaceData") && capBody.getJSONObject("CaptureFaceData").has("captureProgress") && capBody.getJSONObject("CaptureFaceData").getInt("captureProgress") == 0) {
                    logger.warn("Don't capture dace data. Try again.");
                    ImplFunctions.functions.showAlert("Warning", "", "Don't capture face data. Try again.", Alert.AlertType.WARNING);
                } else {
                    logger.error("When capture face data, occurred error.\n\nResponse:\n" + captureRes.getBody());
                    ImplFunctions.functions.showAlert("Error", "", "When capture face data, occurred error.\n\nResponse:\n" + captureRes.getBody(), Alert.AlertType.ERROR);
                }
            } else {
                //Create response binary text file
                String fileName = "tmp/CaptureFaceData_" + userHolder.getActiveUser().getEmpId() + "_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 99999) + 10000);
                Path sourcePath = Paths.get(fileName + ".txt");
                try {
                    Files.write(sourcePath, (byte[]) captureRes.getBody());
                    logger.info(fileName + ".txt file created.");
                } catch (IOException ex) {
                    logger.error(fileName + ".txt file cannot write.");
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

                    logger.info(fileName + ".jpg file created.");
                    logger.info(fileName + ".txt file deleted.");
                    Files.delete(sourcePath);
                } catch (IOException ex) {
                    logger.error(fileName + ".jpg file cannot write or source text file cannot delete.");
                    ex.printStackTrace();
                }

                sendUserData(stage, fileName, "", root);
            }
        };
        newFaceBtn.setOnAction(addEmpFaceEvent);
        hBoxBtnDown.getChildren().add(newFaceBtn);

        Button newCardBtn = new Button("Add card");
        EventHandler<ActionEvent> addEmpCardEvent = e -> {
            if (BASE_URL.isEmpty() || BASE_URL.equals("http://")) {
                comboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Device field is required.");
                return;
            }

            if (empHolder.getEmployee() == null) {
                empComboBox.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Employee field is required.");
                return;
            }

            if (empCardTF.getText().isEmpty() || empCardTF.getText().isBlank()) {
                empCardTF.setStyle("-fx-border-color: #f00;-fx-border-radius: 3px;");
                logger.warn("Card number field is required.");
                return;
            }
            sendUserData(stage, "", empCardTF.getText(), root);
        };
        newCardBtn.setOnAction(addEmpCardEvent);
        hBoxBtnDown.getChildren().add(newCardBtn);

        //Add children to root
        if (permission.getCreate()) {
            root.getChildren().add(getSeparatorWithLabel("Institution"));
            root.getChildren().add(instFlowPane);
            root.getChildren().add(instSep);
            root.getChildren().add(instBtn);
            root.getChildren().add(getSeparatorWithLabel("Device section"));
            root.getChildren().add(flowPane);
            root.getChildren().add(sepTop);
            root.getChildren().add(hBoxBtnTop);
            root.getChildren().add(getSeparatorWithLabel("Employee section"));
            root.getChildren().add(vBox);
            root.getChildren().add(sepDown);
            root.getChildren().add(hBoxBtnDown);
        } else if (permission.getRead()) {
            root.getChildren().add(getSeparatorWithLabel("Institution"));
            root.getChildren().add(instFlowPane);
            root.getChildren().add(instSep);
            root.getChildren().add(instBtn);
            root.getChildren().add(getSeparatorWithLabel("Device section"));
            root.getChildren().add(flowPane);
            root.getChildren().add(sepTop);
            root.getChildren().add(hBoxBtnTop);
        } else {
            Label permDenied = new Label("Permission denied.");
            permDenied.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));
            permDenied.setTextFill(Color.web("#F00"));
            HBox contain = new HBox();
            contain.setPadding(new Insets(10));
            contain.getChildren().add(permDenied);
            root.getChildren().add(contain);
        }
        logTableView = logTableView == null ? Components.getLogTableView() : logTableView;
        root.getChildren().add(logTableView);

        //Set config in stage
        stage.setResizable(true);
        stage.setMinWidth(360);
        stage.setTitle(instHolder.getInst().getInstShortNameEng() + " - Face Recog Terminal");
        Scene scene = new Scene(root, 345, permission.getCreate() ? 630 : permission.getRead() ? 500 : 260);
        stage.setScene(scene);
        stage.show();

        logger.info("Login is successful.");
        logger.info("Face recognition terminal is started.");
    }

    public static Permission checkPermission(String perm) {
        String checkPermRes = ImplFunctions.functions.ErpApiService("/checkperm?perm=" + perm, "GET", "application/json", "", true);
        if (checkPermRes.startsWith("Request failed")) {
            logger.error(checkPermRes);
            return null;
        }

        JSONObject resObj = new JSONObject(checkPermRes);
        Permission newPerm = new Permission(resObj.getString("permid"), resObj.getString("rolename"), resObj.getBoolean("read"), resObj.getBoolean("update"), resObj.getBoolean("change"), resObj.getBoolean("create"), resObj.getBoolean("delete"));
        PermissionHolder.getInstance().setPermission(newPerm);
        logger.info("Permission checked.");
        return newPerm;
    }

    public static JSONObject sendEmpDataFaceRecogTerm(Stage stage, Object data, String cardNo) {
        Integer employeeNo;
        String empName;
        String gender;
        JSONObject employee;
        JSONObject empPhoto = null;
        if (data instanceof JSONObject) {
            employeeNo = ((JSONObject) data).getInt("empid");
            employee = ((JSONObject) data).getJSONObject("employee");
            empPhoto = ((JSONObject) data).isNull("empphoto") ? null : ((JSONObject) data).getJSONObject("empphoto");
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
            logger.error("UserInfo SetUp Error: " + setUserInfoRes.getBody());
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + setUserInfoRes.getBody() + "\"}");
        }

        if ((data instanceof String && !data.toString().isEmpty()) || data instanceof JSONObject) {

            JSONObject setUserInfoResObj = new JSONObject(setUserInfoRes.getBody().toString());
            if (!setUserInfoResObj.getString("statusString").equals("OK")) {
                logger.error(setUserInfoResObj.getString("statusString") + ": " + setUserInfoResObj.getString("subStatusCode"));
                return new JSONObject("{\"code\": \"error\", \"msg\": \"" + setUserInfoResObj.getString("statusString") + ": " + setUserInfoResObj.getString("subStatusCode") + "\"}");
            }

//        showLoading(stage, true);
            String checkFaceReqBody = "{\n" + "    \"searchResultPosition\": 0,\n" + "    \"maxResults\": 30,\n" + "    \"faceLibType\": \"blackFD\",\n" + "    \"FDID\": \"1\",\n" + "    \"FPID\": \"" + employeeNo + "\"\n" + "}";
            DigestResponseData checkFaceExist = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FDSearch?format=json", checkFaceReqBody, "application/json", "POST");
//        showLoading(stage, false);
            if (checkFaceExist.getContentType().startsWith("Request failed")) {
                logger.error("FDLib FDSearch Error: " + checkFaceExist.getBody());
                return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + checkFaceExist.getBody() + "\"}");
            }

            JSONObject checkFaceResObj = new JSONObject(checkFaceExist.getBody().toString());
            if (checkFaceResObj.getString("statusString").equals("OK")) {
                if (checkFaceResObj.getString("responseStatusStrg").equals("OK")) {
                    logger.warn("Face data is already existed.");
                    return new JSONObject("{\"code\": \"warning\", \"msg\": \"Face data is already existed.\"}");
                }
            } else {
                logger.error(checkFaceResObj.getString("statusString") + ": " + checkFaceResObj.getString("subStatusCode"));
                return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkFaceResObj.getString("statusString") + ": " + checkFaceResObj.getString("subStatusCode") + "\"}");
            }

            String faceDataRecord = "{\"faceLibType\":\"blackFD\",\"FDID\":\"1\",\"FPID\":\"" + employeeNo + "\"}";
            MultipartBody.Builder formDataBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            formDataBuilder.addFormDataPart("FaceDataRecord", faceDataRecord);
            String fileName;
            byte[] photoBytes = null;
            if (data instanceof JSONObject) {
                fileName = "tmp/CaptureFaceData_" + employeeNo + "_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 99999) + 10000);
                String empPhotoBase64 = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/showdevempphoto", "POST", "application/json", "{\"photoid\": " + (empPhoto == null ? -1 : empPhoto.getInt("id")) + "}", true);
                if (empPhotoBase64.equalsIgnoreCase("unknown")) {
                    logger.warn(empName + "'s image not found!");
//                    return new JSONObject("{\"code\": \"error\", \"msg\": \"Not found image\"}");
                } else {
                    photoBytes = Base64.getDecoder().decode(empPhotoBase64);
                }
            } else {
                fileName = String.valueOf(data);
                try {
                    photoBytes = Files.readAllBytes(Path.of(fileName + ".jpg"));
                } catch (IOException e) {
                    logger.error(fileName + ".jpg cannot read.");
                    throw new RuntimeException(e);
                }
            }

            if (photoBytes != null) {
                formDataBuilder.addFormDataPart("asd", fileName, RequestBody.create(photoBytes, MediaType.parse("image/jpeg")));

//                showLoading(stage, true);
                DigestResponseData saveUserFaceRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json", formDataBuilder, "application/json", "POST");
//                showLoading(stage, false);
                if (saveUserFaceRes.getContentType().startsWith("Request failed")) {
                    logger.error("FDLib FaceDataRecord Error: " + saveUserFaceRes.getBody());
                    return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + saveUserFaceRes.getBody() + "\"}");
                }

                JSONObject saveUserFaceResObj = new JSONObject(saveUserFaceRes.getBody().toString());
                if (!saveUserFaceResObj.getString("statusString").equals("OK")) {
                    logger.error(saveUserFaceResObj.getString("statusString") + ": " + saveUserFaceResObj.getString("subStatusCode"));
                    return new JSONObject("{\"code\": \"error\", \"msg\": \"" + saveUserFaceResObj.getString("statusString") + ": " + saveUserFaceResObj.getString("subStatusCode") + "\"}");
                }
            }
        }

        if (!(cardNo.isEmpty() || cardNo.isBlank())) {

            String checkCardReqBody = "{\n" + "  \"CardInfoSearchCond\":{\n" + "    \"searchID\":\"1\",\n" + "    \"searchResultPosition\": 0,\n" + "    \"maxResults\": 30,\n" + "    \"CardNoList\":[{\n" + "      \"cardNo\":\"" + cardNo + "\"\n" + "    }]\n" + "  }\n" + "}";
            DigestResponseData checkCardExist = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/CardInfo/Search?format=json", checkCardReqBody, "application/json", "POST");

            if (checkCardExist.getContentType().startsWith("Request failed")) {
                logger.error("CardInfo Search Error: " + checkCardExist.getBody());
                return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + checkCardExist.getBody() + "\"}");
            }

            JSONObject checkCardResObj = new JSONObject(checkCardExist.getBody().toString());
            JSONObject jObjRes = checkCardResObj.getJSONObject("CardInfoSearch");
            if (jObjRes != null) {
                if (jObjRes.getString("responseStatusStrg").equals("OK")) {
                    logger.warn("Card info is already existed.");
                    return new JSONObject("{\"code\": \"warning\", \"msg\": \"Card info is already existed.\"}");
                }
            } else {
                logger.error(checkCardResObj.getString("statusString") + ": " + checkCardResObj.getString("subStatusCode"));
                return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkCardResObj.getString("statusString") + ": " + checkCardResObj.getString("subStatusCode") + "\"}");
            }

            String cardReqBody = "{\"CardInfo\":{\"employeeNo\":\"" + employeeNo + "\",\"cardNo\":\"" + cardNo + "\",\"cardType\":\"normalCard\"}}";
            DigestResponseData saveUserCardRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/CardInfo/SetUp?format=json", cardReqBody, "application/json", "PUT");
            if (saveUserCardRes.getContentType().startsWith("Request failed")) {
                logger.error("CardInfo CardInfoRecord Error: " + saveUserCardRes.getBody());
                return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + saveUserCardRes.getBody() + "\"}");
            }

            JSONObject saveUserCardResObj = new JSONObject(saveUserCardRes.getBody().toString());
            if (!saveUserCardResObj.getString("statusString").equals("OK")) {
                logger.error(saveUserCardResObj.getString("statusString") + ": " + saveUserCardResObj.getString("subStatusCode"));
                return new JSONObject("{\"code\": \"error\", \"msg\": \"" + saveUserCardResObj.getString("statusString") + ": " + saveUserCardResObj.getString("subStatusCode") + "\"}");
            }
        }

        logger.info("Sent employee data to FRT.");
        return new JSONObject("{\"code\": \"success\", \"msg\": \"\"}");
    }

    public static JSONObject sendEmpDataERP(Stage stage, Object data, String cardNo) {

        Integer personId;
        JSONObject empPhoto = null;
        Integer deviceId = deviceHolder.getDevice().getId();
        if (data instanceof JSONObject) {
            personId = ((JSONObject) data).getInt("empid");
            empPhoto = ((JSONObject) data).isNull("empphoto") ? null : ((JSONObject) data).getJSONObject("empphoto");
        } else {
            personId = empHolder.getEmployee().getEmpId();
        }
//        showLoading(stage, true);
        String checkEmpDicRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic?deviceid=" + deviceId + "&personid=" + personId, "GET", "application/json", "", true);
//        showLoading(stage, false);
        if (checkEmpDicRes.startsWith("Request failed")) {
            logger.error("Get device employees error: " + checkEmpDicRes);
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkEmpDicRes + "\"}");
        }
        JSONArray jsonArray = new JSONArray(checkEmpDicRes);
        if (jsonArray.length() > 0) {
            logger.warn("Employee is already existed.");
//            return new JSONObject("{\"code\": \"warning\", \"msg\": \"Employee is already existed.\"}");
        }

        String photoBase64 = "";
        if ((data instanceof String && !data.toString().isEmpty()) || data instanceof JSONObject) {
            if (data instanceof JSONObject) {
                photoBase64 = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/showdevempphoto", "POST", "application/json", "{\"photoid\": " + (empPhoto == null ? -1 : empPhoto.getInt("id")) + "}", true);
                if (photoBase64.equalsIgnoreCase("unknown")) {
                    logger.error("Not found image (" + (empPhoto == null ? -1 : empPhoto.getInt("id")) + ")");
//                    return new JSONObject("{\"code\": \"error\", \"msg\": \"Not found image\"}");
                }
            } else {
                photoBase64 = ImplFunctions.functions.convertImageToBase64(data + ".jpg");
            }
        }

//        showLoading(stage, true);
        String storeDevEmpReqBody = "{\"deviceid\": \"" + deviceId + "\", \"personid\": \"" + personId + "\", \"empid\": \"" + personId + "\", \"photo\": \"" + (photoBase64.equalsIgnoreCase("unknown") ? "" : photoBase64) + "\", \"cardno\": \"" + cardNo + "\"}";
        String storeDevEmpRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic", "POST", "application/json", storeDevEmpReqBody, true);
//        showLoading(stage, false);
        if (storeDevEmpRes.startsWith("Request failed")) {
            logger.error("When insert device employee data, occurred error: " + storeDevEmpRes);
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + storeDevEmpRes + "\"}");
        }

        logger.info("Sent employee data to ERP.");
        return new JSONObject("{\"code\": \"success\", \"msg\": \"\"}");
    }

    public static void sendUserData(Stage stage, String fileName, String cardNo, VBox root) {

        JSONObject frtSentStatus = sendEmpDataFaceRecogTerm(stage, fileName, cardNo);
        if (frtSentStatus.getString("code").startsWith("success")) {
            JSONObject erpSentStatus = sendEmpDataERP(stage, fileName, cardNo);
            if (erpSentStatus.getString("code").startsWith("success")) {
                ImplFunctions.functions.showAlert("Success", "", "Successfully added.", Alert.AlertType.INFORMATION);
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

        if (!fileName.isEmpty()) {
            try {
                Files.delete(Path.of(fileName + ".jpg"));
            } catch (IOException e) {
                logger.error(fileName + ".jpg file cannot delete.");
                e.printStackTrace();
            }
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
                    logger.info("Successfully delete face data from FRT.");
                } else {
                    response.put("info", "When delete face data from Face Recognition Terminal, occurred error.");
                    logger.error("When delete face data from Face Recognition Terminal, occurred error.");
                }
                return response;
            }
            response.put("valid", false);
            response.put("info", "When delete face data from Face Recognition Terminal, occurred error.");
            logger.error("When delete face data from Face Recognition Terminal, occurred error.");
            return response;
        } else {
            JSONObject empPhoto = jsonObject.isNull("empphoto") ? null : jsonObject.getJSONObject("empphoto");
            JSONObject frtSentStatus = sendEmpDataFaceRecogTerm(stage, jsonObject, empPhoto == null || empPhoto.isNull("cardno") ? "" : empPhoto.getString("cardno"));
            if (frtSentStatus.getString("code").startsWith("success")) {
                JSONObject erpSentStatus = sendEmpDataERP(stage, jsonObject, empPhoto == null || empPhoto.isNull("cardno") ? "" : empPhoto.getString("cardno"));
                if (erpSentStatus.getString("code").startsWith("success") || (erpSentStatus.getString("code").startsWith("error") && erpSentStatus.getString("msg").startsWith("Not found image")) || (frtSentStatus.getString("code").startsWith("warning") && frtSentStatus.getString("msg").endsWith("is already existed."))) {
                    response.put("valid", true);
                    response.put("info", "Success.");
                    logger.info("Successfully sent employee data to ERP.");
                } else {
                    response.put("valid", false);
                    response.put("info", erpSentStatus.getString("msg"));
                    logger.error(erpSentStatus.getString("msg"));
                }
                return response;
            } else if ((frtSentStatus.getString("code").startsWith("error") && frtSentStatus.getString("msg").startsWith("Not found image")) || (frtSentStatus.getString("code").startsWith("warning") && frtSentStatus.getString("msg").endsWith("is already existed."))) {
                response.put("valid", true);
                response.put("info", "Success.");
                logger.info("Successfully sent employee data to FRT.");
                return response;
            } else {
                response.put("valid", false);
                response.put("info", frtSentStatus.getString("msg"));
                logger.error(frtSentStatus.getString("msg"));
                return response;
            }
        }
    }

    public static boolean deleteEmpDataFaceRecogTerm(Stage stage, JSONObject jsonObject) {
        Integer employeeNo = jsonObject.getInt("personid");
        String delUserBody = "{\"UserInfoDelCond\":{\"EmployeeNoList\":[{\"employeeNo\":\"" + employeeNo + "\"}]}}";
        DigestResponseData deleteUserStatus = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/UserInfo/Delete?format=json", delUserBody, "application/json", "PUT");
        if (deleteUserStatus.getContentType().startsWith("Request failed")) {
            logger.error("UserInfo Delete Error: " + deleteUserStatus);
            return false;
        }
        return true;
    }

    public static boolean deleteEmpDataERP(Stage stage, JSONObject jsonObject) {
        Integer dicId = jsonObject.getInt("dicid");
        String delEmpDicBody = "{\"dicid\":" + dicId + "}";
        String delEmpDicRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/delempdic", "POST", "application/json", delEmpDicBody, true);
        if (delEmpDicRes.startsWith("Request failed")) {
            logger.error("Delete device employee data error: " + delEmpDicRes);
            return false;
        }
        return true;
    }

//    public static void showLoading(Stage stage, Boolean loading) {
//        getSpinningLoader(stage, loading);
//    }

    public static JSONArray getAcsEvents(String startDate, String endDate) {
        JSONArray sentArray = new JSONArray();

        int searchResultPosition = 0;
        String resultStatus = "MORE";
        while (!resultStatus.equals("OK")) {
            String requestBody = "{\"AcsEventCond\":{\"searchID\":\"1\",\"searchResultPosition\":" + searchResultPosition + ",\"maxResults\":1000,\"major\":5,\"minor\":75,\"startTime\":\"" + startDate + "\",\"endTime\":\"" + endDate + "\",\"thermometryUnit\":\"celcius\",\"currTemperature\":1}}";
//            showLoading(stage, true);
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json", requestBody, "application/json", "POST");
//            showLoading(stage, false);
            if (responseBody.getContentType().startsWith("Request failed")) {
                logger.error("AcsEvent error: " + requestBody);
                ImplFunctions.functions.showAlert("Error", "", "Request failed with status code: " + responseBody.getBody(), Alert.AlertType.ERROR);
                return null;
            }

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
                    searchResultPosition += acsEvent.getInt("numOfMatches");
                    resultStatus = acsEvent.getString("responseStatusStrg");
                    logger.info("AcsEvent response is successfully converted.");
                } else {
                    logger.warn("AcsEvent response hasn't attribute InfoList");
                }
            } catch (JSONException ex) {
                logger.error("When convert AcsEvent response to json, occurred error.");
                ex.printStackTrace();
            }
        }

        return sentArray;
    }

    @Override
    public void draw() {
        System.out.println("Drawing...");
    }
}