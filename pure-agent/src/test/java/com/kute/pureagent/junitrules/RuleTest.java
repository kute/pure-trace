package com.kute.pureagent.junitrules;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

/**
 * created by kute at 2022/4/21 下午4:16
 * <p>
 * junit rule 类似 @Before @After，都是用来增强test的，junit 内置了很多rule
 * org.junit.rules
 */
public class RuleTest {

    // 用于创建临时文件目录的，单测结束后会自动删除，可指定父目录
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    // 用于指定当前类的所有单测方法的执行超时时间，超过时间则认定为失败
    @Rule
    public Timeout timeout = new Timeout(1000, TimeUnit.MILLISECONDS);

    // 耗时统计
    @Rule
    public Stopwatch stopwatch = new Stopwatch();

    @Rule
    public MyRule myRule = new MyRule();

    @BeforeClass
    public static void beforeClass() {
        System.out.println("beforeClass");
    }

    @Before
    public void before() {
        System.out.println("before");
    }

    @Test
    public void test() throws InterruptedException {
        System.out.println("done");
        TimeUnit.MILLISECONDS.sleep(540L);
        System.out.println("method execute takes=" + stopwatch.runtime(TimeUnit.MILLISECONDS));
    }

}
