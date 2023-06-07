package github.cloudtour.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 创建线程池的工具类
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class ThreadPoolFactoryUtil {

    //key: threadNamePrefix    value: threadPool
    //通过 threadNamePrefix 来区分不同线程池（可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil() {

    }

    /**
     * 判断某前缀的线程池是否存在，存在则返回，不存在则创建后返回
     * @param threadNamePrefix  线程名前缀
     * @return java.util.concurrent.ExecutorService
     **/
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        CustomThreadPoolConfig customThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 判断某前缀的线程池是否存在，存在则返回，不存在则创建后返回
     * @param threadNamePrefix  线程名前缀
     * @param customThreadPoolConfig 自定义线程池配置
     * @return java.util.concurrent.ExecutorService
     **/
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    /**
     * 判断某前缀的线程池是否存在，存在则返回，不存在则创建后返回
     * @param customThreadPoolConfig    自定义线程池配置
     * @param threadNamePrefix  线程名前缀
     * @param daemon 是否为守护线程
     * @return java.util.concurrent.ExecutorService
     **/
    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        //在容器中查询，没有则创建后返回
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    /**
     * shutDown 所有线程池
     **/
    public static void shutDownAllThreadPool() {
        log.info("调用shutDownAllThreadPool方法");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭线程池: [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("线程池未终结");
                executorService.shutdownNow();
            }
        });
    }

    /**
     * 创建线程池
     * @param customThreadPoolConfig 自定义线程池配置
     * @param threadNamePrefix 线程名前缀
     * @param daemon 是否为守护线程
     * @return java.util.concurrent.ExecutorService
     **/
    private static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 创建线程工厂--用于指定线程池中线程的名称和是否为守护线程
     * @param threadNamePrefix 线程名前缀
     * @param daemon 是否为守护线程
     * @return java.util.concurrent.ThreadFactory
     **/
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix + "-%d")
                        .setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 打印线程池的状态
     * @param threadPool 线程池对象
     **/
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
