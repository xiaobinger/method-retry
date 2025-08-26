package com.xiaobinger.methodretry.aspect;

import com.xiaobinger.methodretry.exception.RetryException;

import java.lang.annotation.*;

/**
 * @author xiongbing
 * @date 2025/8/25 10:03
 * @description 重试注解 - 支持SpEL表达式
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Retry {
    /**
     * 重试次数
     */
    int count() default 3;

    /**
     * 重试条件表达式
     * 使用SpEL表达式，#result表示方法返回值
     * 例如: "#result == null" 或 "#result.isEmpty()"
     */
    String retryFor() default "";

    /**
     * 重试异常类型
     */
    Class<? extends Throwable>[] retryWithExceptions() default {RetryException. class};
}
