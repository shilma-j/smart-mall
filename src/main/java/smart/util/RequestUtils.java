package smart.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * request utils tools
 */
public class RequestUtils {
    public static long getLong(HttpServletRequest request, String name) {
        return getLong(request, name, 0);
    }
    public static long getLong(HttpServletRequest request, String name, long defaultValue) {
        return Helper.longValue(request.getParameter(name), defaultValue);
    }
    public static String getStr(HttpServletRequest request, String name) {
        return getStr(request, name, null);
    }
    public static String getStr(HttpServletRequest request, String name, String defaultValue) {
        String str = request.getParameter(name);
        return str == null ? defaultValue : str;
    }
}
