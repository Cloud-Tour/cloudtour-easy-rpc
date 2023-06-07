package github.cloudtour.transmission.transport.netty.codec;

import github.cloudtour.compress.Compress;
import github.cloudtour.enums.CompressTypeEnum;
import github.cloudtour.enums.SerializationTypeEnum;
import github.cloudtour.extension.ExtensionLoader;
import github.cloudtour.serialize.Serializer;
import github.cloudtour.transmission.constants.RpcConstants;
import github.cloudtour.transmission.dto.RpcMessage;
import github.cloudtour.transmission.dto.RpcRequest;
import github.cloudtour.transmission.dto.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * 自定义rpc消息解码器
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
 * 1B codec（序列化类型）      1B compress（压缩类型）     4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} 是一种基于长度的解码器，用于解决TCP的拆包和粘贴问题。
 * </p>
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder{
    public RpcMessageDecoder() {
        //lengthFieldOffset：魔术码是4B，版本是1B，然后就是长度字段，因此该值为5
        //lengthFieldLength：长度字段的占位是4B，英雌该值为4
        //lengthAdjustment：数据包长度(16+body) - lengthFieldOffset(4) - lengthFieldLength(5) - 长度域的值(16+body) = -9
        // initialBytesToStrip：我们将手动检查魔术代码和版本，所以不要剥离任何字节。因此值为0
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * RpcMessageDecoder构造器
     * @param maxFrameLength        发送的数据包最大长度
     * @param lengthFieldOffset     长度字段的偏移量
     * @param lengthFieldLength     长度字段的长度
     * @param lengthAdjustment      长度域的偏移量矫正。 如果长度域的值，除了包含有效数据域的长度外，还包含了其他域（如长度域自身）长度，那么，就需要进行矫正。 矫正值 = 数据包长度 - lengthFieldOffset - lengthFieldLength - 长度域的值
     * @param initialBytesToStrip   接收到的发送数据包，去除前initialBytesToStrip位
     **/
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH){//说明是正常数据包
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("解码器出现错误!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * 解码
     * @param in    要解码的byteBuf
     * @return java.lang.Object 解码后的数据
     **/
    private Object decodeFrame(ByteBuf in){
        //按顺序读取byteBuf
        //检查魔数
        checkMagicNumber(in);
        //检查版本
        checkVersion(in);
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        byte compressType = in.readByte();
        int requestId = in.readInt();
        // 生成RpcMessage对象
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();

        //判断心跳消息
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        //解析数据部分
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength>0){
            byte[] bytes = new byte[bodyLength];
            in.readBytes(bytes);
            //解压数据
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bytes = compress.decompress(bytes);

            //进行反序列化
            String codecName = SerializationTypeEnum.getName(codecType);
            log.info("解码器名称为: [{}] ", codecName);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(codecName);
            if (messageType == RpcConstants.REQUEST_TYPE){
                RpcRequest tmp = serializer.deserialize(bytes, RpcRequest.class);
                rpcMessage.setData(tmp);
            }else {
                RpcResponse tmp = serializer.deserialize(bytes, RpcResponse.class);
                rpcMessage.setData(tmp);
            }


        }
        return rpcMessage;
    }

    /**
     * 检查魔数
     * @param in 要检查的byteBuf
     **/
    private void checkMagicNumber(ByteBuf in) {
        // 读取前4位，即魔数，并进行比较
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("未知的魔数: " + Arrays.toString(tmp));
            }
        }
    }

    /**
     * 检查版本
     * @param in 要检查的byteBuf
     **/
    private void checkVersion(ByteBuf in) {
        // 读取版本并进行比较
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("版本不兼容" + version);
        }
    }

}
