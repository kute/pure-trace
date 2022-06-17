package com.kute.agent.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.ClassFile;
import javassist.util.HotSwapAgent;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;

@Slf4j
public abstract class HotSwapUtils {

    /**
     * 热更新java文件
     * JVM的启动参数，jdk11过后默认JVM不允许连接自己，需要加上JVM启动参数: -Djdk.attach.allowAttachSelf=true
     * <p>
     * 优先使用简单的Javassist做热更新，因为Byte Buddy使用了更为复杂的ASM，spring boot web项目中会优先使用Byte Buddy热更新
     *
     * @param bytes .class结尾的字节码文件
     */
    public static synchronized void hotswapClass(byte[] bytes) {

        var clazzName = readClassName(bytes);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            log.info("无法在当前项目找到[class:{}]，所以忽略本次热更新", clazzName);
            return;
        }

        hotswapClassByJavassist(clazz, bytes);
    }

    private static String readClassName(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        DataInputStream dataInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            dataInputStream = new DataInputStream(byteArrayInputStream);
            var classFile = new ClassFile(dataInputStream);
            return classFile.getName().replaceAll("/", ".");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(dataInputStream, byteArrayInputStream);
        }
    }

    //使用 javassist.util.HotSwapAgent 重定义类。这种方式必须 attach 代理程序才能使用：-XXaltjvm=dcevm -javaagent:E:\hotswap-agent-1.3.0.jar
    //HotSwapAgent.redefine(HelloServiceImpl.class, targetClass);
    private static void hotswapClassByJavassist(Class<?> clazz, byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = null;
        CtClass ctClass = null;
        try {
            clazz = Class.forName(readClassName(bytes));
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            ctClass = ClassPool.getDefault().makeClass(byteArrayInputStream);
            // Javassist热更新
            HotSwapAgent.redefine(clazz, ctClass);
            log.info("Javassist热更新[{}]成功", clazz);
        } catch (Throwable t) {
            log.info("无法使用Javassist热更新，开始使用替补方案Byte Buddy做热更新", t);
            hotswapClassByByteBuddy(clazz, bytes);
        } finally {
            IOUtils.closeQuietly(byteArrayInputStream);
            if (ctClass != null) {
                ctClass.defrost();
            }
        }
    }

    private static void hotswapClassByByteBuddy(Class<?> clazz, byte[] bytes) {
        try {
            // Byte Buddy热更新
            var instrumentation = ByteBuddyAgent.install();
            instrumentation.redefineClasses(new ClassDefinition(clazz, bytes));
            log.info("Byte Buddy热更新[{}]成功", clazz);
        } catch (Throwable t) {
            log.error("Byte Buddy热更新未知异常，热更新[{}]失败", clazz);
        }
    }

}
