package github.cloudtour.registry.zk.util;

import github.cloudtour.enums.RpcConfigEnum;
import github.cloudtour.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Curator(zookeeper client) utils--用于使用zookeeper实现服务注册和发现功能
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    //zk存储根节点路径
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    //服务地址容器
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //已注册的路径集合
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private CuratorUtils() {
    }

    /**
     * 创建持久节点。与临时节点不同，当客户端断开连接时，不会删除持久节点
     * @param zkClient  zk客户端
     * @param path 节点路径
     **/
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("节点已存在。节点为:[{}]", path);
            } else {
                //eg: /my-rpc/github.cloudtour.HelloService/127.0.0.1:9999
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("节点已成功创建。节点为:[{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("为路径：[{}] 创建持久节点失败", path);
        }
    }

    /**
     * 获取节点下的子级
     * @param zkClient  zk客户端
     * @param rpcServiceName    远程服务名称  eg：github.cloudtour.HelloServicetest2version1
     * @return java.util.List<java.lang.String>     返回指定节点下的所有子节点
     **/
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            //获取zk中对应的子集并存入缓存中
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("获取路径 [{}] 的子节点失败", servicePath);
        }
        return result;
    }


    /**
     * 清空注册表中的与服务地址相符的数据
     * @param zkClient  zk客户端
     * @param inetSocketAddress   服务地址
     **/
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                //检查尾部内容是否相同
                if (p.endsWith(inetSocketAddress.toString())) {
                    //删除zk中对应的已注册服务
                    zkClient.delete().forPath(p);
                    REGISTERED_PATH_SET.remove(p);
                }
            } catch (Exception e) {
                log.error("清除路径 [{}] 的注册表失败", p);
            }
        });
        log.info("[{}] 对应服务器上所有注册的服务已清除:", inetSocketAddress.toString());
    }

    /**
     * 获取zk客户端
     * @return org.apache.curator.framework.CuratorFramework    返回zk客户端
     **/
    public static CuratorFramework getZkClient() {
        // 检查用户是否设置了zk地址，若未设置则用默认地址 127.0.0.1:2181
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;
        // 如果zkClient已经启动，则直接返回
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略。重试3次，并将增加重试之间的睡眠时间。
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                // 要连接的服务器（可以是服务器列表）
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            // 等待30s，直到连接zookeeper
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("等待连接ZK超时！");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    /**
     * 给对应的服务注册监听事件(监听更改)
     * @param rpcServiceName    远程服务名称  eg：github.cloudtour.HelloServicetest2version1
     * @param zkClient  zk客户端
     **/
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        //给服务对应的路径绑定监听时间，一当对应路径下的节点发生变更，则更新缓存(即:SERVICE_ADDRESS_MAP)
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }
}
