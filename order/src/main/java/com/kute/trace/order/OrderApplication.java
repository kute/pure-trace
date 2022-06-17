package com.kute.trace.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * -javaagent:/Users/kute/app/apache-skywalking-apm-bin-es7/agent/skywalking-agent.jar
 * -Dskywalking_config=/Users/kute/app/apache-skywalking-apm-bin-es7/agent/config/agent.config
 * -Dskywalking.collector.backend_service=10.36.202.18:11800
 * -Dskywalking.agent.service_name=pure-order-app
 */
@MapperScan(basePackages = "com.kute.trace.order.mapper")
@SpringBootApplication
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
