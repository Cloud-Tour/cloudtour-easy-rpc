package github.cloudtour.balance.loadbalancer;

import github.cloudtour.balance.AbstractLoadBalance;
import github.cloudtour.transmission.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡策略的实现
 * @author cloudtour
 * @version 1.0
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    /**
     * 随机返回服务地址列表中的一个
     * @param serviceAddresses  服务地址列表
     * @param rpcRequest        rpc请求
     * @return java.lang.String
     **/
    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
