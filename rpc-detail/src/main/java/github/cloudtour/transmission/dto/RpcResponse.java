package github.cloudtour.transmission.dto;

import github.cloudtour.enums.RpcResponseCodeEnum;
import lombok.*;

import java.io.Serializable;

/**
 * 远程调用响应类型
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcResponse<T> implements Serializable {
    private static final long serialVersionUID = 715745410605631233L;

    private String requestId;
    /**
     * 响应编码
     */
    private Integer code;
    /**
     * 响应消息
     */
    private String message;
    /**
     * 响应数据体
     */
    private T data;

    /**
     * 构建成功响应类
     * @param data  成功响应数据体
     * @param requestId  请求id
     * @return github.cloudtour.transmission.dto.RpcResponse<T>
     **/
    public static <T> RpcResponse<T> success(T data, String requestId) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        response.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        response.setRequestId(requestId);
        if (null != data) {
            response.setData(data);
        }
        return response;
    }

    /**
     * 构建失败响应类
     * @param rpcResponseCodeEnum rpc远程调用响应码枚举类
     * @return github.cloudtour.transmission.dto.RpcResponse<T>
     **/
    public static <T> RpcResponse<T> fail(RpcResponseCodeEnum rpcResponseCodeEnum) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setCode(rpcResponseCodeEnum.getCode());
        response.setMessage(rpcResponseCodeEnum.getMessage());
        return response;
    }
}
