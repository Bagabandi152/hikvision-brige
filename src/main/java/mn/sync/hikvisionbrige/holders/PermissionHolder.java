package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.Permission;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class PermissionHolder {

    private Permission permission;
    private final static PermissionHolder INSTANCE = new PermissionHolder();

    private PermissionHolder() {
    }

    public static PermissionHolder getInstance() {
        return INSTANCE;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public Permission getPermission() {
        return this.permission;
    }
}
