package mn.sync.hikvisionbrige.models;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 20/07/2023 - 10:22 AM
 * @purpose
 * @definition
 */
public class DigestResponseData {
    private String contentType;
    private Object body;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
