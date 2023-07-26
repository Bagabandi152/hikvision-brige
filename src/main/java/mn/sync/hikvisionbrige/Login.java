package mn.sync.hikvisionbrige;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

import mn.sync.hikvisionbrige.constants.ImplFunctions;
import mn.sync.hikvisionbrige.holders.CookieHolder;
import mn.sync.hikvisionbrige.holders.InstHolder;
import mn.sync.hikvisionbrige.holders.UserHolder;
import mn.sync.hikvisionbrige.models.ActiveUser;
import mn.sync.hikvisionbrige.models.InstShortInfo;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 12:24 PM
 * @purpose
 * @definition
 */

public class Login extends Application {
    private TextField usernameTextField;
    private PasswordField passwordField;

    public static void main(String[] args) {
        launch(args);
    }

    private static Logger logger = LogManager.getLogger(Login.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Sync ERP Login is started.");

        // Create the GridPane layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Add Username label and TextField
        Label usernameLabel = new Label("Email:");
        usernameTextField = new TextField();
        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                usernameTextField.setStyle("-fx-border-color: none;");
            }
        });
        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameTextField, 1, 0);

        // Add Password label and PasswordField
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                passwordField.setStyle("-fx-border-color: none;");
            }
        });
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);

        //Login form set default values
        usernameTextField.setText("Bagabandi@sync.mn");
        passwordField.setText("Bagaa010520");

        // Add Login button
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String userName = usernameTextField.getText();
            String password = passwordField.getText();

            if (userName.isEmpty()) {
                logger.warn("Username is required!");
                usernameTextField.setStyle("-fx-border-color: #f00; -fx-border-radius: 3px;");
                return;
            }

            if (password.isEmpty()) {
                logger.warn("Password is required!");
                passwordField.setStyle("-fx-border-color: #f00; -fx-border-radius: 3px;");
                return;
            }

            // Perform login validation here
//            MainApp.showLoading(primaryStage, true);
            JSONObject step1Object = validateLogin(userName, password, null, 1, null, "");
//            MainApp.showLoading(primaryStage, false);
            try {
                assert step1Object != null;
                if ("enterPassword".equals(step1Object.getString("responsecode"))) {
//                    MainApp.showLoading(primaryStage, true);
                    JSONObject step2Object = validateLogin(userName, password, null, 2, step1Object.getString("steptoken"), "");
//                    MainApp.showLoading(primaryStage, false);
                    assert step2Object != null;
                    if (step2Object.getString("responsecode").equals("chooseInst")) {
                        logger.warn("Choose institution.");
                        chooseInst(primaryStage, gridPane, step2Object, userName, password);
                    } else if (step2Object.getString("responsecode").equals("requiredMFA")) {
                        logger.warn("Required FMA.");
                        gridPane.getChildren().clear();
                        gridPane.setPadding(new Insets(30));
                        Label comboLabel = new Label("Enter google authentication code:");
                        comboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));

                        TextField textField = new TextField();
                        textField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue.length() == 6) {
                                JSONObject step4Object;
                                try {
//                                    MainApp.showLoading(primaryStage, true);
                                    step4Object = validateLogin(userName, password, null, 4, step2Object.getString("steptoken"), newValue);
//                                    MainApp.showLoading(primaryStage, false);
                                    assert step4Object != null;
                                    if (step4Object.getString("responsecode").equals("chooseInst")) {
                                        logger.warn("Choose institution.");
                                        chooseInst(primaryStage, gridPane, step4Object, userName, password);
                                    } else {
                                        logger.error("Choose institution error!");
                                    }
                                } catch (JSONException ex) {
                                    ex.printStackTrace();
                                    logger.error("Required FMA error!");
                                }
                            }
                        });
                        gridPane.add(comboLabel, 0, 0);
                        gridPane.add(textField, 0, 1);
                    } else {
                        loginError(step2Object.getString("responsecode"));
                        logger.error("Choose institution or required FMA error!");
                    }
                } else {
                    loginError(step1Object.getString("responsecode"));
                    logger.error("Enter password error!");
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                loginError("notFound");
                logger.error("User not found!");
            }
        });
        gridPane.add(loginButton, 1, 2);

        // Create the Scene and set it on the Stage
        Scene scene = new Scene(gridPane, 300, 150);
        primaryStage.setTitle("ERP-Sync Systems Login");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void chooseInst(Stage stage, GridPane gridPane, JSONObject stepObject, String userName, String password) {
        JSONArray instList;
        try {
            JSONObject jsonObject = stepObject.getJSONObject("data");
            instList = jsonObject.getJSONArray("insts");

            gridPane.getChildren().clear();
            gridPane.setPadding(new Insets(30));
            Label comboLabel = new Label("Select a institution:");
            comboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));

            ComboBox<InstShortInfo> comboBox = new ComboBox<>();
            comboBox.setPrefWidth(280);
            ObservableList<InstShortInfo> observableList = FXCollections.observableArrayList();
            for (int i = 0; i < instList.length(); i++) {
                JSONObject inst = instList.getJSONObject(i);
                observableList.add(new InstShortInfo(inst.getInt("instid"), inst.getString("regno"), inst.getString("instnameshort"), inst.getString("instnameshorteng"), inst.getString("instname"), inst.getString("instnameeng")));
            }
            comboBox.setItems(observableList);
            comboBox.setPromptText("Select . . .");
            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                InstHolder.getInstance().setInst(newValue);
                try {
//                    MainApp.showLoading(stage, true);
                    JSONObject step3Object = validateLogin(userName, password, newValue.getInstId(), 3, stepObject.getString("steptoken"), "");
//                    MainApp.showLoading(stage, false);
                    assert step3Object != null;
                    if (step3Object.getString("responsecode").equals("success")) {
                        logger.info("Login success.");
                        JSONObject successData = step3Object.getJSONObject("data");
                        UserHolder.getInstance().setActiveUser(new ActiveUser(successData.getInt("id"), successData.getInt("empid"), successData.getString("name"), successData.getString("email")));
                        CookieHolder.getInstance().setCookie("login", successData.getString("access_token"));
                        loginSuccess(userName, stage);
                    } else {
                        loginError(step3Object.getString("responsecode"));
                        logger.error("Choose institution error: " + step3Object.getString("responsecode"));
                    }
                } catch (JSONException ex) {
                    loginError("InstNotFound");
                    logger.error("Institution not found!");
                    ex.printStackTrace();
                }
            });
            gridPane.add(comboLabel, 0, 0);
            gridPane.add(comboBox, 0, 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loginSuccess(String username, Stage stage) {
        // Show a success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText("Hello, " + username + ".\nWelcome to face recognition terminal.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty()) {
            System.out.println("Alert is exited, no button has been pressed.\n");
        } else {
            MainApp.start(stage);
        }
    }

    private void loginError(String responseCode) {
        // Show an error message
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("Invalid username or password. Please try again. (" + responseCode + ")");
        alert.showAndWait();
    }

    private JSONObject validateLogin(String email, String password, Integer instId, Integer step, String stepToken, String authCode) {
        String requestBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"instid\":" + instId + ", \"step\":" + step + ",\"steptoken\":\"" + stepToken + "\"}";
        ;
        if (!authCode.isEmpty()) {
            requestBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"instid\":" + instId + ", \"step\":" + step + ",\"steptoken\":\"" + stepToken + "\", \"authcode\":\"" + authCode + "\"}";
        }
        String response = ImplFunctions.functions.ErpApiService("/auth/login", "POST", "application/json", requestBody, false);
        if (response.startsWith("Request failed")) {
            ImplFunctions.functions.showAlert("Error", "", response, Alert.AlertType.ERROR);
            logger.error(response);
            return null;
        }
        try {
            if (response.isEmpty()) {
                logger.warn("Validate login response is null.");
                return null;
            }
            return new JSONObject(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("Validate login error.");
            return null;
        }
    }
}

