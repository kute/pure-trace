package com.kute.asyncinitbean;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.PriorityOrdered;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
@Slf4j
public class AsyncProxyBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware,
        PriorityOrdered {

    private ApplicationContext applicationContext;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        String methodName = AsyncInitBeanHolder.getAsyncInitMethodName(beanName);
        if (methodName == null || methodName.length() == 0) {
            return bean;
        }
        log.info("AsyncProxyBeanPostProcessor 1 for bean={}, methodName={}", beanName, methodName);
        // 方法增强，即 targetClass中的任何方法被调用时触发增强
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(bean.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(new AsyncInitializeBeanMethodInvoker(bean, beanName, methodName));
        log.info("AsyncProxyBeanPostProcessor 2 for bean={}, methodName={}", beanName, methodName);
        return proxyFactory.getProxy();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    class AsyncInitializeBeanMethodInvoker implements MethodInterceptor {
        private final Object targetObject;
        private final String asyncMethodName;
        private final String beanName;
        private final CountDownLatch initCountDownLatch = new CountDownLatch(1);
        // mark async-init method is during first invocation.
        private volatile boolean isAsyncCalling = false;
        // mark init-method is called.
        private volatile boolean isAsyncCalled = false;

        AsyncInitializeBeanMethodInvoker(Object targetObject, String beanName, String methodName) {
            this.targetObject = targetObject;
            this.beanName = beanName;
            this.asyncMethodName = methodName;
            log.info("AsyncInitializeBeanMethodInvoker construct for bean={}， methodName={}", beanName, methodName);
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            // if the spring refreshing is finished
            if (AsyncTaskExecutor.isStarted()) {
                log.info("AsyncInitializeBeanMethodInvoker 1 for bean={}, invocation={}", beanName, invocation.getMethod().getName());
                return invocation.getMethod().invoke(targetObject, invocation.getArguments());
            }

            log.info("AsyncInitializeBeanMethodInvoker 2 for bean={}, invocation={}", beanName, invocation.getMethod().getName());
            Method method = invocation.getMethod();
            final String methodName = method.getName();
            if (!isAsyncCalled && methodName.equals(asyncMethodName)) {
                log.info("AsyncInitializeBeanMethodInvoker 3 for bean={}", beanName);
                isAsyncCalled = true;
                isAsyncCalling = true;
                AsyncTaskExecutor.submitTask(applicationContext.getEnvironment(), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long startTime = System.currentTimeMillis();
                            log.info(String.format(
                                    "%s(%s) %s method execute %dms.", targetObject
                                            .getClass().getName(), beanName, methodName, (startTime)));
                            invocation.getMethod().invoke(targetObject, invocation.getArguments());
                            log.info(String.format(
                                    "%s(%s) %s method execute %dms.", targetObject
                                            .getClass().getName(), beanName, methodName, (System
                                            .currentTimeMillis() - startTime)));
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        } finally {
                            initCountDownLatch.countDown();
                            isAsyncCalling = false;
                        }
                    }
                });
                return null;
            }

            log.info("AsyncInitializeBeanMethodInvoker 4 for bean={}, isAsyncCalling={}", beanName, isAsyncCalling);
            if (isAsyncCalling) {
                long startTime = System.currentTimeMillis();
                initCountDownLatch.await();
                log.info(String.format("%s(%s) %s method wait %dms: %s.",
                        targetObject.getClass().getName(), beanName, methodName,
                        (System.currentTimeMillis() - startTime)));
            }
            return invocation.getMethod().invoke(targetObject, invocation.getArguments());
        }
    }

}
