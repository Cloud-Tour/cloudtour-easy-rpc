package github.cloudtour.serialize.kyro;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.cloudtour.exception.SerializeException;
import github.cloudtour.serialize.Serializer;
import github.cloudtour.transmission.dto.RpcRequest;
import github.cloudtour.transmission.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化方式--Kryo的序列化效率很高，但只兼容Java语言
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class KryoSerializer implements Serializer {

    /**
     * 使用ThreadLocal来存储Kryo对象,解决Kryo线程安全问题
     * 对于每一个线程都使用它自己的Kryo对象
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)){
            Kryo kryo = kryoThreadLocal.get();
            //序列化对象
            kryo.writeObject(output,obj);
            //删除kryoThreadLocal中对象引用，防止内存泄露
            kryoThreadLocal.remove();
            return output.toBytes();

        }catch (Exception e){
            throw new SerializeException("Kryo序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoThreadLocal.get();
            //反序列化获得对象
            Object o = kryo.readObject(input,clazz);
            //防止内存泄露
            kryoThreadLocal.remove();
            return clazz.cast(o);
        }catch (Exception e){
            throw new SerializeException("Kryo反序列化失败");
        }
    }
}
