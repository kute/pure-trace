package com.kute.agent.async_init_bean.service;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by kute on 2023/6/5 09:52
 */
@Slf4j
public class MaxBean {

    public void init() throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        log.info("---------init maxbean");
    }

}
