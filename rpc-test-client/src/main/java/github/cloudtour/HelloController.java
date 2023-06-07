package github.cloudtour;


import github.cloudtour.annotation.RpcReference;
import org.springframework.stereotype.Component;

/**
 *
 * @author cloudtour
 * @version 1.0
 */
@Component
public class HelloController {

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Message("111", "222"));
        String s = this.helloService.helloWorld(new Message("你好", "世界"));
    }
}
