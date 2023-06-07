package github.cloudtour.spring;

import github.cloudtour.annotation.RpcReference;
import github.cloudtour.annotation.RpcService;
import github.cloudtour.config.RpcServiceConfig;
import github.cloudtour.extension.ExtensionLoader;
import github.cloudtour.factory.SingletonFactory;
import github.cloudtour.provider.ServiceProvider;
import github.cloudtour.provider.impl.ZkServiceProviderImpl;
import github.cloudtour.proxy.RpcClientProxy;
import github.cloudtour.transmission.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * 在创建bean之前调用此类中方法，查看类是否被标注
 *
 * @author cloudtour
 * @version 1.0
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {

    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    /**
     * Spring Bean 在实例化之前会调用该方法
     **/
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}]用[{}]进行了注释", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // 获取RpcService注释
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 生成远程调用服务配置
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean).build();
            //将服务进行发布
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * Spring Bean 在实例化之后会调用该方法
     **/
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        //获取类声明的字段
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            //取出有@RpcReference标注的字段
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
