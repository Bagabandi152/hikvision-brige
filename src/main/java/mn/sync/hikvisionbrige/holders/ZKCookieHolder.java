package mn.sync.hikvisionbrige.holders;

import java.net.HttpCookie;

public final class ZKCookieHolder {

    private HttpCookie cookie;
    private final static ZKCookieHolder INSTANCE = new ZKCookieHolder();

    public static ZKCookieHolder getInstance() {
        return INSTANCE;
    }

    public ZKCookieHolder() {
        cookie = new HttpCookie("cookieName", "cookieValue");
    }

    public String getCookie(String key) {
        if (cookie.getName().equals(key)) {
            return cookie.getValue();
        }
        return null;
    }

    public void setCookie(String key, String value) {
        cookie = new HttpCookie(key, value);
        cookie.setPath("/");
    }

    public void removeCookie(String key) {
        if (cookie.getName().equals(key)) {
            cookie.setMaxAge(0);
        }
    }
}
