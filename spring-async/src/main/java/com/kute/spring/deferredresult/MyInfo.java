package com.kute.spring.deferredresult;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Created by kute on 2023/7/24 12:07
 */
@Data
@Accessors(chain = true)
public class MyInfo implements Serializable {

    /**
     * -1:超时
     * 0: 异常
     * 1：成功
     */
    private Integer code;
    private String message;
    private Object data;

    public static MyInfo defaultTimeoutResult() {
        return new MyInfo()
                .setCode(-1)
                .setMessage("timeout message");
    }

    public static MyInfo defaultExceptionResult() {
        return new MyInfo()
                .setCode(0)
                .setMessage("exception message");
    }

    public static MyInfo defaultSuccessResult() {
        return new MyInfo()
                .setCode(1)
                .setMessage("success message");
    }

}
