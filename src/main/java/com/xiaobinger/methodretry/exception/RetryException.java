package com.xiaobinger.methodretry.exception;

/**
 * @author xiongbing
 * @date 2025/8/25 18:56
 * @description 重试异常
 */
public class RetryException extends RuntimeException {

    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
