package com.kute.junit5demo;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

/**
 * created by kute at 2022/4/24 下午2:22
 */
public class ThreadLocalParameterResolver extends TypeBasedParameterResolver<ThreadLocalParameterResolver.MyContext> {

    public static final ThreadLocal<MyContext> testConextThreadLocal = new ThreadLocal<>();

    @Override
    public MyContext resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (testConextThreadLocal.get() == null) {
            testConextThreadLocal.set(new MyContext());
        }
        return testConextThreadLocal.get();
    }

    public static class MyContext {

        private Integer id;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }
    }
}
