package mn.sync.hikvisionbrige.holders;

import java.net.HttpCookie;

public final class CookieHolder {

    private HttpCookie cookie;
    private final static CookieHolder INSTANCE = new CookieHolder();

    public static CookieHolder getInstance() {
        return INSTANCE;
    }

    public CookieHolder() {
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
