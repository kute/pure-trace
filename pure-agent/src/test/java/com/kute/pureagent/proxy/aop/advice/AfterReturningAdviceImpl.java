package com.kute.pureagent.proxy.aop.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by kute on 2023/6/12 11:37
 */
@Slf4j
public class AfterReturningAdviceImpl implements AfterReturningAdvice {

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        log.info("AfterReturningAdvice returnValue={}, method={}, args={}, target={}",
                returnValue, method.getName(), Arrays.toString(args), target);
    }
}
