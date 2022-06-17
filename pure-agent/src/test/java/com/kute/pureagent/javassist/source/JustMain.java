package com.kute.pureagent.javassist.source;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * created by kute at 2022/5/7 上午10:06
 */
@Slf4j
public class JustMain {

    public static void main(String[] args) throws NoSuchFieldException {
        log.info("JustMain main args={}", Arrays.toString(args));
        Field field = String.class.getField("newField");
        log.info("JustMain main string newField={}", field.getName());
    }

    public static void print() {
        log.info("====print======");
    }
}
