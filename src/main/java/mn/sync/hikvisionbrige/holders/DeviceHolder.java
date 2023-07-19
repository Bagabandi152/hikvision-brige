package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.Device;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 19/07/2023 - 11:18 AM
 * @purpose
 * @definition
 */
public class DeviceHolder {
    private Device device;
    private final static DeviceHolder INSTANCE = new DeviceHolder();

    private DeviceHolder() {}

    public static DeviceHolder getInstance() {
        return INSTANCE;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return this.device;
    }
}
