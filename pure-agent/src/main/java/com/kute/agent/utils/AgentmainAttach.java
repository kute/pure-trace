package com.kute.agent.utils;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

/**
 * 测试agentmain
 * <p>
 * 首先启动 PureAgentApplication，然后通过 VirtualMachine 运行时attach jar到目标进程
 */
public class AgentmainAttach {

    public static void main(String[] args) throws AgentLoadException, IOException, AttachNotSupportedException, AgentInitializationException, InterruptedException {
        attach("PureAgentApplication");
    }

    public static void attach(String keyword) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException, InterruptedException {
        String pid = getPureAgentApplicationPID();
//        VirtualMachine vm = VirtualMachine.attach("7997");//7997是待绑定的jvm进程的pid号
//        vm.loadAgent("target/myAgent-1.0-SNAPSHOT.jar", "com.binecy.MyAgentTest&hello");
        Process process = Runtime.getRuntime().exec(new String[]{"ps aux"});

//        BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
        InputStream stderr = process.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stderr);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        System.out.println("<error></error>");
        while ((line = br.readLine()) != null)
            System.out.println(line);
        System.out.println("");
        int exitVal = process.waitFor();
        System.out.println("Process exitValue: " + exitVal);

    }

    private static String getPureAgentApplicationPID() {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        return null;
    }

    /**
     * 获取clazz的绝对路径
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    public static URI getJarURI(Class<?> clazz) throws Exception {
        /**
         * 如果直接执行.class文件那么会得到当前class的绝对路径。
         *
         * 如果封装在jar包里面执行jar包那么会得到当前jar包的绝对路径
         */
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        if (null != url) {
            return url.toURI();
        }

        String resourcePath = "/some file.txt";
        url = clazz.getResource(resourcePath);
        if (null == url) {
            throw new Exception("Can not locate resource file.");
        }

        String path = url.getPath();
        if (!path.endsWith("!" + resourcePath)) {
            throw new Exception("Invalid resource path.");
        }

        path = path.substring(0, path.length() - resourcePath.length() - 1);

        return new URI(path);

    }

}
