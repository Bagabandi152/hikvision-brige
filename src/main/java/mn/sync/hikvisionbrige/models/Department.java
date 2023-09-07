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
 * @created 07/09/2023 - 9:10 AM
 * @purpose
 * @definition
 */
public class Department {
    String depName;
    String depNameEng;
    String depId;
    String parentId;
    String depFullName;

    private static final Logger logger = LogManager.getLogger(Department.class);

    public Department(String depName, String depNameEng, String depId, String parentId, String depFullName) {
        this.depName = depName;
        this.depNameEng = depNameEng;
        this.depId = depId;
        this.parentId = parentId;
        this.depFullName = depFullName;
    }

    public static ObservableList<Department> getDepList() {
        ObservableList<Department> list = FXCollections.observableArrayList();

        String response = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/getdeplist", "POST", "application/json", "{}", true);
        if (response.startsWith("Request failed")) {
            logger.error("Get departments from ERP: " + response);
            ImplFunctions.functions.showAlert("Error", "", response, Alert.AlertType.ERROR);
            return list;
        }

        JSONArray jsonArray;
        try {
            if (response == null) {
                logger.error("When fetch department list from server, occurred error.");
                ImplFunctions.functions.showAlert("Error", "", "When fetch department list from server, occurred error.", Alert.AlertType.ERROR);
                return list;
            }
            jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    list.add(new Department(jsonObject.getString("depname"), jsonObject.getString("depengname"), jsonObject.getString("depid"), jsonObject.getString("parentid"), jsonObject.getString("fulldepname")));
                }
            }
            list.sort(Comparator.comparing(Department::getDepFullName));
        } catch (JSONException e) {
            e.printStackTrace();
            logger.error("Response cannot convert to json array.");
        }

        return list;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public String getDepNameEng() {
        return depNameEng;
    }

    public void setDepNameEng(String depNameEng) {
        this.depNameEng = depNameEng;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDepFullName() {
        return depFullName;
    }

    public void setDepFullName(String depFullName) {
        this.depFullName = depFullName;
    }
}
