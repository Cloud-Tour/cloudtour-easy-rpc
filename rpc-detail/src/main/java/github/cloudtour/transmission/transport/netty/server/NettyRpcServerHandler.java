package github.cloudtour.transmission.transport.netty.server;

import github.cloudtour.enums.CompressTypeEnum;
import github.cloudtour.enums.RpcResponseCodeEnum;
import github.cloudtour.enums.SerializationTypeEnum;
import github.cloudtour.factory.SingletonFactory;
import github.cloudtour.transmission.constants.RpcConstants;
import github.cloudtour.transmission.dto.RpcMessage;
import github.cloudtour.transmission.dto.RpcRequest;
import github.cloudtour.transmission.dto.RpcResponse;
import github.cloudtour.transmission.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义客户端ChannelHandler以处理客户端发送的数据
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage){
                log.info("服务器接收消息: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                //构造返回消息
                RpcMessage rpcMessage = RpcMessage.builder().codec(SerializationTypeEnum.HESSIAN.getCode())
                        .compress(CompressTypeEnum.GZIP.getCode()).build();
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){//若是心跳则构建心跳消息
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else {//处理rpc请求
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    //执行目标方法（客户端需要执行的方法）并返回方法结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info("服务器获取结果: [{}]", result.toString());
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("无法写入，消息已删除");
                    }

                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            //确保ByteBuf已释放，防止出现内存泄漏
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     *  处理空闲事件
     **/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //服务器触发空闲事件则自动关闭连接
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("发生空闲检查，服务器关闭连接");
                ctx.close();
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
        log.error("服务器捕获异常");
        cause.printStackTrace();
        ctx.close();
    }
}
