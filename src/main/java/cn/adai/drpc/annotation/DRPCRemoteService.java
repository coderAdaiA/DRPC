package cn.adai.drpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: coderAdai
 * @date 2022/08/18 22:10
 * @description: TODO
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DRPCRemoteService {
    /**
     * remote server identifier
     */
    String serviceIdentifier();

    /**
     * server impl class(if you have multiple implementations class).
     */
    Class<?> serviceImplClass() default Class.class;
}
