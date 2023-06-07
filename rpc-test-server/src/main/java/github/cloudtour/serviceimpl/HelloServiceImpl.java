package github.cloudtour.serviceimpl;


import github.cloudtour.HelloService;
import github.cloudtour.Message;
import github.cloudtour.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Message message) {
        log.info("HelloServiceImpl收到: {}.", message.getMessage());
        String result = "Hello description is " + message.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }

    @Override
    public String helloWorld(Message message) {
        log.info("HelloServiceImpl收到: {}.", message.getMessage());
        String result = "helloWorld description is " + message.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
