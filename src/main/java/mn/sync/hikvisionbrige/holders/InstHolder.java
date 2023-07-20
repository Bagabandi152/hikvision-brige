package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.InstShortInfo;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class InstHolder {

    private InstShortInfo instShortInfo;
    private final static InstHolder INSTANCE = new InstHolder();

    private InstHolder() {}

    public static InstHolder getInstance() {
        return INSTANCE;
    }

    public void setInst(InstShortInfo instShortInfo) {
        this.instShortInfo = instShortInfo;
    }

    public InstShortInfo getInst() {
        return this.instShortInfo;
    }
}
