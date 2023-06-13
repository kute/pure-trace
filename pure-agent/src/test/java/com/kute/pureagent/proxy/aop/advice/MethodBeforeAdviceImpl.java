package com.kute.pureagent.proxy.aop.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by kute on 2023/6/12 11:40
 */
@Slf4j
public class MethodBeforeAdviceImpl implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        log.info("MethodBeforeAdvice method={}, args={}, target={}",
                method.getName(), Arrays.toString(args), target);
    }
}
