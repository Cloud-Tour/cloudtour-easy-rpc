import github.cloudtour.HelloService;
import github.cloudtour.annotation.RpcScan;
import github.cloudtour.config.RpcServiceConfig;
import github.cloudtour.serviceimpl.HelloServiceImpl2;
import github.cloudtour.transmission.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 服务器：通过@RpcService注释的自动注册服务
 *
 * @author cloudtour
 * @version 1.0
 */
@RpcScan(basePackage = {"github.cloudtour"})
public class NettyServerMain {
    public static void main(String[] args) {
        // 通过注释注册服务
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        // 手动注册服务
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(helloService2).build();

        nettyRpcServer.registerService(rpcServiceConfig);
        nettyRpcServer.start();
    }
}
