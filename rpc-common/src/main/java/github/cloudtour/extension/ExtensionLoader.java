package github.cloudtour.extension;


import github.cloudtour.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 拓展类加载器，用于获取指定类型的类--实现SPI机制
 * 参考自 dubbo spi: https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/spi/overview/
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public final class ExtensionLoader<T> {

    //服务的目录
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    //拓展类加载器容器
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    //拓展实例容器
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Class<?> type;
    //实例缓存容器
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    //类缓存容器
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取拓展类加载器
     * @param type 类
     * @return github.cloudtour.extension.ExtensionLoader<S>
     **/
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("扩展类型不应为null。");
        }
        if (!type.isInterface()) {
            throw new IllegalArgumentException("扩展类型必须是接口。");
        }
        if (type.getAnnotation(SPI.class) == null) {
            throw new IllegalArgumentException("扩展类型必须由@SPI注释");
        }
        // 首先从缓存中获取，如果未命中，则创建一个
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 获取拓展类实例
     * @param name 拓展类名称
     * @return T
     **/
    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("扩展名不应为null或为空");
        }
        //首先从缓存中获取，如果未命中，请创建一个
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 如果不存在实例，则创建单例
        Object instance = holder.get();
        //双检索构造单例
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 创建拓展类实例
     * @param name 拓展类名称
     * @return T
     **/
    private T createExtension(String name) {
        // 从文件中加载所有类型为T的扩展类，并按名称获取特定的扩展类
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("没有这样的名称扩展：" + name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if (instance == null) {
            try {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    /**
     * 获取当前类加载器中的缓存类容器
     * @return java.util.Map<java.lang.String,java.lang.Class<?>>
     **/
    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        // 双检索，若缓存类容器(即classes)中为空，则去对应的类目录中加载类信息
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 从扩展目录中加载类信息
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * 从扩展目录中加载本加载器type全路径类名下的类信息
     * @param extensionClasses 拓展类容器
     **/
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        //加载META-INF/extensions/type的全路径类名    中的类信息
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    //拿到文件所在磁盘位置
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 加载磁盘路径下的资源--将指定路径下的键值对存放进extensionClasses中
     * @param extensionClasses 拓展类容器
     * @param classLoader 类加载器
     * @param resourceUrl 加载路径
     **/
    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 读取每一行
            while ((line = reader.readLine()) != null) {
                // 获取注释索引
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // #后面的字符串是注释，所以我们忽略它
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // 我们的SPI使用键值对，所以它们都不能为空
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
