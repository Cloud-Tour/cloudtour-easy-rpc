package github.cloudtour.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 压缩类型枚举类--用于选择编码时对数据进行压缩的形式
 * @author cloudtour
 * @version 1.0
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {

    GZIP((byte) 0x01, "gzip");

    private final byte code;    //编码
    private final String name;  //名字

    /**
     * 根据编码获得编码类型的名字
     * @param code 编码
     * @return java.lang.String
     **/
    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

}
