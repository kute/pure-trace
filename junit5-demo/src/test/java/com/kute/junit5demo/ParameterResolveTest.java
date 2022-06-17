package com.kute.junit5demo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.support.TypeBasedParameterResolver;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Date;

/**
 * created by kute at 2022/4/22 下午4:06
 * <p>
 * 参数注入以及解析
 */
// 定义解析器
@ExtendWith(value = {
        ParameterResolveTest.RandomIntParamResolve.class,
        ParameterResolveTest.DateResolve.class,
        ThreadLocalParameterResolver.class
})
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class ParameterResolveTest {

    /**
     * unit会自动注入这些上下文
     *
     * @param testInfo 被 TestInfoParameterResolver 解析
     * @param testReporter 被 TestReporterParameterResolver 解析
     */
    @Test
    @Order(1)
    public void test2(TestInfo testInfo,
                      ThreadLocalParameterResolver.MyContext myContext,
                      TestReporter testReporter,
                      @RandomIntParamResolve.Random Integer random,
                      Date date) {
        System.out.println("hello");
        testReporter.publishEntry("ddddddd");
        System.out.println(testInfo);
        System.out.println(testReporter);
        System.out.println(random);
        System.out.println(date);

        // 这里设置的数据，可以在其他 test单测里 获取到，只要保证 当前设置属性的单测方法先执行
        myContext.setId(11);
    }

    @Order(2)
    @Test
    public void test2(ThreadLocalParameterResolver.MyContext myContext) {
        Assertions.assertEquals(11, myContext.getId());
    }

    /**
     * 自定义参数解析
     */
    public static class RandomIntParamResolve implements ParameterResolver {

        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.PARAMETER)
        public @interface Random {
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
//            return parameterContext.getParameter().isAnnotationPresent(Random.class);
//            return org.junit.platform.commons.support.AnnotationSupport.findAnnotation(extensionContext.getTestMethod(), Random.class).isPresent();
            return parameterContext.isAnnotated(Random.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return getRandomValue(parameterContext.getParameter(), extensionContext);
        }

        private Object getRandomValue(Parameter parameter, ExtensionContext extensionContext) {
            Class<?> type = parameter.getType();
            java.util.Random random = extensionContext.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)//
                    .getOrComputeIfAbsent(java.util.Random.class);
            if (int.class.equals(type) || Integer.class.equals(type)) {
                return random.nextInt();
            }
            if (double.class.equals(type)) {
                return random.nextDouble();
            }
            throw new ParameterResolutionException("No random generator implemented for " + type);
        }
    }

    /**
     * TypeBasedParameterResolver： 一个根据参数类型 注入的 参数解析器
     */
    public static class DateResolve extends TypeBasedParameterResolver<Date> {

        @Override
        public Date resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return new Date();
        }
    }

}
