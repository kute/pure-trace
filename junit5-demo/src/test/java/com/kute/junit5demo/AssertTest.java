package com.kute.junit5demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * created by kute at 2022/4/22 下午3:17
 */
public class AssertTest {

    @TestOnMac
    public void test() {

        assertEquals(2, 2, "error");
        assertArrayEquals(new byte[0], new byte[0]);

        assertTrue(2 > 1);
        assertTrue(2 > 1, "error");
        assertTrue(2 > 1, () -> "error");

        // batch
        assertAll("test",
                () -> assertTrue(2 > 1),
                () -> assertNotEquals(1, 2));

        Exception exception = assertThrows(ArithmeticException.class, () -> this.divide(1, 0));
        assertInstanceOf(ArithmeticException.class, exception);

        // 超时断言
        String defaultValue = assertTimeout(Duration.ofSeconds(2), () -> {
            return "timeout default value";
        });
        assertEquals(defaultValue, "timeout default value");
    }

    public int divide(int a, int b) {
        return a / b;
    }

}
