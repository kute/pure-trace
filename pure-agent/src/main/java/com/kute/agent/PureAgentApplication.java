package com.kute.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 首先先执行打包，生成agent jar
 * 启动时在vm options里添加如下参数来测试premain和agentmain，相关的配置在pom中定义：
 * -javaagent:/Users/kute/work/ideawork/pure-trace/pure-agent/target/pure-agent-0.0.1-SNAPSHOT.jar=a=b2
 * 访问接口时会记录请求日志，见：com.kute.agent.main.PureAgentMain
 */
@SpringBootApplication(scanBasePackages = {"com"})
public class PureAgentApplication {

    public static void main(String[] args) {
        System.out.println("test main");
        SpringApplication.run(PureAgentApplication.class, args);
    }

}
