package github.cloudtour.transmission.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储和获取通道对象
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class ChannelProvider {
    private final Map<String, Channel>  channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    /**
     * 根据地址获取通道对象
     * @param inetSocketAddress 地址
     * @return io.netty.channel.Channel 通道对象
     **/
    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        //确定是否存在对应地址的连接
        if (channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            //确定连接是否可用，如果可用，则直接获取连接
            if (channel !=null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    /**
     * 将管道存储数据存储进缓存中
     * @param inetSocketAddress 地址
     * @param channel 管道
     **/
    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    /**
     * 删除管道
     * @param inetSocketAddress 想要删除的管道对应的路径
     **/
    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("存储管道容器的大小 :[{}]", channelMap.size());
    }
}
