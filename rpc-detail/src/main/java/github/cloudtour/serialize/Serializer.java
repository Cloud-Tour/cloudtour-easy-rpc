package github.cloudtour.serialize;

import github.cloudtour.extension.SPI;

/**
 * 自定义序列化接口--所有序列化方式都需要实现该接口
 * @author cloudtour
 * @version 1.0
 */
@SPI
public interface Serializer {

    /**
     * 序列化
     * @param obj  要序列化的对象
     * @return byte[]
     **/
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes    需要反序列化的字节数组
     * @param clazz    反序列化后的类类型
     * @return T
     **/
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
