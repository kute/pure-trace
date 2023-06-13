package com.kute.pureagent.proxy.aop;

/**
 * 5. AOP 基础
 * 5.1 Advice 通知
 * 具体做事情的类，也就是说你需要如何做或者做什么，都在 Advice 里面实现，目前常用的有
 * <p>
 * AfterReturningAdvice：方法执行后运行
 * MethodBeforeAdvice：方法调用前执行
 * ThrowsAdvice：出现异常后调用。
 * 5.2 Pointcut
 * 定义什么样的东西需要被拦截。比如说以 get 开通我就拦截。常用有：
 * <p>
 * NameMatchMethodPointcut：通过名称
 * AspectJExpressionPointcut：通过使用一套表达式
 * 5.3 Advisor
 * Advisor = Advice + Pointcut。常用有：
 * <p>
 * AspectJExpressionPointcutAdvisor
 * NameMatchMethodPointcutAdvisor
 * 6. 总结
 * 在AOP构建主线的过程中，提供了很多扩展的空间。比如：
 * <p>
 * AopProxyFactory：如果不满足现在有的 JDK和CGLib 的动态代理实现，可以自定义实现。
 * AdvisedSupportListener：可以注册监听动作
 * AdvisorChainFactory：在具体实现调用增强的时候可以进行优化
 */