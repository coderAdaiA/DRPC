package cn.adai.drpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: coderAdai
 * @date 2022/08/18 11:40
 * @description: TODO
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DRPCRemoteClient {

    /**
     * remote server instance name
     */
    String serverName();

    /**
     * remote server identifier
     */
    String serviceIdentifier();
}
