package github.cloudtour.transmission.handler;

import github.cloudtour.exception.RpcException;
import github.cloudtour.factory.SingletonFactory;
import github.cloudtour.provider.ServiceProvider;
import github.cloudtour.provider.impl.ZkServiceProviderImpl;
import github.cloudtour.transmission.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 远程请求处理器--服务端使用，用于调用rpc请求中的目标类的目标方法
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class RpcRequestHandler {

    /**
     * 服务提供类
     */
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    /**
     * 处理rpcRequest：调用rpc请求中相应的方法，然后返回该方法的返回值
     * @param rpcRequest rpc请求
     * @return java.lang.Object 调用方法的结果
     **/
    public Object handle(RpcRequest rpcRequest) {
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest, service);
    }

    /**
     * 利用反射获取方法执行结果
     * @param rpcRequest    客户端发过来的rpc请求
     * @param service       rpc请求中对应的服务类
     * @return java.lang.Object     目标方法执行的结果
     **/
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            result = method.invoke(service, rpcRequest.getParameters());
            log.info("服务:[{}] ->成功调用方法:[{}]", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RpcException(e.getMessage(), e);
        }
        return result;
    }
}
