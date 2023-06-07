package github.cloudtour.utils;

/**
 * 字符串工具类
 * @author cloudtour
 * @version 1.0
 */
public class StringUtil {
    /**
     * 判空-对于null或长度为0或全为空格的字符串判定为空
     * @param s 需要判断的字符串
     * @return boolean
     **/
    public static boolean isBlank(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        for (int i = 0; i < s.length(); ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
