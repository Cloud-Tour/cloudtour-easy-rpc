package github.cloudtour.provider;

import github.cloudtour.config.RpcServiceConfig;

/**
 * 存储和提供服务对象--用于服务器端(提供服务一端)--接口
 * @author cloudtour
 * @version 1.0
 */
public interface ServiceProvider {

    /**
     * 添加服务
     * @param rpcServiceConfig rpc服务相关属性
     **/
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务
     * @param rpcServiceName    rpc服务名称
     * @return java.lang.Object  服务类
     **/
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     * @param rpcServiceConfig rpc服务相关属性
     **/
    void publishService(RpcServiceConfig rpcServiceConfig);
}
