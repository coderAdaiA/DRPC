package cn.adai.drpc.annotation;

import java.lang.annotation.*;

/**
 * @author: coderAdai
 * @date 2022/08/21 11:11
 * @description: TODO
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(value = DRPCSerializeBinders.class)
public @interface DRPCSerializeBinder {
    /**
     * remote class name
     */
    String remoteClassName();

    /**
     * current class
     */
    Class<?> currentClass();
}
