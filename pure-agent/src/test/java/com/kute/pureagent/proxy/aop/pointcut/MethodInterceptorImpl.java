package com.kute.pureagent.proxy.aop.pointcut;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by kute on 2023/6/12 11:37
 */
@Slf4j
public class MethodInterceptorImpl implements MethodInterceptor {

    private Object target;

    public MethodInterceptorImpl() {
    }

    public MethodInterceptorImpl(Object target) {
        this.target = target;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        log.debug("begin log MethodInterceptor before method={}", methodName);
        Object rs;
        if (null != target) {
            rs = invocation.getMethod().invoke(target, invocation.getArguments());
        } else {
            rs = invocation.proceed();
        }
        log.debug("after log MethodInterceptor after method={}, rs={}", methodName, rs);
        return rs;
    }
}
