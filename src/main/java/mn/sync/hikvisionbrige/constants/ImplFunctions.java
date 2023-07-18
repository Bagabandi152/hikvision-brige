package mn.sync.hikvisionbrige.constants;

import org.json.JSONObject;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 5:37 PM
 * @purpose
 * @definition
 */
public class ImplFunctions {
    public static Functions functions = new Functions() {
        @Override
        public String objectToJson(Object object) {
            JSONObject jsonObject = new JSONObject();
            return jsonObject.toString();
        }
    };
}
