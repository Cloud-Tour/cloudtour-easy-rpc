package github.cloudtour.registry;

import github.cloudtour.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册接口
 * @author cloudtour
 * @version 1.0
 */
@SPI
public interface ServiceRegistry {

    /**
     * 服务注册
     * @param rpcServiceName     rpc服务名称(class name+group+version)
     * @param inetSocketAddress  服务地址
     **/
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
