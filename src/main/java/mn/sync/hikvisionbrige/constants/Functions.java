package mn.sync.hikvisionbrige.constants;

import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.models.DigestResponseData;
import org.json.JSONObject;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 5:25 PM
 * @purpose
 * @definition
 */
public interface Functions {
    default String objectToJson(Object object) {
        JSONObject jsonObject = new JSONObject();
        return jsonObject.toString();
    }

    DigestResponseData DigestApiService(String API, Object requestBody, String type, String requestMethod);

    String ErpApiService(String API, String method, String type, String requestBody, Boolean auth);

    void showAlert(String title, String headerText, String msg, Alert.AlertType alertType);

    String convertImageToBase64(String imagePath);
}
