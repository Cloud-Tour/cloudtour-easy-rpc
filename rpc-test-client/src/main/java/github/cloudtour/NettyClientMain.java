package github.cloudtour;

import github.cloudtour.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author cloudtour
 * @version 1.0
 */
@RpcScan(basePackage = {"github.cloudtour"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        //通过spring容器调用helloController的方法
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
