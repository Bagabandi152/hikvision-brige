package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.DeviceUser;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class DeviceUserHolder {

    private DeviceUser deviceUser;
    private final static DeviceUserHolder INSTANCE = new DeviceUserHolder();

    private DeviceUserHolder() {
    }

    public static DeviceUserHolder getInstance() {
        return INSTANCE;
    }

    public void setDeviceUser(DeviceUser deviceUser) {
        this.deviceUser = deviceUser;
    }

    public DeviceUser getDeviceUser() {
        return this.deviceUser;
    }
}
