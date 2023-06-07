package github.cloudtour.transmission.transport.netty.codec;

import github.cloudtour.compress.Compress;
import github.cloudtour.enums.CompressTypeEnum;
import github.cloudtour.enums.SerializationTypeEnum;
import github.cloudtour.extension.ExtensionLoader;
import github.cloudtour.serialize.Serializer;
import github.cloudtour.transmission.constants.RpcConstants;
import github.cloudtour.transmission.dto.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * 自定义rpc消息编码器
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B codec（序列化类型）     1B compress（压缩类型）     4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    /**
     *  原子整数，用来生成请求的id
     **/
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcMessage rpcMessage, ByteBuf byteBuf){
        try {
            byteBuf.writeBytes(RpcConstants.MAGIC_NUMBER);//魔数
            byteBuf.writeByte(RpcConstants.VERSION); //版本
            //预留位置来写完整长度的值
            byteBuf.writerIndex(byteBuf.writerIndex() + 4);
            byte messageType = rpcMessage.getMessageType();
            byteBuf.writeByte(messageType);     //消息类型
            byteBuf.writeByte(rpcMessage.getCodec());   //消息编码格式
            byteBuf.writeByte(rpcMessage.getCompress()); //消息压缩类型编码
            byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement()); //请求的id

            byte[] bodyBytes = null;
            //获取总体长度
            int fullLength = RpcConstants.HEAD_LENGTH;
            //如果messageType不是heartbeat(心跳)消息，则fullLength=头部长度+正文长度]
            if (messageType!= RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                //序列化数据部分
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                log.info("使用的编码器为：[{}]",codecName);
                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
                bodyBytes = serializer.serialize(rpcMessage.getData());
                //压缩数据体
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
                bodyBytes = compress.compress(bodyBytes);
                fullLength += bodyBytes.length;
            }

            if (bodyBytes!=null){//说明数据正常且不是心跳数据
                byteBuf.writeBytes(bodyBytes);
            }

            //补充头部格式中的数据长度内容
            int writerIndex = byteBuf.writerIndex();
            //先定位到头部格式存放数据长度的位置
            byteBuf.writerIndex(writerIndex-fullLength+RpcConstants.MAGIC_NUMBER.length +1);
            byteBuf.writeInt(fullLength);
            byteBuf.writerIndex(writerIndex);
        } catch (Exception e) {
            log.error("编码请求错误！", e);
        }
    }
}
