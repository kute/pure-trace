package com.kute.pureagent.bytebuddy;

import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

/**
 * created by kute at 2022/4/17 上午11:02
 */
public class Target {

    /**
     * @return
     */
    public static String intercept() {
        return "one";
    }

    // 禁用严格类型检查
    @RuntimeType
    public static String intercept(@Argument(0) /* 绑定第一个参数*/ String name) {
        return "two " + name;
    }

    public static String intercept(Integer name) {
        return "three " + name;
    }

    public static String intercept(Object name) {
        return "four " + name;
    }

    public static String slash(Object name) {
        return "five " + name;
    }

    /**
     * 这里虽然接收参数是按照 name 为第一个参数，但是绑定 确实age为第一个，所以第一个参数值会先绑定到age上
     * 见：test6()
     *
     * @param name
     * @param age
     *
     * @return
     */
    public static String intercept(@Argument(1) String name, @Argument(0) String age) {
        return "six " + name + age;
    }

}
