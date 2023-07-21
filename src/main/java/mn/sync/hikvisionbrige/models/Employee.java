package mn.sync.hikvisionbrige.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import mn.sync.hikvisionbrige.constants.ImplFunctions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 20/07/2023 - 4:34 PM
 * @purpose
 * @definition
 */
public class Employee {
    Integer empId;
    Integer instId;
    String empFirstName;
    String empLastNameAbr;
    String empLastName;
    String empFirstNameEng;
    String empLastNameEng;
    String empLastNameAbrEng;
    String endUserName;
    String endUserNameEng;
    String prflNo;
    String gender;

    public Employee(Integer empId, Integer instId, String empFirstName, String empLastNameAbr, String empLastName, String empFirstNameEng, String empLastNameEng, String empLastNameAbrEng, String endUserName, String endUserNameEng, String prflNo, String gender) {
        this.empId = empId;
        this.instId = instId;
        this.empFirstName = empFirstName;
        this.empLastNameAbr = empLastNameAbr;
        this.empLastName = empLastName;
        this.empFirstNameEng = empFirstNameEng;
        this.empLastNameEng = empLastNameEng;
        this.empLastNameAbrEng = empLastNameAbrEng;
        this.endUserName = endUserName;
        this.endUserNameEng = endUserNameEng;
        this.prflNo = prflNo;
        this.gender = gender;
    }

    @Override
    public String toString() {
        return  endUserNameEng;
    }

    public static ObservableList<Employee> getEmpList() {
        ObservableList<Employee> list = FXCollections.observableArrayList();
        String response = ImplFunctions.functions.ErpApiService("/timerpt/deviceupload/getactiveemps","POST","application/json","{}",true);
        if(response.startsWith("Request failed")){
            ImplFunctions.functions.showAlert("Error", "",response, Alert.AlertType.ERROR);
            return list;
        }
        // Print the response
        JSONArray jsonArray = null;
        try {
            if(response == null){
                ImplFunctions.functions.showAlert("Error", "", "When fetch employee list from server, occurred error.", Alert.AlertType.ERROR);
                return list;
            }
            jsonArray = new JSONArray(response);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Employee newEmployee = new Employee(jsonObject.getInt("empid"), jsonObject.getInt("instid"), jsonObject.getString("empfname"), jsonObject.getString("emplname"), jsonObject.getString("emplnameabr"), jsonObject.getString("empfnameeng"), jsonObject.getString("emplnameeng"), jsonObject.getString("emplnameabreng"), jsonObject.getString("endusername"), jsonObject.getString("enduserengname"), !jsonObject.isNull("prflno") ? jsonObject.getString("prflno") : "", jsonObject.getInt("gender") == 1 ? "male": jsonObject.getInt("gender") == 2 ? "female" : "unknown");
                list.add(newEmployee);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * get field
     *
     * @return empId
     */
    public Integer getEmpId() {
        return this.empId;
    }

    /**
     * set field
     *
     * @param empId
     */
    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    /**
     * get field
     *
     * @return instId
     */
    public Integer getInstId() {
        return this.instId;
    }

    /**
     * set field
     *
     * @param instId
     */
    public void setInstId(Integer instId) {
        this.instId = instId;
    }

    /**
     * get field
     *
     * @return empFirstName
     */
    public String getEmpFirstName() {
        return this.empFirstName;
    }

    /**
     * set field
     *
     * @param empFirstName
     */
    public void setEmpFirstName(String empFirstName) {
        this.empFirstName = empFirstName;
    }

    /**
     * get field
     *
     * @return empLastNameAbr
     */
    public String getEmpLastNameAbr() {
        return this.empLastNameAbr;
    }

    /**
     * set field
     *
     * @param empLastNameAbr
     */
    public void setEmpLastNameAbr(String empLastNameAbr) {
        this.empLastNameAbr = empLastNameAbr;
    }

    /**
     * get field
     *
     * @return empLastName
     */
    public String getEmpLastName() {
        return this.empLastName;
    }

    /**
     * set field
     *
     * @param empLastName
     */
    public void setEmpLastName(String empLastName) {
        this.empLastName = empLastName;
    }

    /**
     * get field
     *
     * @return empFirstNameEng
     */
    public String getEmpFirstNameEng() {
        return this.empFirstNameEng;
    }

    /**
     * set field
     *
     * @param empFirstNameEng
     */
    public void setEmpFirstNameEng(String empFirstNameEng) {
        this.empFirstNameEng = empFirstNameEng;
    }

    /**
     * get field
     *
     * @return empLastNameEng
     */
    public String getEmpLastNameEng() {
        return this.empLastNameEng;
    }

    /**
     * set field
     *
     * @param empLastNameEng
     */
    public void setEmpLastNameEng(String empLastNameEng) {
        this.empLastNameEng = empLastNameEng;
    }

    /**
     * get field
     *
     * @return empLastNameAbrEng
     */
    public String getEmpLastNameAbrEng() {
        return this.empLastNameAbrEng;
    }

    /**
     * set field
     *
     * @param empLastNameAbrEng
     */
    public void setEmpLastNameAbrEng(String empLastNameAbrEng) {
        this.empLastNameAbrEng = empLastNameAbrEng;
    }

    /**
     * get field
     *
     * @return endUserName
     */
    public String getEndUserName() {
        return this.endUserName;
    }

    /**
     * set field
     *
     * @param endUserName
     */
    public void setEndUserName(String endUserName) {
        this.endUserName = endUserName;
    }

    /**
     * get field
     *
     * @return endUserNameEng
     */
    public String getEndUserNameEng() {
        return this.endUserNameEng;
    }

    /**
     * set field
     *
     * @param endUserNameEng
     */
    public void setEndUserNameEng(String endUserNameEng) {
        this.endUserNameEng = endUserNameEng;
    }

    /**
     * get field
     *
     * @return prflNo
     */
    public String getPrflNo() {
        return this.prflNo;
    }

    /**
     * set field
     *
     * @param prflNo
     */
    public void setPrflNo(String prflNo) {
        this.prflNo = prflNo;
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
}
