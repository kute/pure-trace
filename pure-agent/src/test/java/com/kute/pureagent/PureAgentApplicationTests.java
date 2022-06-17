package com.kute.pureagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
public class PureAgentApplicationTests {

    public static void main(String[] args) {
        System.out.println("test main");
        SpringApplication.run(PureAgentApplicationTests.class, args);
    }

    @RestController
    public class ApiController {
        @PostMapping("/ping")
        public String ping(HttpServletRequest request) {
            return "pong";
        }
    }

}
