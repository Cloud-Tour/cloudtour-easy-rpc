package github.cloudtour.transmission.transport.netty.client;

import github.cloudtour.enums.CompressTypeEnum;
import github.cloudtour.enums.SerializationTypeEnum;
import github.cloudtour.factory.SingletonFactory;
import github.cloudtour.transmission.constants.RpcConstants;
import github.cloudtour.transmission.dto.RpcMessage;
import github.cloudtour.transmission.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 自定义客户端ChannelHandler以处理服务器发送的数据
 *
 * <p>
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {

    //存放服务器未处理的请求
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 读取服务器发送的消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("客户端接收到消息: [{}]", msg);
        if (msg instanceof RpcMessage){
            RpcMessage tmp = (RpcMessage) msg;
            byte messageType = tmp.getMessageType();
            if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                log.info("心跳 [{}]", tmp.getData());
            }else if (messageType == RpcConstants.RESPONSE_TYPE){
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) tmp.getData();
                //设置结果
                unprocessedRequests.complete(rpcResponse);
            }
        }
    }

    /**
     * 用来处理空闲事件
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //客户端发生空闲事件则向服务器发送一个心跳请求
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("发生写入空闲 [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 在处理消息时发生异常时调用
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("客户端捕获异常：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
