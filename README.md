# 方法重试
```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaobinger</groupId>
        <artifactId>method-retry</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```
tips:
在需要重试的方法上加上@Retry注解
注解有三个参数：
1. 重试次数 count 默认3 需要设置其他次数则手动设置，否则可以不加该参数
2. 重试条件 retryFor 支持SpEL表达式 #result表示方法的返回值，必须设置重试条件
3. 需要重试的异常集合 默认 RetryException.class 可以不设置，如果要设置其他异常则需要手动设置,也可以在业务中判断需要重试的地方抛出RetryException
示例:
```java
@Retry(count = 3, retryFor = "#result.code == 500", value = {RetryException.class})
public String retryMethod(String param) {return "retryMethod";}
```

版本记录:
<li>v1.0.0 创建</li>
<li>v1.0.1 首次执行异常拦截处理</li>
<li>v1.0.2 首次异常判断是否命中重试异常,否则中断重试</li>
需要注意的一个点是,该注解只对Spring容器管理的Bean代理的方法有效。

