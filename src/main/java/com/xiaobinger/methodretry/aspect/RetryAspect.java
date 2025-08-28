
package com.xiaobinger.methodretry.aspect;

import com.xiaobinger.methodretry.exception.RetryException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


/**
 * @author xiongbing
 */
@Aspect
public class RetryAspect {

    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();


    @Pointcut("@annotation(com.xiaobinger.methodretry.aspect.Retry)")
    public void retryPointcut() {
    }

    @Around("retryPointcut()")
    public Object retryForCondition(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Retry retry = signature.getMethod().getAnnotation(Retry.class);
        // 首次执行
        Object result = joinPoint.proceed();
        boolean shouldRetry = shouldRetry(result, retry.retryFor());
        if (!shouldRetry) {
            return result;
        }
        if (retry.isAsync()) {
            asyncRetry(joinPoint, retry, signature);
            return result;
        }
        //同步重试首次延迟时间
        sleepWithInterval(retry.delay());
        return syncRetry(joinPoint, retry, signature);
    }

    private void asyncRetry(ProceedingJoinPoint joinPoint, Retry retry, MethodSignature signature) {
        CompletableFuture.supplyAsync(() -> syncRetry(joinPoint, retry, signature)).join();
    }


    private Object syncRetry(ProceedingJoinPoint joinPoint, Retry retry,MethodSignature signature) {
        int count = retry.count();
        String condition = retry.retryFor();
        return reTry( v -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                log.error("方法重试异常，请求路径：{}", signature.getMethod().getName(), e);
                if (isRetryException(e, retry.retryWithExceptions())) {
                    throw new RetryException(e.getMessage(), e);
                }
                throw new RuntimeException(e);
            }
        }, (v) -> shouldRetry(v, condition), count,retry.retryInterval());
    }

    private boolean isRetryException(Throwable e, Class<? extends Throwable>[] classes) {
        if (classes == null) {
            return false;
        }
        for (Class<? extends Throwable> aClass : classes) {
            if (aClass.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldRetry(Object result, String condition) {
        try {
            Expression expression = parser.parseExpression(condition);
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("result", result);
            return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
        } catch (Exception e) {
            // 如果表达式解析失败，默认不重试
            return false;
        }
    }


    /**
     * 方法重试
     * @param func 需要重试的方法
     * @param reTryCondition 重试满足的条件
     * @param reTryCount 重试次数
     * @return 重试方法的返回值
     */
    private static <V> V reTry(Function<Void,V> func, Function<V,Boolean> reTryCondition,
                               int reTryCount,long retryInterval) {
        reTryCount = reTryCount - 1;
        V v = null;
        boolean reTry;
        try {
            v = func.apply(null);
            reTry = reTryCondition.apply(v);
        } catch (RetryException e) {
            //命中重试异常
            reTry = true;
            if (reTryCount <= 0) {
                throw e;
            }
        }
        if (reTry && reTryCount > 0) {
            sleepWithInterval(retryInterval);
            v = reTry(func,reTryCondition,reTryCount,retryInterval);
        }
        return v;
    }

    private static void sleepWithInterval(long retryInterval) {
        if (retryInterval <= 0L) {
            return;
        }
        try {
            Thread.sleep(retryInterval);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}