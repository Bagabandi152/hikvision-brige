package mn.sync.hikvisionbrige.commands;

import mn.sync.hikvisionbrige.constants.ImplFunctions;
import mn.sync.hikvisionbrige.holders.CookieHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static mn.sync.hikvisionbrige.MainApp.getAcsEvents;
import static mn.sync.hikvisionbrige.MainApp.getZkTimeLogs;
import static mn.sync.hikvisionbrige.MainApp.setZKTecoCookie;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 10/10/2023 - 11:25 AM
 * @purpose
 * @definition
 */
public class SyncTimeData {

    private static Logger logger = LogManager.getLogger(SyncTimeData.class);

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Usage: java SyncTimeData <instId> <deviceId>");
            System.exit(1);
        }

        Integer instId = Integer.parseInt(args[0]);
        Integer deviceId = Integer.parseInt(args[1]);

//        Integer instId = 51;
//        Integer deviceId = 583; //448; //583;
        logger.info("instID: " + instId + ", deviceID: " + deviceId);

        LocalDateTime now = LocalDateTime.now();
        ZoneOffset zoneOffset = ZoneOffset.ofHours(8);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmm");
        Integer hr = Integer.parseInt(now.atOffset(zoneOffset).format(formatter));
        Integer code = (8888 - hr) * (8888 - hr);

        String requestBody = "{\"instid\": " + instId + ", \"code\": " + code + "}";
        logger.info("Request code: " + code);
        String response = ImplFunctions.functions.ErpApiService("/auth/devicelogin", "POST", "application/json", requestBody, false);
        if (response.startsWith("Request failed")) {
            System.err.println("Error: " + response);
            logger.error("Error: " + response);
            return;
        }
        try {
            if (response.isEmpty()) {
                System.out.println("Validate login response is null.");
                logger.error("Validate login response is null.");
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            System.err.println("Validate login error.");
            logger.error("Validate login response is null.");
            return;
        }
        JSONObject successData = new JSONObject(response).getJSONObject("data");
        System.out.println("response: " + successData);
        CookieHolder.getInstance().setCookie("login", successData.getString("access_token"));

        syncTimeData(deviceId);
    }

    public static void syncTimeData(Integer deviceId) {
        String startDate = "";
        String endDate;

        LocalDateTime now = LocalDateTime.now();
        ZoneOffset zoneOffset = ZoneOffset.ofHours(8);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        endDate = now.atOffset(zoneOffset).format(formatter);

        String lastUploadRes = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/getlastupload", "POST", "application/json", "{\"deviceid\":" + deviceId + "}", true);
        if (lastUploadRes.startsWith("Request failed")) {
            System.err.println("Error: " + lastUploadRes);
            logger.error("Error: " + lastUploadRes);
            return;
        }
        if (lastUploadRes.isEmpty() || lastUploadRes.equals("null")) {
            LocalDateTime oneMonthAgo = now.minusMonths(1);
            startDate = oneMonthAgo.atOffset(zoneOffset).format(formatter);
        } else {
            try {
                JSONObject lastUpload = new JSONObject(lastUploadRes);
                String lastUploadDate = lastUpload.getString("uploaddate");
                DateTimeFormatter simpleFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(lastUploadDate, simpleFormatter);
                startDate = dateTime.atOffset(zoneOffset).format(formatter);
                System.out.println("Last upload date is successfully formatted.");
                logger.info("Last upload date is successfully formatted.");
            } catch (JSONException ex) {
                ex.printStackTrace();
                System.err.println("When format last upload date, occurred error.");
                logger.info("When format last upload date, occurred error.");
            }
        }
        System.out.println("startDate: " + startDate);
        System.out.println("endDate: " + endDate);
        logger.info("Start date: " + startDate + ", End date: " + endDate);

        String deviceRes = ImplFunctions.functions.ErpApiService("/timerpt/device/get", "POST", "application/json", "{\"deviceid\":" + deviceId + "}", true);
        if (deviceRes.startsWith("Request failed")) {
            System.err.println("Error: " + deviceRes);
            logger.error("Error: " + deviceRes);
            return;
        }

        JSONObject device;
        if (deviceRes.isEmpty() || deviceRes.equals("null")) {
            device = null;
        } else {
            device = new JSONObject(deviceRes);
        }

        JSONArray sentArray = new JSONArray();
        if (device != null && device.getString("deviceserial").toLowerCase().startsWith("zkteco")) {
            logger.info("Device serial: " + device.getString("deviceserial") + ", IP address: " + device.getString("ipaddress"));
            if (setZKTecoCookie(device.getString("username"), device.getString("userpwd"), "http://" + device.getString("ipaddress"))) {
                sentArray = getZkTimeLogs(startDate, endDate, "http://" + device.getString("ipaddress"));
            } else {
                logger.error("Cannot connect to device! Check IP address.");
            }
        } else if (device != null) {
            logger.info("Device serial: " + device.getString("deviceserial") + ", IP address: " + device.getString("ipaddress"));
            JSONArray faceEventsArray = getAcsEvents(startDate, endDate, 75, "http://" + device.getString("ipaddress"), device.getString("username"), device.getString("userpwd")); // 75 -> Face events
            JSONArray cardEventsArray = getAcsEvents(startDate, endDate, 1, "http://" + device.getString("ipaddress"), device.getString("username"), device.getString("userpwd")); // 1 -> Card events
            for (int i = 0; i < Objects.requireNonNull(cardEventsArray).length(); i++) {
                assert faceEventsArray != null;
                faceEventsArray.put(cardEventsArray.getJSONObject(i));
            }
            sentArray = faceEventsArray;
        } else {
            logger.error("Device is null.");
        }

        String uploadResponse = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/inserttimedata", "POST", "application/json", "{\"deviceid\":" + deviceId + ", \"timedata\":" + sentArray + "}", true);
        if (uploadResponse.startsWith("Request failed")) {
            System.err.println("Insert time data error: " + uploadResponse);
            logger.error("Insert time data error: " + uploadResponse);
            return;
        }
        if (uploadResponse.isEmpty() || uploadResponse.isBlank()) {
            System.err.println("When insert time data to ERP, occurred error.");
            logger.error("When insert time data to ERP, occurred error.");
            return;
        }
        System.out.println("Successfully inserted time data to ERP.");
        logger.info("Successfully inserted time data to ERP.");
        System.out.println(sentArray.length() + " rows time data inserted.");
        logger.info(sentArray.length() + " rows time data inserted.");
    }
}
