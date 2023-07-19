package mn.sync.hikvisionbrige.constants;

import org.json.JSONObject;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 5:25 PM
 * @purpose
 * @definition
 */
public interface Functions {
    public default String objectToJson(Object object) {
        JSONObject jsonObject = new JSONObject();
        return jsonObject.toString();
    }
    public String DigestApiService(String API, String requestBody, String type);
    public String ErpApiService(String API, String method, String type, String requestBody, Boolean auth);
}
