package com.kute.agent.main;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.Callable;

/**
 * 方法拦截实现，见com.kute.agent.main.PureAgentMain#premain(java.lang.String, java.lang.instrument.Instrumentation)
 */
public class DoDispatchInterceptor {

    /**
     * @param request
     * @param callable
     * @return
     * @Argument 绑定单个参数，参数的绑定需要参考原始方法
     * @SuperCall 用于调用父类版本的方法，对原方法的封装
     * @RuntimeType 可以用在返回值、参数上，提示ByteBuddy禁用严格的类型检查
     */
    @RuntimeType
    public static Object intercept(@Argument(0) HttpServletRequest request, @SuperCall Callable<?> callable) {
        final StringBuilder in = new StringBuilder();
        if (request.getParameterMap() != null && request.getParameterMap().size() > 0) {
            request.getParameterMap().keySet().forEach(key -> in.append("key=" + key + "_value=" + request.getParameter(key) + ","));
        }
        long agentStart = System.currentTimeMillis();
        try {
            return callable.call();
        } catch (Exception e) {
            System.out.println("Exception :" + e.getMessage());
            return null;
        } finally {
            System.out.println("path:" + request.getRequestURI() + " 入参:" + in + " 耗时:" + (System.currentTimeMillis() - agentStart));
        }
    }
}