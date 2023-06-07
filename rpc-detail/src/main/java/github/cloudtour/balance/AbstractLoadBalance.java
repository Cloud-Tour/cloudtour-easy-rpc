package github.cloudtour.balance;

import github.cloudtour.transmission.dto.RpcRequest;
import github.cloudtour.utils.CollectionUtil;

import java.util.List;

/**
 * 负载平衡策略的抽象类
 * @author cloudtour
 * @version 1.0
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RpcRequest rpcRequest) {
        if (CollectionUtil.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest);

}
