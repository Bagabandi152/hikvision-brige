package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import org.apache.logging.log4j.Logger;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 01/09/2023 - 11:55 AM
 * @purpose
 * @definition
 */
public class DeviceUser {
    String employeeNo;
    String name;
    String userType;
    String gender;
    Integer numOfCard;
    Integer numOfFace;

    public DeviceUser(String employeeNo, String name, String userType, String gender, Integer numOfCard, Integer numOfFace) {
        this.employeeNo = employeeNo;
        this.name = name;
        this.userType = userType;
        this.gender = gender;
        this.numOfCard = numOfCard;
        this.numOfFace = numOfFace;
    }

    public static ObservableList<DeviceUser> getDeviceUserList(String BASE_URL, Logger logger) {
        ObservableList<DeviceUser> list = FXCollections.observableArrayList();

        int searchResultPosition = 0;
        String resultStatus = "MORE";
        while (!resultStatus.equals("OK")) {
            String requestBody = "{\n" + "    \"UserInfoSearchCond\": {\n" + "        \"searchID\": \"1\",\n" + "        \"searchResultPosition\": " + searchResultPosition + ",\n" + "        \"maxResults\": 1000\n" + "    }\n" + "}";
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/UserInfo/Search?format=json", requestBody, "application/json", "POST");
            if (responseBody.getContentType().startsWith("Request failed")) {
                logger.error("when get device user list, occurred error: " + requestBody);
                ImplFunctions.functions.showAlert("Error", "", "Request failed with status code: " + responseBody.getBody(), Alert.AlertType.ERROR);
                return null;
            }

            try {
                JSONObject userInfoSearch = new JSONObject(responseBody.getBody().toString()).getJSONObject("UserInfoSearch");
                if (userInfoSearch.has("UserInfo")) {
                    JSONArray responseJson = userInfoSearch.getJSONArray("UserInfo");
                    for (int i = 0; i < responseJson.length(); i++) {
                        JSONObject jo = responseJson.getJSONObject(i);
                        DeviceUser newDeviceUser = new DeviceUser(jo.getString("employeeNo"), jo.getString("name"), jo.getString("userType"), jo.getString("gender"), jo.getInt("numOfCard"), jo.getInt("numOfFace"));
                        list.add(newDeviceUser);
                    }
                    searchResultPosition += userInfoSearch.getInt("numOfMatches");
                    resultStatus = userInfoSearch.getString("responseStatusStrg");
                    logger.info("Device user list is successfully converted.");
                } else {
                    logger.warn("UserInfo response hasn't attribute InfoList");
                }
            } catch (JSONException ex) {
                logger.error("When convert UserInfo response to json, occurred error.");
                ex.printStackTrace();
            }
        }
        list.sort(Comparator.comparing(DeviceUser::getName));
        return list;
    }


    /**
     * get field
     *
     * @return employeeNo
     */
    public String getEmployeeNo() {
        return this.employeeNo;
    }

    /**
     * set field
     *
     * @param employeeNo
     */
    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    /**
     * get field
     *
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * set field
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get field
     *
     * @return userType
     */
    public String getUserType() {
        return this.userType;
    }

    /**
     * set field
     *
     * @param userType
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * get field
     *
     * @return gender
     */
    public String getGender() {
        return this.gender;
    }

    /**
     * set field
     *
     * @param gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * get field
     *
     * @return numOfCard
     */
    public Integer getNumOfCard() {
        return this.numOfCard;
    }

    /**
     * set field
     *
     * @param numOfCard
     */
    public void setNumOfCard(Integer numOfCard) {
        this.numOfCard = numOfCard;
    }

    /**
     * get field
     *
     * @return numOfFace
     */
    public Integer getNumOfFace() {
        return this.numOfFace;
    }

    /**
     * set field
     *
     * @param numOfFace
     */
    public void setNumOfFace(Integer numOfFace) {
        this.numOfFace = numOfFace;
    }
}
