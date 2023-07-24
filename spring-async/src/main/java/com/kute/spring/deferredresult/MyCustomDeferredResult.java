package com.kute.spring.deferredresult;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Created by kute on 2023/7/24 12:07
 * 通过继承 DeferredResult 可以携带额外的数据
 */
@Data
@Accessors(chain = true)
public class MyCustomDeferredResult extends DeferredResult<MyInfo> {

    public MyCustomDeferredResult(Long timeoutValue) {
        super(timeoutValue);
    }

    public MyCustomDeferredResult(Long timeoutValue, Object timeoutResult) {
        super(timeoutValue, timeoutResult);
    }

    private Integer index;
    private MyInfo myInfo;

}
