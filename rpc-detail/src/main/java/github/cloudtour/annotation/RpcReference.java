package github.cloudtour.annotation;

import java.lang.annotation.*;


/**
 * RPC引用注释，自动连接服务实现类
 * @author cloudtour
 * @version 1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RpcReference {

    /**
     * 服务版本，默认值为空字符串
     */
    String version() default "";

    /**
     * 服务组，默认值为空字符串
     */
    String group() default "";

}