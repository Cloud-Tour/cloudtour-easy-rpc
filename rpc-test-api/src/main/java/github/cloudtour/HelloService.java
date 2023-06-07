package github.cloudtour;

/**
 * 远程调用接口
 *
 * @author cloudtour
 * @version 1.0
 */
public interface HelloService {
    String hello(Message message);

    String helloWorld(Message message);
}
