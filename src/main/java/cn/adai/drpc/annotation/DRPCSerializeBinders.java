package cn.adai.drpc.annotation;

import java.lang.annotation.*;

/**
 * @author: coderAdai
 * @date 2022/08/21 12:16
 * @description: TODO
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DRPCSerializeBinders {
    DRPCSerializeBinder[] value();
}
