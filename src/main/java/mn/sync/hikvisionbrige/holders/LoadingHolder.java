package mn.sync.hikvisionbrige.holders;

import mn.sync.hikvisionbrige.models.Loader;

/**
 * @author Bagaa
 * @project hikvision-brige
 * @created 18/07/2023 - 3:28 PM
 * @purpose
 * @definition
 */
public final class LoadingHolder {

    private Loader loader;
    private final static LoadingHolder INSTANCE = new LoadingHolder();

    private LoadingHolder() {
    }

    public static LoadingHolder getInstance() {
        return INSTANCE;
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    public Loader getLoader() {
        return this.loader;
    }

    public void clear() {
        loader = null;
    }
}
