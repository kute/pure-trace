package com.kute.spi;

import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;

/**
 * Created by kute on 2023/8/16 17:14
 */
@Activate(group = {"a", "b"}, value = {"x"}, order = 1)
@Slf4j
public class HashLoadBalance implements LoadBalance {

    public HashLoadBalance() {
        log.info("HashLoadBalance construct");
    }

    @Override
    public void print() {
        log.info("HashLoadBalance print");
    }

    @Override
    public void log(URL url) {
        log.info("HashLoadBalance log");
    }

}
