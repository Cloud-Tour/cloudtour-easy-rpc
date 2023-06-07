package github.cloudtour.transmission.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 远程调用服务时使用的常量
 * @author cloudtour
 * @version 1.0
 */
public class RpcConstants {
    /**
     * 魔数,用于验证 RpcMessage
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    /**
     * 默认字符串编码-UTF-8
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    /**
     * 版本信息
     */
    public static final byte VERSION = 1;
    /**
     * 消息最少总体长度
     */
    public static final byte TOTAL_LENGTH = 16;
    /**
     * 消息请求类型
     */
    public static final byte REQUEST_TYPE = 1;
    /**
     * 消息响应类型
     */
    public static final byte RESPONSE_TYPE = 2;
    /**
     * 心跳请求类型
     */
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    /**
     * 心跳响应类型
     */
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    /**
     * 头部长度
     */
    public static final int HEAD_LENGTH = 16;

    /**
     * 一条数据的最大长度
     */
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    public static final String PING = "ping";

    public static final String PONG = "pong";
}
