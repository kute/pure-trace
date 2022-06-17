package com.kute.pureagent.javassist.source;

import net.bytebuddy.utility.RandomString;

/**
 * created by kute at 2022/5/6 上午11:10
 */
public interface IEvent {

    default String id() {
        return RandomString.make(10);
    }

}
