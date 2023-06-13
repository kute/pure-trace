package com.kute.pureagent.proxy;


import com.kute.pureagent.proxy.aop.AdvisedSupportListenerImpl;
import com.kute.pureagent.proxy.aop.advice.AfterReturningAdviceImpl;
import com.kute.pureagent.proxy.aop.advice.MethodBeforeAdviceImpl;
import com.kute.pureagent.proxy.aop.advice.WenotifyServiceThrow;
import com.kute.pureagent.proxy.aop.pointcut.MethodInterceptorImpl;
import com.kute.pureagent.proxy.aop.advisor.NameMatchMethodPointcutAdvisorImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by kute on 2018/6/8 16:26
 * aop 代理测试
 */
@Slf4j
public class ProxyTest {

    private AdvisedSupportListenerImpl listener = new AdvisedSupportListenerImpl();

    @Test
    public void test() {
        // 编程式 aop ：ProxyFactory
        // 声明 target 方式
        ProxyFactory proxyFactory = new ProxyFactory(new WenotifyService());
        proxyFactory.setProxyTargetClass(true); // cglib
        proxyFactory.addListener(listener);

        // 添加增强
        // 方法拦截
        proxyFactory.addAdvice(new MethodInterceptorImpl());

        // 增强
        // 方法返回后执行
        AfterReturningAdviceImpl advice = new AfterReturningAdviceImpl();
//        proxyFactory.addAdvice(advice);

        // 方法执行前
        proxyFactory.addAdvice(new MethodBeforeAdviceImpl());

        // 异常时触发，实现 ThrowsAdvice 接口，方法命名都以  afterThrowing ，具体见 ThrowsAdviceInterceptor
        proxyFactory.addAdvice(new WenotifyServiceThrow());

        // advisor = advice + pointcut
        NameMatchMethodPointcutAdvisorImpl advisor = new NameMatchMethodPointcutAdvisorImpl();
//        advisor.addMethodName("notify"); // 方法名匹配
        advisor.setMappedNames("get*", "noti*");  // 通配符匹配，匹配实现：org.springframework.util.PatternMatchUtils.simpleMatch(java.lang.String, java.lang.String)
        advisor.setAdvice(advice); // 设置对应的增强
        proxyFactory.addAdvisor(advisor);

        // 当调用方法时触发增，未拦截 toString, equals, hashCode 方法
        WenotifyService wenotifyServiceProxy = (WenotifyService) proxyFactory.getProxy();
        log.info("{}", wenotifyServiceProxy.notify("notify message"));
        log.info("{}", wenotifyServiceProxy.wechat("a", "b"));
        // 测试异常增强，测试时打开注释
//        wenotifyServiceProxy.throwException();
    }

    @Test
    public void test1() {
        WenotifyService wenotifyService = new WenotifyService();

        ProxyFactory proxyFactory = new ProxyFactory();
        // 如果是 setTargetClass 这种方式下，就不能使用 invocation.proceed ，因为target 是 null，即 EmptyTargetSource，需要使用原始 invoke
        proxyFactory.setTargetClass(wenotifyService.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addListener(listener);

        // 添加增强
        // 方法拦截，需要声明 target
        proxyFactory.addAdvice(new MethodInterceptorImpl(wenotifyService));

        // 增强，因为 target 为空，所以 AfterReturningAdvice 和 MethodBeforeAdvice 都无效

        WenotifyService wenotifyServiceProxy = (WenotifyService) proxyFactory.getProxy();
        Assert.assertNotEquals(wenotifyService, wenotifyServiceProxy);
        log.info("{}", wenotifyServiceProxy.notify("notify message"));
        log.info("{}", wenotifyServiceProxy.wechat("a", "b"));
    }

    @Test
    public void test2() {
        // 测试 jdk 代理
        ITransport transport = message -> {
            log.info("test2 ITransport impl for message={}", message);
            return message;
        };
        ProxyFactory proxyFactory = new ProxyFactory(transport);
        proxyFactory.addListener(listener);

        proxyFactory.addAdvice(new MethodInterceptorImpl());

        ITransport transportProxy = (ITransport) proxyFactory.getProxy();
        transportProxy.transport("xx");
    }

    @Test
    public void test3() {

        ITransport transport = message -> {
            log.info("test2 ITransport impl for message={}", message);
            return message;
        };

        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // proxy 就是代理对象，newProxyInstance方法的返回对象
                log.info("invocationHandler begin for method={}", method.getName());
                return method.invoke(transport, args) + "yy";
            }
        };
        ITransport transportProxy = (ITransport) Proxy.newProxyInstance(
                ITransport.class.getClassLoader(),
                new Class[]{ITransport.class},
                invocationHandler);
        log.info("{}", transportProxy.transport("xx"));
    }

}
