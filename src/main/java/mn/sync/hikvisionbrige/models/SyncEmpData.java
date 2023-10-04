package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 07/09/2023 - 9:59 AM
 * @purpose
 * @definition
 */
public class SyncEmpData {
    Integer deviceId;
    String deviceName;
    Integer dicId;
    Integer empId;
    String empName;
    String empEngName;
    String identity;

    private static final Logger logger = LogManager.getLogger(SyncEmpData.class);

    public SyncEmpData(Integer deviceId, String deviceName, Integer dicId, Integer empId, String empName, String empEngName, String identity) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.dicId = dicId;
        this.empId = empId;
        this.empName = empName;
        this.empEngName = empEngName;
        this.identity = identity;
    }

    public static ObservableList<SyncEmpData> getEmpDataList() {
        ObservableList<SyncEmpData> list = FXCollections.observableArrayList();

        String response = ImplFunctions.functions.ErpApiService("/timerpt/deviceempdic/getotherdevemps", "POST", "application/json", "{\"listType\": \"all\"}", true);
        if (response.startsWith("Request failed")) {
            logger.error("Get sync employees data error: " + response);
            return list;
        }

        JSONArray jsonArray;
        try {
            if (response == null) {
                logger.error("When fetch sync employees data list from server, occurred error.");
                ImplFunctions.functions.showAlert("Error", "", "When fetch sync employees data list from server, occurred error.", Alert.AlertType.ERROR);
                return list;
            }
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    JSONObject emp = jsonObject.isNull("employee") ? null : jsonObject.getJSONObject("employee");
                    list.add(new SyncEmpData(jsonObject.getInt("deviceid"), jsonObject.getString("devicename"), jsonObject.getInt("dicid"), jsonObject.getInt("empid"), emp == null ? "" : emp.getString("empfname"), emp == null ? "" : emp.getString("empfnameeng"), jsonObject.getString("identname")));
                }
            }
            list.sort(Comparator.comparing(SyncEmpData::getIdentity));
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("Response cannot convert to json array.");
        }

        return list;
    }

    public Integer getDicId() {
        return dicId;
    }

    public void setDicId(Integer dicId) {
        this.dicId = dicId;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Integer getEmpId() {
        return empId;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpEngName() {
        return empEngName;
    }

    public void setEmpEngName(String empEngName) {
        this.empEngName = empEngName;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
