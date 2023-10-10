package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.MainApp;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import org.apache.logging.log4j.LogManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 01/09/2023 - 11:55 AM
 * @purpose
 * @definition
 */
public class DeviceUser {
    String employeeNo;
    String name;
    String userType;
    String gender;
    Integer numOfCard;
    Integer numOfFace;
    Integer UID;

    private static final Logger logger = LogManager.getLogger(MainApp.class);


    public DeviceUser(String employeeNo, String name, String userType, String gender, Integer numOfCard, Integer numOfFace) {
        this.employeeNo = employeeNo;
        this.name = name;
        this.userType = userType;
        this.gender = gender;
        this.numOfCard = numOfCard;
        this.numOfFace = numOfFace;
    }

    public static ObservableList<DeviceUser> getDeviceUserList(String BASE_URL) {
        ObservableList<DeviceUser> list = FXCollections.observableArrayList();

        int searchResultPosition = 0;
        String resultStatus = "MORE";
        while (!(resultStatus.equals("OK") || resultStatus.equals("NO MATCH"))) {
            String requestBody = "{\n" + "    \"UserInfoSearchCond\": {\n" + "        \"searchID\": \"1\",\n" + "        \"searchResultPosition\": " + searchResultPosition + ",\n" + "        \"maxResults\": 1000\n" + "    }\n" + "}";
            DigestResponseData responseBody = ImplFunctions.functions.DigestApiService(BASE_URL + "/ISAPI/AccessControl/UserInfo/Search?format=json", requestBody, "application/json", "POST");
            if (responseBody.getContentType().startsWith("Request failed")) {
                logger.error("when get device user list, occurred error: " + requestBody);
                ImplFunctions.functions.showAlert("Error", "", "Request failed with status code: " + responseBody.getBody(), Alert.AlertType.ERROR);
                return null;
            }

            try {
                JSONObject userInfoSearch = new JSONObject(responseBody.getBody().toString()).getJSONObject("UserInfoSearch");
                if (userInfoSearch.has("UserInfo")) {
                    JSONArray responseJson = userInfoSearch.getJSONArray("UserInfo");
                    for (int i = 0; i < responseJson.length(); i++) {
                        JSONObject jo = responseJson.getJSONObject(i);
                        DeviceUser newDeviceUser = new DeviceUser(jo.getString("employeeNo"), jo.getString("name"), jo.getString("userType"), jo.getString("gender"), jo.getInt("numOfCard"), jo.getInt("numOfFace"));
                        list.add(newDeviceUser);
                    }
                    searchResultPosition += userInfoSearch.getInt("numOfMatches");
                    logger.info("Device user list is successfully converted.");
                } else {
                    logger.warn("UserInfo response hasn't attribute InfoList");
                }
                resultStatus = userInfoSearch.getString("responseStatusStrg");
            } catch (JSONException ex) {
                logger.error("When convert UserInfo response to json, occurred error.");
                ex.printStackTrace();
            }
        }
        list.sort(Comparator.comparing(DeviceUser::getName));
        return list;
    }

    public static ArrayList getUsersFromHtml(String html) {
        Document document = Jsoup.parse(html);
        ArrayList<Element> tables = document.select("table");

        Element tHead = null;
        Element tBody = null;
        if (tables.size() > 5) {
            tHead = tables.get(4);
            tBody = tables.get(5);
        }

        ArrayList<String> headers = new ArrayList<>();
        if (tHead != null) {
            Element tableHeader = tHead.getElementsByClass("table_header").get(0);
            if (tableHeader != null) {
                ArrayList<Element> headCols = tableHeader.getElementsByTag("td");
                for (int i = 0; i < headCols.size(); i++) {
                    if (i > 0) {
                        Element elmCol = headCols.get(i);
                        headers.add(elmCol.text());
                    }
                }
            }
        }

        ArrayList<JSONObject> deviceUsers = new ArrayList<>();
        if (tBody != null) {
            ArrayList<Element> users = tBody.getElementsByTag("tr");
            for (int i = 0; i < users.size(); i++) {
                Element userElm = users.get(i);
                if (userElm != null) {
                    JSONObject userData = new JSONObject();
                    ArrayList<Element> bodyCols = userElm.getElementsByTag("td");
                    for (int j = 0; j < bodyCols.size(); j++) {
                        Element elm = bodyCols.get(j);
                        if (j == 0) {
                            userData.put(elm.getElementsByTag("input").first().attr("name"), elm.getElementsByTag("input").first().attr("value"));
                        } else if (j > 0 && j < bodyCols.size() - 1) {
                            userData.put(headers.get(j - 1), elm.text());
                        } else if (j == bodyCols.size() - 1) {
                            userData.put(headers.get(j - 1), elm.getElementsByTag("a").first().attr("href"));
                        }
                    }
                    deviceUsers.add(userData);
                }
            }
        }

        return deviceUsers;
    }

    public static ObservableList<DeviceUser> getZKTecoUserList(String BASE_URL) {
        ObservableList<DeviceUser> list = FXCollections.observableArrayList();

        ArrayList<JSONObject> allUsers = new ArrayList<>();
        int searchResultPosition = 0;
        String resultStatus = "MORE";
        while (!resultStatus.equals("OK")) {
            String url = BASE_URL + "/csl/user?first= " + searchResultPosition + "&last= " + (searchResultPosition + 20);
            String responseBody = ImplFunctions.functions.ZKTecoApiService(url, "GET", new HashMap<>());
            ArrayList<JSONObject> pageUsers = getUsersFromHtml(responseBody);
            allUsers.addAll(pageUsers);
            if (pageUsers.size() < 20) {
                resultStatus = "OK";
            }
            searchResultPosition += 20;
        }

        for (int i = 0; i < allUsers.size(); i++) {
            JSONObject jo = allUsers.get(i);
            DeviceUser newDeviceUser = new DeviceUser(jo.getString("ID Number"), jo.getString("Name").equals("") ? jo.getString("ID Number") : jo.getString("Name"), jo.getString("Privilege"), "", jo.getString("Card").equals("0") ? 0 : 1, 0);
            newDeviceUser.setUID(jo.getInt("uid"));
            list.add(newDeviceUser);
        }

        list.sort(Comparator.comparing(DeviceUser::getName));
        return list;
    }

    public Integer getUID() {
        return UID;
    }

    public void setUID(Integer UID) {
        this.UID = UID;
    }

    /**
     * get field
     *
     * @return employeeNo
     */
    public String getEmployeeNo() {
        return this.employeeNo;
    }

    /**
     * set field
     *
     * @param employeeNo
     */
    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
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
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * get field
     *
     * @return userType
     */
    public String getUserType() {
        return this.userType;
    }

    /**
     * set field
     *
     * @param userType
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * get field
     *
     * @return gender
     */
    public String getGender() {
        return this.gender;
    }

    /**
     * set field
     *
     * @param gender
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * get field
     *
     * @return numOfCard
     */
    public Integer getNumOfCard() {
        return this.numOfCard;
    }

    /**
     * set field
     *
     * @param numOfCard
     */
    public void setNumOfCard(Integer numOfCard) {
        this.numOfCard = numOfCard;
    }

    /**
     * get field
     *
     * @return numOfFace
     */
    public Integer getNumOfFace() {
        return this.numOfFace;
    }

    /**
     * set field
     *
     * @param numOfFace
     */
    public void setNumOfFace(Integer numOfFace) {
        this.numOfFace = numOfFace;
    }
}
