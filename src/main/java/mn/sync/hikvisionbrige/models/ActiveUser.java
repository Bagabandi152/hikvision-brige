package mn.sync.hikvisionbrige.models;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public class ActiveUser {
    Integer userId;
    Integer empId;
    String name;
    String email;
    String accessToken;

    public ActiveUser(Integer userId, Integer empId, String name, String email, String accessToken) {
        this.userId = userId;
        this.empId = empId;
        this.name = name;
        this.email = email;
        this.accessToken = accessToken;
    }


    /**
     * get field
     *
     * @return userId
     */
    public Integer getUserId() {
        return this.userId;
    }


    /**
     * set field
     *
     * @param userId
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
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
    public Integer setEmpId(Integer empId) {
        return this.empId = empId;
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
     * @return email
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * set field
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * get field
     *
     * @return accessToken
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * set field
     *
     * @param accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
