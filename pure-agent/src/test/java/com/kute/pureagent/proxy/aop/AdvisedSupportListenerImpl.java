package com.kute.pureagent.proxy.aop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AdvisedSupportListener;

/**
 * Created by kute on 2023/6/12 11:47
 */
@Slf4j
public class AdvisedSupportListenerImpl implements AdvisedSupportListener {

    @Override
    public void activated(AdvisedSupport advised) {
        log.info("AdvisedSupportListener activated for {}", advised.getTargetSource());
    }

    @Override
    public void adviceChanged(AdvisedSupport advised) {
        log.info("AdvisedSupportListener adviceChanged for {}", advised.getTargetSource());
    }
}
