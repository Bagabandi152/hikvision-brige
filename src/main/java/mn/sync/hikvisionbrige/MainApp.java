package mn.sync.hikvisionbrige;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import mn.sync.hikvisionbrige.appender.LogTableViewAppender;
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
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
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
    private static TableView<LogData> logTableView;
    private static Logger logger = LogManager.getLogger(MainApp.class);

    public static void start(Stage stage) {

        final Permission permission = checkPermission("TRDeviceEmpDic");

        //Create root
        VBox root = new VBox();
        root.setSpacing(10);
        root.setPadding(new Insets(15, 25, 10, 25));

        //Create tableView columns data
        JSONArray tabCols = new JSONArray();
        String[][] tabColsData = {{"Timestamp", "Level", "Logger", "Message"}, {"timestamp", "level", "logger", "message"}};
        for (int i = 0; i < 4; i++) {
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
            if (lastUploadRes.isEmpty() || lastUploadRes.equals("null") || lastUploadRes == null) {
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

            String requestBody = "{\"AcsEventCond\":{\"searchID\":\"1\",\"searchResultPosition\":0,\"maxResults\":1000,\"major\":5,\"minor\":75,\"startTime\":\"" + startDate + "\",\"endTime\":\"" + endDate + "\",\"thermometryUnit\":\"celcius\",\"currTemperature\":1}}";
//            showLoading(stage, true);
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/AcsEvent?format=json", requestBody, "application/json", "POST");
//            showLoading(stage, false);
            if (responseBody.getContentType().startsWith("Request failed")) {
                logger.error("AcsEvent error: " + requestBody);
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
                    logger.info("AcsEvent response is successfully converted.");
                } else {
                    logger.warn("AcsEvent response hasn't attribute InfoList");
                }
            } catch (JSONException ex) {
                logger.error("When convert AcsEvent response to json, occurred error.");
                ex.printStackTrace();
            }

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
            if (BASE_URL.isEmpty()) {
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
        Button newBtn = new Button("Add new employee");
        EventHandler<ActionEvent> addEmpEvent = e -> {
            if (BASE_URL.isEmpty()) {
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

                sendUserData(stage, fileName, root);
            }
        };
        newBtn.setOnAction(addEmpEvent);
        hBoxBtnDown.getChildren().add(newBtn);

        logTableView = new TableView<>();

        TableColumn<LogData, String> timestampColumn = new TableColumn<>("Time");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampColumn.setPrefWidth(115);

        TableColumn<LogData, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        levelColumn.setPrefWidth(45);

        TableColumn<LogData, String> loggerColumn = new TableColumn<>("Logger");
        loggerColumn.setCellValueFactory(new PropertyValueFactory<>("logger"));
        loggerColumn.prefWidthProperty().bind(logTableView.widthProperty().subtract(165).divide(2));

        TableColumn<LogData, String> messageColumn = new TableColumn<>("Message");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageColumn.prefWidthProperty().bind(logTableView.widthProperty().subtract(165).divide(2));

        logTableView.getColumns().addAll(timestampColumn, levelColumn, messageColumn, loggerColumn);

        //Add children to root
        if (permission.getCreate()) {
            root.getChildren().add(getSeparatorWithLabel("Device section"));
            root.getChildren().add(flowPane);
            root.getChildren().add(sepTop);
            root.getChildren().add(hBoxBtnTop);
            root.getChildren().add(getSeparatorWithLabel("Employee section"));
            root.getChildren().add(empFlowPane);
            root.getChildren().add(sepDown);
            root.getChildren().add(hBoxBtnDown);
        } else if (permission.getRead()) {
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
        root.getChildren().add(logTableView);

        // Initialize Log4j2 and set up a custom appender to add log messages to the TableView
        LogTableViewAppender appender = new LogTableViewAppender(logTableView);
        appender.start();

        // Add the appender to Log4j2's root logger
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        config.getRootLogger().addAppender(appender, null, null);

        // Update the TableView whenever new log messages are received
        appender.setOnLogUpdate(logTableView::refresh);

        //Set config in stage
        stage.setResizable(true);
        stage.setMinWidth(360);
        stage.setTitle(instHolder.getInst().getInstShortNameEng() + " - Face Recog Terminal");
        Scene scene = new Scene(root, 345, permission.getCreate() ? 480 : permission.getRead() ? 350 : 260);
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
            logger.error("UserInfo SetUp Error: " + setUserInfoRes.getBody());
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + setUserInfoRes.getBody() + "\"}");
        }

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
        byte[] photoBytes;
        if (data instanceof JSONObject) {
            fileName = "tmp/CaptureFaceData_" + employeeNo + "_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 99999) + 10000);
            String empPhotoBase64 = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/showdevempphoto", "POST", "application/json", "{\"photoid\": " + empPhoto.getInt("id") + "}", true);
            if (empPhotoBase64.equalsIgnoreCase("unknown")) {
                logger.warn(empName + "'s image not found!");
                return new JSONObject("{\"code\": \"error\", \"msg\": \"Not found image\"}");
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
        formDataBuilder.addFormDataPart("asd", fileName, RequestBody.create(photoBytes, MediaType.parse("image/jpeg")));

//        showLoading(stage, true);
        DigestResponseData saveUserFaceRes = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json", formDataBuilder, "application/json", "POST");
//        showLoading(stage, false);
        if (saveUserFaceRes.getContentType().startsWith("Request failed")) {
            logger.error("FDLib FaceDataRecord Error: " + saveUserFaceRes.getBody());
            return new JSONObject("{\"code\": \"error\", \"msg\": \"Request failed with status code: " + saveUserFaceRes.getBody() + "\"}");
        }

        JSONObject saveUserFaceResObj = new JSONObject(saveUserFaceRes.getBody().toString());
        if (!saveUserFaceResObj.getString("statusString").equals("OK")) {
            logger.error(saveUserFaceResObj.getString("statusString") + ": " + saveUserFaceResObj.getString("subStatusCode"));
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + saveUserFaceResObj.getString("statusString") + ": " + saveUserFaceResObj.getString("subStatusCode") + "\"}");
        }

        logger.info("Sent employee data to FRT.");
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
            logger.error("Get device employees error: " + checkEmpDicRes);
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + checkEmpDicRes + "\"}");
        }
        JSONArray jsonArray = new JSONArray(checkEmpDicRes);
        if (jsonArray.length() > 0) {
            logger.warn("Employee is already existed.");
            return new JSONObject("{\"code\": \"warning\", \"msg\": \"Employee is already existed.\"}");
        }

        String photoBase64;
        if (data instanceof JSONObject) {
            photoBase64 = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/showdevempphoto", "POST", "application/json", "{\"photoid\": " + empPhoto.getInt("id") + "}", true);
            if (photoBase64.equalsIgnoreCase("unknown")) {
                logger.error("Not found image (" + empPhoto.getString("id") + ")");
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
            logger.error("When insert device employee data, occurred error: " + storeDevEmpRes);
            return new JSONObject("{\"code\": \"error\", \"msg\": \"" + storeDevEmpRes + "\"}");
        }

        logger.info("Sent employee data to ERP.");
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
            logger.error(fileName + ".jpg file cannot delete.");
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
            JSONObject frtSentStatus = sendEmpDataFaceRecogTerm(stage, jsonObject);
            if (frtSentStatus.getString("code").startsWith("success")) {
                JSONObject erpSentStatus = sendEmpDataERP(stage, jsonObject);
                if (erpSentStatus.getString("code").startsWith("success") || (erpSentStatus.getString("code").startsWith("error") && erpSentStatus.getString("msg").startsWith("Not found image"))) {
                    response.put("valid", true);
                    response.put("info", "Success.");
                    logger.info("Successfully sent employee data to ERP.");
                    return response;
                } else {
                    response.put("valid", false);
                    response.put("info", erpSentStatus.getString("msg"));
                    logger.error(erpSentStatus.getString("msg"));
                    return response;
                }
            } else if (frtSentStatus.getString("code").startsWith("error") && frtSentStatus.getString("msg").startsWith("Not found image")) {
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

    @Override
    public void draw() {
        System.out.println("Drawing...");
    }
}