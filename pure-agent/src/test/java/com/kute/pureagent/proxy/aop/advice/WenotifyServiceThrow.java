package com.kute.pureagent.proxy.aop.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.ThrowsAdvice;

import javax.servlet.ServletException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by kute on 2023/6/8 16:29
 * <p>
 * 方法命名都以 afterThrowing命名，最后一个是 异常，具体见 ThrowsAdviceInterceptor
 */
@Slf4j
public class WenotifyServiceThrow implements ThrowsAdvice {

    public void afterThrowing(Exception ex) {
        log.info("afterThrowing ex 1");
    }

    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
        log.info("afterThrowing ex 2 for method={}, args={}, target={}",
                method.getName(), Arrays.toString(args), target);
    }

    public void afterThrowing(Method method, Object[] args, Object target, ServletException ex) {
        log.info("afterThrowing ex 3 for method={}, args={}, target={}",
                method.getName(), Arrays.toString(args), target);
    }

}
