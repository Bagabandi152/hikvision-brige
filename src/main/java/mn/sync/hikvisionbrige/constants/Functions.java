package mn.sync.hikvisionbrige.constants;

import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.models.DigestResponseData;
import org.json.JSONObject;

import java.util.Map;

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

    DigestResponseData DigestApiService(String API, Object requestBody, String type, String requestMethod, String userName, String userPwd);

    String ErpApiService(String API, String method, String type, String requestBody, Boolean auth);

    void showAlert(String title, String headerText, String msg, Alert.AlertType alertType);

    String convertImageToBase64(String imagePath);

    String convertImageUrlToBase64(String url);

    String ZKTecoApiService(String API, String method, Map<String, String> requestBody);
}
