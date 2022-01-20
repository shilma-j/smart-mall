package smart.lib;

import smart.lib.session.Session;

import java.util.regex.Pattern;

public class Validate {

    public static String address(String data, String alias) {
        if (data == null || data.trim().length() == 0) {
            return null;
        }
        data = data.trim();
        if (data.length() < 6) {
            return String.format("%s最少需要6个字", alias);
        }
        if (data.length() > 50) {
            return String.format("%s最多不得超过50个字", alias);
        }
        return null;
    }

    public static String captcha(String captcha, Session session, String alias) {
        if (captcha != null && session != null) {
            String val = (String) session.get(Helper.camelCase(Captcha.class));
            if (captcha.equals(val)) {
                return null;
            }
        }
        return String.format("%s不正确", alias);
    }

    /**
     * 字母或数字
     *
     * @param data
     * @param alias
     * @return null 成功， or 错误信息
     */
    public static String letterOrNumber(String data, String alias) {
        if (Pattern.matches("^[0-9a-zA-Z]*$", data)) {
            return null;
        }
        return String.format("%s只能由字母或数字组成", alias);
    }

    /**
     * validate mobile phone
     *
     * @param data
     * @param alias
     * @return null 成功， or 错误信息
     */
    public static String mobile(String data, String alias) {
        if (data == null || data.length() == 0) {
            return null;
        }
        if (!Pattern.matches("^1\\d{10}$", data)) {
            return String.format("%s不正确", alias);
        }
        return null;
    }

    /**
     * 用户名判断
     *
     * @param data  要判断的用户名
     * @param alias 用户名别称
     * @return null 成功， or 错误信息
     */
    public static String name(String data, String alias) {
        String msg = notEmpty(data, alias);
        if (msg != null) {
            return msg;
        }
        if (data.length() < 4) {
            return String.format("%s长度过短", alias);
        }
        if (data.length() > 12) {
            return String.format("%s长度过长", alias);
        }
        if (Pattern.matches("^\\d+$", data)) {
            return String.format("%s不能是存数字", alias);
        }
        if (!Pattern.matches("^[a-z\\d]+$", data)) {
            return String.format("%s只能由字母或数字组成", alias);
        }
        return null;
    }

    /**
     * 不为空判断
     *
     * @param data  要判断的内容
     * @param alias 别名
     * @return null 成功， or 错误信息
     */
    public static String notEmpty(String data, String alias) {
        if (data == null || data.trim().length() == 0) {
            return String.format("%s不的为空", alias);
        }
        return null;
    }

    public static String password(String data, String alias) {
        String msg = notEmpty(data, alias);
        if (msg != null) {
            return msg;
        }
        if (data.length() < 6) {
            return alias + "过于简单";
        }
        return null;
    }

    public static String price(String data, String alias) {
        String msg = notEmpty(data, alias);
        if (msg != null) {
            return msg;
        }
        data = data.trim();
        if (Pattern.matches("^\\d+$", data)) {
            return null;
        }
        if (Pattern.matches("^\\d+\\.\\d{0,2}$", data)) {
            return null;
        }
        return String.format("%s格式不正确", alias);
    }
}
