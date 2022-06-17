package com.kute.agent.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ApiController {
    @GetMapping("/ping")
    public String ping(HttpServletRequest request) {
        return "pong";
    }
}