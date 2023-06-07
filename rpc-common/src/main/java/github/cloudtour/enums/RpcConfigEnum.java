package github.cloudtour.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * rpc配置枚举类
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {

    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");

    private final String propertyValue;
}
