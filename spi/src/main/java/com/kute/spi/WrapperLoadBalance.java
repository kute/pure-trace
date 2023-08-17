package com.kute.spi;

import lombok.extern.slf4j.Slf4j;

import org.apache.dubbo.common.URL;

/**
 * Created by kute on 2023/8/16 17:58
 */
@Slf4j
public class WrapperLoadBalance implements LoadBalance{

    private final LoadBalance loadBalance;

    public WrapperLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void print() {
        log.info("WrapperLoadBalance log");
        loadBalance.print();
    }

    @Override
    public void log(URL url) {
        log.info("WrapperLoadBalance log");
        loadBalance.log(url);
    }
}
