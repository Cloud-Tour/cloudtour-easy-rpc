package github.cloudtour.registry;

import github.cloudtour.extension.SPI;
import github.cloudtour.transmission.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现接口
 * @author cloudtour
 * @version 1.0
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 按rpcServiceName查找服务
     * @param rpcRequest    远程调用服务请求
     * @return java.net.InetSocketAddress    返回请求服务的地址
     **/
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
