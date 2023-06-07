package github.cloudtour.transmission.dto;

import lombok.*;

/**
 * 网络传输消息类
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 序列化类型
     */
    private byte codec;
    /**
     * 压缩类型
     */
    private byte compress;
    /**
     * request id
     */
    private int requestId;
    /**
     * 具体消息数据
     */
    private Object data;
}
