package mn.sync.hikvisionbrige.models;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 25/07/2023 - 7:19 PM
 * @purpose
 * @definition
 */
public class Permission {
    String permCode;
    String permName;
    Boolean read;
    Boolean update;
    Boolean change;
    Boolean create;
    Boolean delete;

    public Permission(String permCode, String permName, Boolean read, Boolean update, Boolean change, Boolean create, Boolean delete) {
        this.permCode = permCode;
        this.permName = permName;
        this.read = read;
        this.update = update;
        this.change = change;
        this.create = create;
        this.delete = delete;
    }

    /**
     * get field
     *
     * @return permCode
     */
    public String getPermCode() {
        return this.permCode;
    }

    /**
     * set field
     *
     * @param permCode
     */
    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    /**
     * get field
     *
     * @return permName
     */
    public String getPermName() {
        return this.permName;
    }

    /**
     * set field
     *
     * @param permName
     */
    public void setPermName(String permName) {
        this.permName = permName;
    }

    /**
     * get field
     *
     * @return read
     */
    public Boolean getRead() {
        return this.read;
    }

    /**
     * set field
     *
     * @param read
     */
    public void setRead(Boolean read) {
        this.read = read;
    }

    /**
     * get field
     *
     * @return update
     */
    public Boolean getUpdate() {
        return this.update;
    }

    /**
     * set field
     *
     * @param update
     */
    public void setUpdate(Boolean update) {
        this.update = update;
    }

    /**
     * get field
     *
     * @return change
     */
    public Boolean getChange() {
        return this.change;
    }

    /**
     * set field
     *
     * @param change
     */
    public void setChange(Boolean change) {
        this.change = change;
    }

    /**
     * get field
     *
     * @return create
     */
    public Boolean getCreate() {
        return this.create;
    }

    /**
     * set field
     *
     * @param create
     */
    public void setCreate(Boolean create) {
        this.create = create;
    }

    /**
     * get field
     *
     * @return delete
     */
    public Boolean getDelete() {
        return this.delete;
    }

    /**
     * set field
     *
     * @param delete
     */
    public void setDelete(Boolean delete) {
        this.delete = delete;
    }
}
