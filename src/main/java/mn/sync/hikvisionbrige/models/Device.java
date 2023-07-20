package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public Device(){
        this(-1,"","","");
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
     *
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
     *
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
     *
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
        String response = ImplFunctions.functions.ErpApiService("/timerpt/device","GET","application/json","",true);

        // Print the response
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(response.toString());
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if( !jsonObject.isNull("ipaddress") && !jsonObject.getString("ipaddress").isBlank() && !jsonObject.getString("ipaddress").isEmpty()){
                    list.add(new Device(jsonObject.getInt("deviceid"), jsonObject.getString("ipaddress"), jsonObject.getString("devicename"), jsonObject.getString("deviceserial")));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
}
