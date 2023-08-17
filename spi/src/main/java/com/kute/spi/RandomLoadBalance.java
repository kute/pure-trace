package com.kute.spi;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;

/**
 * Created by kute on 2023/8/16 17:14
 */
@Activate(group = {"b"}, order = 0)
@Slf4j
public class RandomLoadBalance implements LoadBalance {

    public RandomLoadBalance() {
        log.info("RandomLoadBalance construct");
    }

    @Override
    public void print() {
        log.info("RandomLoadBalance print");
    }

    @Override
    public void log(URL url) {
        log.info("RandomLoadBalance log");
    }
}
