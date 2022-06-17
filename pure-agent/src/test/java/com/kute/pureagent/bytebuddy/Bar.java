package com.kute.pureagent.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.BindingPriority;

public class Bar {

    /**
     * 当 代理到Bar类中的方法有多个方法签名或者返回类型一致时，可通过 BindingPriority 来声明优先级
     * 数字越大，方法被匹配到的优先级越高
     *
     * @return
     */
    @BindingPriority(3)
    public static String sayHelloBar() {
        return "sayHelloBar";
    }

    @BindingPriority(2)
    public static String sayBar() {
        return "bar";
    }
}