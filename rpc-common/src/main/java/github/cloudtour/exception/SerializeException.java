package github.cloudtour.exception;

/**
 * 自定义序列化异常
 * @author cloudtour
 * @version 1.0
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String message) {
        super(message);
    }
}
