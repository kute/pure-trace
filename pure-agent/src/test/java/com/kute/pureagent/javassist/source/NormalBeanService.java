package com.kute.pureagent.javassist.source;

import lombok.extern.slf4j.Slf4j;

/**
 * created by kute at 2022/5/6 下午4:08
 */
@Slf4j
public class NormalBeanService {

    public void dealEvent(StartEvent startEvent) {
        log.info("NormalBeanService receive event={}", startEvent);
    }

}
