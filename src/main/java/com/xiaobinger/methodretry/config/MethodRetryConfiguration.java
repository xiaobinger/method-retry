package com.xiaobinger.methodretry.config;

import com.xiaobinger.methodretry.aspect.RetryAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
/**
 * @author xiongbing
 * @date 2025/8/25 10:26
 * @description
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = "com.xiaobinger.methodretry")
public class MethodRetryConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MethodRetryConfiguration.class);

    @Bean
    public RetryAspect retryAspect() {
        logger.info("MethodRetryConfiguration retryAspect init");
        return new RetryAspect();
    }

}
