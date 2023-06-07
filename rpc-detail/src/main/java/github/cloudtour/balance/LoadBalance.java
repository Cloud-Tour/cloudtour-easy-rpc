package github.cloudtour.balance;

import github.cloudtour.extension.SPI;
import github.cloudtour.transmission.dto.RpcRequest;

import java.util.List;

/**
 * 负载均衡策略接口
 * @author cloudtour
 * @version 1.0
 */
@SPI
public interface LoadBalance {

    /**
     * 从现有服务地址列表中选择一个
     * @param serviceUrlList    服务地址列表
     * @param rpcRequest        rpc请求
     * @return java.lang.String 返回其中一个地址
     **/
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);

}
