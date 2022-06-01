package au.org.ala.auth;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationCookieUtils {

    private final static Logger logger = LoggerFactory.getLogger(AuthenticationCookieUtils.class);

    public static final String ALA_AUTH_COOKIE = "ALA-Auth";

    public static boolean isUserLoggedIn(HttpServletRequest request) {
        return cookieExists(request, ALA_AUTH_COOKIE);
    }

    public static String getUserName(HttpServletRequest request) {
        return getCookieValue(request, ALA_AUTH_COOKIE);
    }

    public static boolean cookieExists(HttpServletRequest request, String name) {
        return getCookieValue(request, name) != null;
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        String value = null;
        Cookie cookie = getCookie(request, name);
        if (cookie != null) {
            value = cookie.getValue();
        }
        return value;
    }

    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie cookie = null;
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (c.getName().equals(name)) {
                    cookie = c;
                    break;
                }
            }
        }

        if (cookie == null) {
            logger.debug("Cookie " + name + " not found");
        } else {
            logger.debug("Cookie " + name + " found");
        }

        return cookie;
    }
}