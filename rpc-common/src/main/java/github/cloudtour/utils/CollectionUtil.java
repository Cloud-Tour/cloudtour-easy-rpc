package github.cloudtour.utils;

import java.util.Collection;

/**
 * 集合工具类
 * @author cloudtour
 * @version 1.0
 */
public class CollectionUtil {
    /**
     * 对集合判空
     * @param c 需要判断的集合
     * @return boolean
     **/
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

}
