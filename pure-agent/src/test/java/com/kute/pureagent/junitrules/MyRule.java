package com.kute.pureagent.junitrules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * created by kute at 2022/4/21 下午4:41
 */
public class MyRule implements TestRule {
    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                System.out.println("单测执行前：" + description.getClassName() + "." + description.getMethodName());
                base.evaluate();
                System.out.println("单测执行后：" + description.getClassName() + "." + description.getMethodName());
            }
        };
    }
}
