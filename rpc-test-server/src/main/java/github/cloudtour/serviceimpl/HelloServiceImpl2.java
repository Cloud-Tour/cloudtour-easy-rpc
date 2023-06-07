package github.cloudtour.serviceimpl;


import github.cloudtour.HelloService;
import github.cloudtour.Message;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Message message) {
        log.info("HelloServiceImpl2收到: {}.", message.getMessage());
        String result = "Hello description is " + message.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }

    @Override
    public String helloWorld(Message message) {
        log.info("HelloServiceImpl2收到: {}.", message.getMessage());
        String result = "helloWorld description is " + message.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
