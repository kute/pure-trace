package com.kute.pureagent.javassist.source;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyService {

    public StartEvent generateStartEvent(Integer x, IEvent event) {
        log.info("generateStartEvent");
        return new StartEvent();
    }

}
