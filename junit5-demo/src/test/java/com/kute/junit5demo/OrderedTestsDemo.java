package com.kute.junit5demo;

import org.junit.jupiter.api.*;

import java.util.Comparator;

/**
 * 单测顺序
 * 1、MethodOrderer.OrderAnnotation：根据 @Order注解顺序
 * 2、MethodOrderer.DisplayName:
 * 3、Random
 */
//@TestClassOrder(value = ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(value = OrderedTestsDemo.MyMethodOrder.class)
//@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
public class OrderedTestsDemo {

    @Test
    @Order(1)
        // 数字越大，优先级越高
    void nullValues() {
        // perform assertions against null values
    }

    @Test
    @Order(2)
    void emptyValues() {
        // perform assertions against empty values
    }

    @Test
    @Order(3)
    void validValues() {
        // perform assertions against valid values
    }

    // 自定义排序
    public static class MyMethodOrder implements MethodOrderer {
        @Override
        public void orderMethods(MethodOrdererContext context) {
            context.getMethodDescriptors().sort(Comparator.comparing(MethodDescriptor::getDisplayName));
        }
    }

}