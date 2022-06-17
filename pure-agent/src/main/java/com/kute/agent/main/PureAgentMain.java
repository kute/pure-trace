package com.kute.agent.main;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * java agent本质上可以理解为一个插件，该插件就是一个精心提供的jar包，
 * 这个jar包通过JVMTI（JVM Tool Interface）完成加载，
 * 最终借助JPLISAgent（Java Programming Language Instrumentation Services Agent）完成对目标代码的修改
 * <p>
 * 在pom.xml里build插件中声明
 * 然后执行打包
 * 在ManiFest属性中指定“Premain-Class”或者“Agent-Class”,且需根据需求定义Can-Redefine-Classes和Can-Retransform-Classes，或者使用pom里的插件完成
 * 然后在测试类的vm option中加入agent:  -javaagent:/Users/kute/work/ideawork/pure-trace/pure-agent/target/pure-agent-0.0.1-SNAPSHOT.jar=a=b&c=d
 * <p>
 * <p>
 * JAVA两大字节码工具：Byte Buddy 和 Javassist
 */
public class PureAgentMain {

    /**
     * 在jvm启动时加载
     *
     * @param agentArgs
     */
    public static void premain(String agentArgs) {
        System.out.println("premain agentArgs=" + agentArgs);
    }

    /**
     * 当Java 虚拟机启动时，在执行 main 函数之前，JVM 会先运行 -javaagent 所指定 jar 包内 Premain-Class 这个类的 premain 方法
     *
     * @param agentArgs       第一个参数AgentArgs是随同 “–javaagent”一起传入的程序参数，如果这个字符串代表了多个参数，就需要自己解析这些参数, 如 -javaagent: /agent.jar=a=b&c=d
     * @param instrumentation inst是Instrumentation类型的对象，是在指定了-javaagent后JVM自动传入的，我们可以拿这个参数进行类增强等操作
     */
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        System.out.println("premain agentArgs=" + agentArgs);
        System.out.println("agent param:" + agentArgs);

        // 如下是使用byte buddy通过添加transform拦截springmvc的请求分发
        new AgentBuilder.Default()
                // 拦截 DispatcherServlet
                .type(ElementMatchers.named("org.springframework.web.servlet.DispatcherServlet"))
                .transform((builder, type, classLoader, module) ->
                        builder.method(ElementMatchers.named("doDispatch"))
                                .intercept(MethodDelegation.to(DoDispatchInterceptor.class)))
                .with(new DoDispatchListener())
                .installOn(instrumentation);

    }

    /**
     * Agentmain是JDK SE6的新特性，可以在java程序运行时，通过VirtualMachine 将jar包绑定到jvm进程中，动态地改变类的行为
     * <p>
     * try {
     * String jvmPid = 目标进行的pid;
     * logger.info("Attaching to target JVM with PID: " + jvmPid);
     * VirtualMachine jvm = VirtualMachine.attach(jvmPid);
     * jvm.loadAgent(agentFilePath);//agentFilePath为agent的路径
     * jvm.detach();
     * logger.info("Attached to target JVM and loaded Java agent successfully");
     * } catch (Exception e) {
     * throw new RuntimeException(e);
     * }
     *
     * @param agentArgs
     * @param instrumentation
     */
    public static void agentmain(String agentArgs, Instrumentation instrumentation) {

    }

    /**
     * jvm运行时加载
     *
     * @param agentArgs
     */
    public static void agentmain(String agentArgs) {

    }

}
