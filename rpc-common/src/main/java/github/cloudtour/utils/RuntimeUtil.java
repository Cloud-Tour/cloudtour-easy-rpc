package github.cloudtour.utils;

/**
 * 运行时工具类
 * @author cloudtour
 * @version 1.0
 */
public class RuntimeUtil {
    /**
     * 获取cpu核心数
     * @return int   cpu核心
     **/
    public static int cpus() {
        return Runtime.getRuntime().availableProcessors();
    }
}
