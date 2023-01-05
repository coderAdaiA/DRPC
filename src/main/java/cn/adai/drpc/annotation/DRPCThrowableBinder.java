package cn.adai.drpc.annotation;

import java.lang.annotation.*;

/**
 * @author: coderAdai
 * @date 2022/08/21 13:41
 * @description: TODO
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DRPCThrowableBinder {
    Class<? extends Throwable> exceptionClass();
}
