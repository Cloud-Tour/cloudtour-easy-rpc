package github.cloudtour.transmission.transport.netty.client;

import github.cloudtour.enums.CompressTypeEnum;
import github.cloudtour.enums.SerializationTypeEnum;
import github.cloudtour.extension.ExtensionLoader;
import github.cloudtour.factory.SingletonFactory;
import github.cloudtour.registry.ServiceDiscovery;
import github.cloudtour.transmission.constants.RpcConstants;
import github.cloudtour.transmission.dto.RpcMessage;
import github.cloudtour.transmission.dto.RpcRequest;
import github.cloudtour.transmission.dto.RpcResponse;
import github.cloudtour.transmission.transport.RpcRequestTransport;
import github.cloudtour.transmission.transport.netty.codec.RpcMessageDecoder;
import github.cloudtour.transmission.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 初始化并关闭Bootstrap对象
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    //服务发现
    private final ServiceDiscovery serviceDiscovery;
    //未完成的请求
    private final UnprocessedRequests unprocessedRequests;
    //存放通道
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient() {
        //初始化资源
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) //设置超时时间，如果超过此时间或无法建立连接，则连接将失败
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //空闲事件处理器，如果在10秒内没有向服务器发送数据，则会触发空闲事件
                        pipeline.addLast(new IdleStateHandler(0,10,0, TimeUnit.SECONDS));
                        pipeline.addLast(new RpcMessageEncoder());     //编码器
                        pipeline.addLast(new RpcMessageDecoder());     //解码器
                        pipeline.addLast(new NettyRpcClientHandler()); //数据处理器

                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension("zk");
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //构造返回值
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //获取服务器地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        //获取服务通道
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()){
            //放入未处理的请求
            unprocessedRequests.put(rpcRequest.getRequestId(),resultFuture);
            //构造rpc消息  ---这里的序列化方式、压缩形式都写死了,可以通过方法重载来进行指定
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            //向通道写入并绑定监听器
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("客户端发送消息: [{}]", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("发送失败:", future.cause());
                }
            });
        }else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    /**
     * 获取通道
     * @param inetSocketAddress 想要获取通道的地址
     * @return io.netty.channel.Channel
     **/
    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }

    /**
     * 连接服务器并获取通道，这样就可以向服务器发送rpc消息
     * @param inetSocketAddress 连接服务器的地址
     * @return io.netty.channel.Channel 通道
     **/
    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        //构建连接并绑定一个监听器(收到返回值时触发)
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端已成功连接[{}] !", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        //异步等待结果返回
        return completableFuture.get();
    }

}
