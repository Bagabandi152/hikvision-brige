package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.Device;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class DeviceHolder {

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
