package com.kute.junit5demo;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * created by kute at 2022/4/22 下午12:16
 */
@ExtendWith(value = {TimingExtension.class, IgnoreIOExceptionExtension.class})
@DisplayName("HelloWorld JUnit 5")
@Timeout(value = 2)
public class HelloWorld {

    @BeforeAll
    public static void beforeAll() {
        System.out.println("所有单测执行前调用，只执行一次，方法需要为static");
    }

    @AfterAll
    public static void afterAll() {
        System.out.println("afterAll");
    }

    @BeforeEach
    public void beforeEach() {
        System.out.println("每个单测执行前调用");
    }

    @AfterEach
    public void afterEach() {
        System.out.println("每个单测执行前调用");
    }

    @DisplayName(value = "单测单测") // 通过DisplayName声明定制的单测名称，也可通过 DisplayNameGenerator 自定义生成策略
    @Timeout(value = 2) // 期望单测在2s执行完成
    @Test
    public void test() {
        System.out.println("hello");
    }

    /**
     * 而 @RepeatedTest 注解是通过  RepeatedTestExtension 解析的
     *
     * @param repetitionInfo 是通过 RepetitionInfoParameterResolver 解析注入的
     */
    @RepeatedTest(value = 4) // 重复执行 4次
    public void repeatTest(RepetitionInfo repetitionInfo) {
        System.out.println("hello repeat for repetitionInfo=" + repetitionInfo);
    }

    @Test
    void failingTest() {
        // 快速失败单测
//        fail("a failing test");
    }

}
