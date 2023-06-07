package github.cloudtour.transmission.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Rpc请求类型
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    /**
     * request id
     */
    private String requestId;
    /**
     * 接口名称
     */
    private String interfaceName;
    /**
     * 方法名称
     */
    private String methodName;
    /**
     * 方法参数
     */
    private Object[] parameters;
    /**
     * 方法参数类型
     */
    private Class<?>[] paramTypes;
    /**
     * 版本
     */
    private String version;
    /**
     * 所在组
     */
    private String group;

    /**
     * 获取远程请求服务名称
     * @return java.lang.String
     **/
    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
