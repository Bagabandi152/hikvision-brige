package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 24/07/2023 - 5:36 PM
 * @purpose
 * @definition
 */
public class TimeDataLog {
    Integer id;
    String empFirstName;
    String empFirstNameEng;
    String empLastName;
    String empLastNameEng;
    String registerDate;
    String uploadDate;
    String deviceName;
    String instName;
    String instNameEng;
    String endUserName;
    String endUserNameEng;

    public TimeDataLog(Integer id, String empFirstName, String empFirstNameEng, String empLastName, String empLastNameEng, String registerDate, String uploadDate, String deviceName, String instName, String instNameEng) {
        this.id = id;
        this.empFirstName = empFirstName;
        this.empFirstNameEng = empFirstNameEng;
        this.empLastName = empLastName;
        this.empLastNameEng = empLastNameEng;
        this.registerDate = registerDate;
        this.uploadDate = uploadDate;
        this.deviceName = deviceName;
        this.instName = instName;
        this.instNameEng = instNameEng;
        this.endUserName = empLastName + " " + empFirstName.toUpperCase();
        this.endUserNameEng = empFirstNameEng.toUpperCase() + " " + empLastNameEng;
    }

    public static ObservableList<TimeDataLog> getTimeDataLog() {
        ObservableList<TimeDataLog> list = FXCollections.observableArrayList();

        String response = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/gettimedata", "POST", "application/json", "{}", true);
        if (response.startsWith("Request failed")) {
            ImplFunctions.functions.showAlert("Error", "", response, Alert.AlertType.ERROR);
            return list;
        }

        JSONArray jsonArray;
        try {
            if (response == null) {
                ImplFunctions.functions.showAlert("Error", "", "When fetch employee list from server, occurred error.", Alert.AlertType.ERROR);
                return list;
            }
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                TimeDataLog timeDataLog = new TimeDataLog(jsonObject.getInt("id"), jsonObject.getString("empfname"), jsonObject.getString("empfnameeng"), jsonObject.getString("emplname"), jsonObject.getString("emplnameeng"), jsonObject.getString("registerdate"), jsonObject.getString("uploaddate"), jsonObject.getString("devicename"), jsonObject.getString("instname"), jsonObject.getString("instnameeng"));
                list.add(timeDataLog);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * get field
     *
     * @return id
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * set field
     *
     * @param id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * get field
     *
     * @return empFirstName
     */
    public String getEmpFirstName() {
        return this.empFirstName;
    }

    /**
     * set field
     *
     * @param empFirstName
     */
    public void setEmpFirstName(String empFirstName) {
        this.empFirstName = empFirstName;
    }

    /**
     * get field
     *
     * @return empFirstNameEng
     */
    public String getEmpFirstNameEng() {
        return this.empFirstNameEng;
    }

    /**
     * set field
     *
     * @param empFirstNameEng
     */
    public void setEmpFirstNameEng(String empFirstNameEng) {
        this.empFirstNameEng = empFirstNameEng;
    }

    /**
     * get field
     *
     * @return empLastName
     */
    public String getEmpLastName() {
        return this.empLastName;
    }

    /**
     * set field
     *
     * @param empLastName
     */
    public void setEmpLastName(String empLastName) {
        this.empLastName = empLastName;
    }

    /**
     * get field
     *
     * @return empLastNameEng
     */
    public String getEmpLastNameEng() {
        return this.empLastNameEng;
    }

    /**
     * set field
     *
     * @param empLastNameEng
     */
    public void setEmpLastNameEng(String empLastNameEng) {
        this.empLastNameEng = empLastNameEng;
    }

    /**
     * get field
     *
     * @return registerDate
     */
    public String getRegisterDate() {
        return this.registerDate;
    }

    /**
     * set field
     *
     * @param registerDate
     */
    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    /**
     * get field
     *
     * @return uploadDate
     */
    public String getUploadDate() {
        return this.uploadDate;
    }

    /**
     * set field
     *
     * @param uploadDate
     */
    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * get field
     *
     * @return deviceName
     */
    public String getDeviceName() {
        return this.deviceName;
    }

    /**
     * set field
     *
     * @param deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * get field
     *
     * @return instName
     */
    public String getInstName() {
        return this.instName;
    }

    /**
     * set field
     *
     * @param instName
     */
    public void setInstName(String instName) {
        this.instName = instName;
    }

    /**
     * get field
     *
     * @return instNameEng
     */
    public String getInstNameEng() {
        return this.instNameEng;
    }

    /**
     * set field
     *
     * @param instNameEng
     */
    public void setInstNameEng(String instNameEng) {
        this.instNameEng = instNameEng;
    }

    /**
     * get field
     *
     * @return endUserName
     */
    public String getEndUserName() {
        return this.endUserName;
    }

    /**
     * set field
     *
     * @param endUserName
     */
    public void setEndUserName(String endUserName) {
        this.endUserName = endUserName;
    }

    /**
     * get field
     *
     * @return endUserNameEng
     */
    public String getEndUserNameEng() {
        return this.endUserNameEng;
    }

    /**
     * set field
     *
     * @param endUserNameEng
     */
    public void setEndUserNameEng(String endUserNameEng) {
        this.endUserNameEng = endUserNameEng;
    }
}
