package github.cloudtour.registry.zk;

import github.cloudtour.balance.LoadBalance;
import github.cloudtour.enums.RpcErrorMessageEnum;
import github.cloudtour.exception.RpcException;
import github.cloudtour.extension.ExtensionLoader;
import github.cloudtour.registry.ServiceDiscovery;
import github.cloudtour.registry.zk.util.CuratorUtils;
import github.cloudtour.transmission.dto.RpcRequest;
import github.cloudtour.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 利用zk实现服务发现功能
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public  ZkServiceDiscoveryImpl(){
        //使用一致性hash负载均衡
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("consistentHash");
    }



    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // 负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("已成功找到服务地址:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
