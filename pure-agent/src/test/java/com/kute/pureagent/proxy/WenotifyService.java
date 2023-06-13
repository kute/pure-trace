package com.kute.pureagent.proxy;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by kute on 2023/6/8 16:29
 */
@Slf4j
public class WenotifyService {


    public Object notify(String message) {
        log.info("WenotifyService notify for message={}", message);
        return "ok";
    }

    public Object wechat(String a, String b) {
        log.info("WenotifyService wechat for a={}, a={}", a, b);
        return a + b;
    }

    public void throwException() {
        throw new RuntimeException("some exception");
    }

}
