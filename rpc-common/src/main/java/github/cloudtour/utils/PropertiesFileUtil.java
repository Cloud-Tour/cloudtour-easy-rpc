package github.cloudtour.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 配置文件工具类
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public final class PropertiesFileUtil {
    private PropertiesFileUtil() {
    }

    /**
     * 获取配置文件
     * @param fileName 文件名称
     * @return java.util.Properties
     **/
    public static Properties readPropertiesFile(String fileName) {
        //获取类加载器的地址
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        //读取配置文件到properties中
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("读取配置文件[{}]时发生异常", fileName);
        }
        return properties;
    }
}
