package mn.sync.hikvisionbrige.models;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 26/07/2023 - 9:43 AM
 * @purpose
 * @definition
 */
public class LogData {
    private String timestamp;
    private String level;
    private String logger;
    private String message;

    public LogData(String timestamp, String level, String logger, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.logger = logger;
        this.message = message;
    }


    /**
     * get field
     *
     * @return timestamp
     */
    public String getTimestamp() {
        return this.timestamp;
    }

    /**
     * set field
     *
     * @param timestamp
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * get field
     *
     * @return level
     */
    public String getLevel() {
        return this.level;
    }

    /**
     * set field
     *
     * @param level
     */
    public void setLevel(String level) {
        this.level = level;
    }

    /**
     * get field
     *
     * @return logger
     */
    public String getLogger() {
        return this.logger;
    }

    /**
     * set field
     *
     * @param logger
     */
    public void setLogger(String logger) {
        this.logger = logger;
    }

    /**
     * get field
     *
     * @return message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * set field
     *
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
