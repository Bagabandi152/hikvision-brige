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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

import mn.sync.hikvisionbrige.constants.FinalVariables;
import mn.sync.hikvisionbrige.holders.CookieHolder;
import mn.sync.hikvisionbrige.holders.UserHolder;
import mn.sync.hikvisionbrige.models.ActiveUser;
import mn.sync.hikvisionbrige.models.InstShortInfo;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

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

    @Override
    public void start(Stage primaryStage) {

        // Create the GridPane layout
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        // Add Username label and TextField
        Label usernameLabel = new Label("Email:");
        usernameTextField = new TextField();
        usernameTextField.textProperty().addListener( (observable, oldValue, newValue) -> {
            if(!newValue.isEmpty()){
                usernameTextField.setStyle("-fx-border-color: none;");
            }
        });
        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameTextField, 1, 0);

        // Add Password label and PasswordField
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.textProperty().addListener( (observable, oldValue, newValue) -> {
            if(!newValue.isEmpty()){
                passwordField.setStyle("-fx-border-color: none;");
            }
        });
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);

        // Add Login button
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String userName = usernameTextField.getText();
            String password = passwordField.getText();

            if(userName.isEmpty()){
                usernameTextField.setStyle("-fx-border-color: #f00; -fx-border-radius: 3px;");
                return;
            }

            if(password.isEmpty()){
                passwordField.setStyle("-fx-border-color: #f00; -fx-border-radius: 3px;");
                return;
            }

            // Perform login validation here
            JSONObject step1Object = validateLogin(userName, password, null, 1, null);
            try {
                assert step1Object != null;
                if ("enterPassword".equals(step1Object.getString("responsecode"))) {
                    JSONObject step2Object = validateLogin(userName, password, null, 2, step1Object.getString("steptoken"));
                    assert step2Object != null;
                    if (step2Object.getString("responsecode").equals("chooseInst")){
                        JSONArray instList = new JSONArray(step2Object.getJSONObject("data").getString("insts"));
                        System.out.println("instList" + instList);

                        gridPane.getChildren().clear();
                        gridPane.setPadding(new Insets(30));
                        Label comboLabel = new Label("Select a institution:");
                        comboLabel.setFont(Font.font("", FontWeight.BOLD, FontPosture.REGULAR, 13));

                        ComboBox<InstShortInfo> comboBox = new ComboBox<>();
                        ObservableList<InstShortInfo> observableList = FXCollections.observableArrayList();
                        for(int i = 0; i < instList.length(); i++){
                            JSONObject inst = instList.getJSONObject(i);
                            observableList.add(new InstShortInfo(inst.getInt("instid"), inst.getString("regno"), inst.getString("instnameshort"), inst.getString("instnameshorteng"), inst.getString("instname"), inst.getString("instnameeng")));
                        }
                        comboBox.setItems(observableList);
                        comboBox.setPromptText("Select . . .");
                        comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                            JSONObject step3Object;
                            try {
                                step3Object = validateLogin(userName, password, newValue.getInstId(), 3, step2Object.getString("steptoken"));
                                assert step3Object != null;
                                if(step3Object.getString("responsecode").equals("success")){
                                    JSONObject successData = step3Object.getJSONObject("data");
                                    UserHolder.getInstance().setActiveUser(new ActiveUser(successData.getInt("id"), successData.getInt("empid"), successData.getString("name"),successData.getString("email")));
                                    CookieHolder.getInstance().setCookie("login", successData.getString("access_token"));
                                    loginSuccess(userName, primaryStage);
                                }else{
                                    loginError(step3Object.getString("responsecode"));
                                }
                            } catch (JSONException ex) {
                                loginError("InstNotFound");
                                ex.printStackTrace();
                            }
                        });
                        gridPane.add(comboLabel, 0,0);
                        gridPane.add(comboBox, 0,1);
                    }else{
                        loginError(step2Object.getString("responsecode"));
                    }
                }else{
                    loginError(step1Object.getString("responsecode"));
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
                loginError("notFound");
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

    private void loginSuccess(String username, Stage stage){
        // Show a success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText("Hello, " + username + ".\nWelcome to face recognition terminal.");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.isEmpty()){
            System.out.println("Alert is exited, no button has been pressed.\n");
        }else{
            stage.close();
            MainApp.start(stage);
        }
    }

    private void loginError(String responseCode){
        // Show an error message
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("Invalid username or password. Please try again. (" + responseCode + ")");
        alert.showAndWait();
    }

    private JSONObject validateLogin(String email, String password, Integer instId, Integer step, String stepToken) {
        try {
            // Create the URL object
            URL url = new URL(FinalVariables.ERP_URL + "auth/login");

            // Create the HttpURLConnection object
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("POST");

            // Set request headers
            connection.setRequestProperty("Content-Type", "application/json");

            // Create the request body
            String requestBody = "{\"email\": \"" + email + "\", \"password\": \"" + password + "\", \"instid\":" + instId + ", \"step\":" + step + ",\"steptoken\":\"" + stepToken + "\"}";

            System.out.println("requestBody: " + requestBody);
            // Enable output and send the request body
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(requestBody.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            if(responseCode == 200){
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Print the response
                System.out.println("Response: " + response);

                // Close the connection
                connection.disconnect();
                return new JSONObject(response.toString());
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

