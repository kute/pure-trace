package com.kute.junit5demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * created by kute at 2022/4/22 下午2:10
 */
@Disabled(value = "这是是禁用原因")
public class EnabledDisableTest {

    @Disabled
    @Test
    public void test() {
        System.out.println("@Disabled 标记当前类 或者 方法 不执行单测");
    }

    //    @EnabledOnOs(value = {OS.MAC, OS.LINUX})
    @DisabledOnOs(value = {OS.MAC, OS.LINUX})
    @Test
    public void test1() {
        System.out.println("根据操作系统类型决定是否禁用");
    }

    //    @EnabledOnOs(value = {JRE.JAVA_8, JRE.JAVA_9})
    @DisabledOnJre(value = {JRE.JAVA_8, JRE.JAVA_9})
    @Test
    public void test2() {
        System.out.println("根据JDK版本决定是否禁用");
    }

    @TestOnMac
    public void test3() {
        System.out.println("自定义组合注解");
    }

    @Test
//    @EnabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_18, disabledReason = "原因")
    @DisabledForJreRange(min = JRE.JAVA_8, max = JRE.JAVA_18, disabledReason = "原因")
    public void test4() {
        System.out.println("范围禁用");
    }

    //    @EnabledIfSystemProperty(named = "system.path", matches = "ss", disabledReason = "")
    @DisabledIfSystemProperty(named = "system.path", matches = "ss", disabledReason = "")
    public void test5() {
        System.out.println("匹配系统属性决定是否禁用");
    }

    @EnabledIfSystemProperties({
            @EnabledIfSystemProperty(named = "system.path", matches = "ss", disabledReason = ""),
            @EnabledIfSystemProperty(named = "system.path2", matches = "ss", disabledReason = "")
    })
    @DisabledIfSystemProperties({
            @DisabledIfSystemProperty(named = "system.path", matches = "ss", disabledReason = ""),
            @DisabledIfSystemProperty(named = "system.path2", matches = "ss", disabledReason = "")
    })
    public void test6() {
        System.out.println("批量匹配系统属性决定是否禁用");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server", disabledReason = "reason")
    @DisabledIfEnvironmentVariable(named = "ENV", matches = "staging-server", disabledReason = "reason")
    public void test7() {
        System.out.println("环境变量");
    }

    @Test
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server", disabledReason = "reason"),
            @EnabledIfEnvironmentVariable(named = "ENV", matches = "staging-server", disabledReason = "reason")
    })
    @DisabledIfEnvironmentVariables({
            @DisabledIfEnvironmentVariable(named = "ENV", matches = "staging-server", disabledReason = "reason"),
            @DisabledIfEnvironmentVariable(named = "ENV", matches = "staging-server", disabledReason = "reason")
    })
    public void test8() {
        System.out.println("批量环境变量");
    }

    @Test
//    @EnabledIf(value = "myEvaluateMethod", disabledReason = "方法返回 true表示启用")
//    @DisabledIf(value = "myEvaluateMethod", disabledReason = "方法返回 true表示禁用")
    @DisabledIf(value = "com.kute.junit5demo.EnabledDisableTest#myEvaluateMethod2", disabledReason = "调用外部类的静态方法评估")
    public void test9() {
        System.out.println("自定义方法决定禁用");
    }

    public static boolean myEvaluateMethod2(ExtensionContext extensionContext) {
        System.out.println("评估方法可以不带参，或者带一个 ExtensionContext 参数=" + extensionContext);
        return false;
    }

    public boolean myEvaluateMethod(ExtensionContext extensionContext) {
        System.out.println("评估方法可以不带参，或者带一个 ExtensionContext 参数=" + extensionContext);
        return false;
    }

}
