package github.cloudtour.transmission.transport;

import github.cloudtour.extension.SPI;
import github.cloudtour.transmission.dto.RpcRequest;

/**
 * 远程请求接口
 * @author cloudtour
 * @version 1.0
 */
@SPI
public interface RpcRequestTransport {

    /**
     * 向服务器发送rpc请求并获取结果
     * @param rpcRequest rpc请求
     * @return java.lang.Object     来自服务器的数据
     **/
    Object sendRpcRequest(RpcRequest rpcRequest);
}
