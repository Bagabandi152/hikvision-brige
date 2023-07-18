package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.ActiveUser;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class UserHolder {

    private ActiveUser activeUser;
    private final static UserHolder INSTANCE = new UserHolder();

    private UserHolder() {}

    public static UserHolder getInstance() {
        return INSTANCE;
    }

    public void setActiveUser(ActiveUser u) {
        this.activeUser = u;
    }

    public ActiveUser getActiveUser() {
        return this.activeUser;
    }
}
