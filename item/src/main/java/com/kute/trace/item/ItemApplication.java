package com.kute.trace.item;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * -javaagent:/Users/kute/app/apache-skywalking-apm-bin-es7/agent/skywalking-agent.jar
 * -Dskywalking_config=/Users/kute/app/apache-skywalking-apm-bin-es7/agent/config/agent.config
 * -Dskywalking.collector.backend_service=10.36.202.18:11800
 * -Dskywalking.agent.service_name=pure-item-app
 */
@MapperScan(basePackages = "com.kute.trace.item.mapper")
@SpringBootApplication
public class ItemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItemApplication.class, args);
    }

}
