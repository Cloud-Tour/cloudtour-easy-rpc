package github.cloudtour.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * rpc远程调用响应码枚举类
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {

    SUCCESS(200, "远程调用成功"),
    FAIL(500, "远程调用失败");

    private final int code;
    private final String message;
}
