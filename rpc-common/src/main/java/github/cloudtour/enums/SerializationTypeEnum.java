package github.cloudtour.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列化类型枚举类--这里给出kyro、protostuff、hessian三种序列化形式
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public enum  SerializationTypeEnum {

    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0X03, "hessian");

    private final byte code;
    private final String name;

    /**
     * 根据编码获取序列化类型
     * @param code 编码
     * @return java.lang.String
     **/
    public static String getName(byte code) {
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
