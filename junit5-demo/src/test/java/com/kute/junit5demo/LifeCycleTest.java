package com.kute.junit5demo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.extension.ExtensionRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * created by kute at 2022/4/26 上午10:00
 * <p>
 * 单测生命周期测试
 * <p>
 * 1、BeforeTestExecutionCallback和AfterTestExecutionCallback定义了扩展的api，
 * 希望添加在测试方法执行之前和之后立即执行的行为，因此，这些回调非常适合于计时、跟踪和类似的用例。
 * <p>
 * 2、如果您需要实现围绕@BeforeEach和@AfterEach方法调用的回调，那么应该实现BeforeEachCallback和AfterEachCallback。
 */
@ExtendWith(value = {LifeCycleTest.CallBackExtension.class})
public class LifeCycleTest {

    @BeforeAll
    public static void BeforeAll() {
        System.out.println("@BeforeAll");
    }

    @BeforeEach
    public void BeforeEach() {
        System.out.println("@BeforeEach");
    }

    @AfterAll
    public static void AfterAll() {
        System.out.println("@AfterAll");
    }

    @AfterEach
    public void AfterEach() {
        System.out.println("@AfterEach");
    }

    @Test
    public void test() {
        System.out.println("test");
    }

    /**
     * BeforeEachCallback 针对 @BeforeEach注解的回调
     * BeforeTestExecutionCallback: 针对在真正的方法执行前添加自定义逻辑
     * BeforeAllCallback：针对 @BeforeAll
     * AfterEachCallback：针对 @AfterEach
     * AfterTestExecutionCallback：针对在真正的方法执行后添加自定义逻辑
     * AfterAllCallback：针对 @AfterAll
     * InvocationInterceptor: 用于拦截 callback的
     */
    public static class CallBackExtension implements
            BeforeEachCallback, BeforeAllCallback,
            AfterEachCallback, AfterAllCallback,
            BeforeTestExecutionCallback, AfterTestExecutionCallback,
            BeforeEachMethodAdapter, AfterEachMethodAdapter,
            InvocationInterceptor {

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            System.out.println("afterAll callback");
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {
            System.out.println("afterEach callback");
        }

        @Override
        public void beforeAll(ExtensionContext context) throws Exception {
            System.out.println("beforeAll callback");
        }

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            System.out.println("beforeEach callback");
        }

        @Override
        public void invokeAfterEachMethod(ExtensionContext context, ExtensionRegistry registry) throws Throwable {
            System.out.println("invokeAfterEachMethod");
        }

        @Override
        public void invokeBeforeEachMethod(ExtensionContext context, ExtensionRegistry registry) throws Throwable {
            System.out.println("invokeBeforeEachMethod");
        }

        @Override
        public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptTestClassConstructor");
            return invocation.proceed();
        }

        @Override
        public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptBeforeAllMethod");
            invocation.proceed();
        }

        @Override
        public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptBeforeEachMethod");
            invocation.proceed();
        }

        @Override
        public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptTestMethod");
            invocation.proceed();
        }

        @Override
        public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptTestFactoryMethod");
            return invocation.proceed();
        }

        @Override
        public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptTestTemplateMethod");
            invocation.proceed();
        }

        @Override
        public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptDynamicTest");
            interceptDynamicTest(invocation, extensionContext);
        }

        @Override
        public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptAfterEachMethod");
            invocation.proceed();
        }

        @Override
        public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
            System.out.println("interceptAfterAllMethod");
            invocation.proceed();
        }

        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            System.out.println("afterTestExecution");
        }

        @Override
        public void beforeTestExecution(ExtensionContext context) throws Exception {
            System.out.println("beforeTestExecution");
        }
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
    }

}
