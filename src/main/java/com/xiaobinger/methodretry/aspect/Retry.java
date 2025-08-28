package com.xiaobinger.methodretry.aspect;

import com.xiaobinger.methodretry.exception.RetryException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
     * 异步重试
     */
    boolean isAsync() default false;
    /**
     * 首次重试延迟时间 毫秒 默认0秒
     */
    long delay() default 0L;
    /**
     * 重试间隔时间 毫秒 默认1秒
     */
    long retryInterval() default 0L;
    /**
     * 重试异常类型
     */
    Class<? extends Throwable>[] retryWithExceptions() default {RetryException. class};
}
