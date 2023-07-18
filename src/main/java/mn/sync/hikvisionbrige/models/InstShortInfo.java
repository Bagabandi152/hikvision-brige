package mn.sync.hikvisionbrige.models;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:08 PM
 * @purpose
 * @definition
 */
public class InstShortInfo {
    Integer instId;
    String regNo;
    String instShortName;
    String instShortNameEng;
    String instName;
    String instEngName;

    public InstShortInfo(Integer instId, String regNo, String instShortName, String instShortNameEng, String instName, String instEngName) {
        this.instId = instId;
        this.regNo = regNo;
        this.instShortName = instShortName;
        this.instShortNameEng = instShortNameEng;
        this.instName = instName;
        this.instEngName = instEngName;
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
     * @return regNo
     */
    public String getRegNo() {
        return this.regNo;
    }

    /**
     * set field
     *
     * @param regNo
     */
    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    /**
     * get field
     *
     * @return instShortName
     */
    public String getInstShortName() {
        return this.instShortName;
    }

    /**
     * set field
     *
     * @param instShortName
     */
    public void setInstShortName(String instShortName) {
        this.instShortName = instShortName;
    }

    /**
     * get field
     *
     * @return instShortNameEng
     */
    public String getInstShortNameEng() {
        return this.instShortNameEng;
    }

    /**
     * set field
     *
     * @param instShortNameEng
     */
    public void setInstShortNameEng(String instShortNameEng) {
        this.instShortNameEng = instShortNameEng;
    }

    /**
     * get field
     *
     * @return instName
     */
    public String getInstName() {
        return this.instName;
    }

    /**
     * set field
     *
     * @param instName
     */
    public void setInstName(String instName) {
        this.instName = instName;
    }

    /**
     * get field
     *
     * @return instEngName
     */
    public String getInstEngName() {
        return this.instEngName;
    }

    /**
     * set field
     *
     * @param instEngName
     */
    public void setInstEngName(String instEngName) {
        this.instEngName = instEngName;
    }

    @Override
    public String toString() {
        return instName;
    }
}
