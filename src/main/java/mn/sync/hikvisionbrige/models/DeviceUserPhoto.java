package mn.sync.hikvisionbrige.models;

import javafx.collections.ObservableList;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 01/09/2023 - 12:01 PM
 * @purpose
 * @definition
 */
public class DeviceUserPhoto {
    String FPID;
    String faceURL;
    String modelData;

    public DeviceUserPhoto(String FPID, String faceURL, String modelData) {
        this.FPID = FPID;
        this.faceURL = faceURL;
        this.modelData = modelData;
    }

    /**
     * get field
     *
     * @return FPID
     */
    public String getFPID() {
        return this.FPID;
    }

    /**
     * set field
     *
     * @param FPID
     */
    public void setFPID(String FPID) {
        this.FPID = FPID;
    }

    /**
     * get field
     *
     * @return faceURL
     */
    public String getFaceURL() {
        return this.faceURL;
    }

    /**
     * set field
     *
     * @param faceURL
     */
    public void setFaceURL(String faceURL) {
        this.faceURL = faceURL;
    }

    /**
     * get field
     *
     * @return modelData
     */
    public String getModelData() {
        return this.modelData;
    }

    /**
     * set field
     *
     * @param modelData
     */
    public void setModelData(String modelData) {
        this.modelData = modelData;
    }
}
