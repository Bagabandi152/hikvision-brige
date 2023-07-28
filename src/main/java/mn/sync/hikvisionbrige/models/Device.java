package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.MainApp;
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
 * @created 17/07/2023 - 3:23 PM
 * @purpose
 * @definition
 */
public class Device {
    Integer id;
    String ipAddress;
    String name;
    String serial;

    public Device(Integer id, String ipAddress, String name, String serial) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.name = name;
        this.serial = serial;
    }

    public Device() {
        this(-1, "", "", "");
    }

    private static Logger logger = LogManager.getLogger(Device.class);

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
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * get field
     *
     * @return ipAddress
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * set field
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
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
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get field
     *
     * @return serial
     */
    public String getSerial() {
        return this.serial;
    }

    /**
     * set field
     */
    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ObservableList<Device> getDeviceList() {
        ObservableList<Device> list = FXCollections.observableArrayList();
        String response = ImplFunctions.functions.ErpApiService("/timerpt/device", "GET", "application/json", "", true);
        if (response.startsWith("Request failed")) {
            logger.error("Get devices from ERP: " + response);
            ImplFunctions.functions.showAlert("Error", "", response, Alert.AlertType.ERROR);
            return list;
        }

        JSONArray jsonArray;
        try {
            if (response == null) {
                logger.error("When fetch device list from server, occurred error.");
                ImplFunctions.functions.showAlert("Error", "", "When fetch device list from server, occurred error.", Alert.AlertType.ERROR);
                return list;
            }
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (!jsonObject.isNull("ipaddress") && !jsonObject.getString("ipaddress").isBlank() && !jsonObject.getString("ipaddress").isEmpty()) {
                    list.add(new Device(jsonObject.getInt("deviceid"), jsonObject.getString("ipaddress"), jsonObject.getString("devicename"), jsonObject.getString("deviceserial")));
                }
            }
            list.sort(Comparator.comparing(Device::getName));
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("Response cannot convert to json array.");
        }

        return list;
    }
}
