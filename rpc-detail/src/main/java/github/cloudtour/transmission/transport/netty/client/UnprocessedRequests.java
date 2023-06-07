package github.cloudtour.transmission.transport.netty.client;

import github.cloudtour.transmission.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放服务器未处理的请求
 *
 * @author cloudtour
 * @version 1.0
 */
public class UnprocessedRequests {
    /**
     *  存放服务器未处理的请求容器
     **/
    public static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    /**
     * 将未处理的请求添加进容器中
     * @param requestId    请求id
     * @param future       为处理的请求
     **/
    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    /**
     * 将已完成的请求从容器中删除
     * @param rpcResponse rpc响应
     **/
    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (null != future) {
            //设置结果
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
