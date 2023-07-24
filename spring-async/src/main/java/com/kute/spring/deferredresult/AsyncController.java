package com.kute.spring.deferredresult;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.vavr.control.Try;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.DeferredResultMethodReturnValueHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by kute on 2023/7/24 11:06
 * client 间隔时间循环 发起 长轮询请求
 * server 端 返回 deferredResult 对象：对象设置超时时间，以及 超时返回的数据，并在服务端保存改对象
 * 待 某一关联任务处理完成后，从 容器取出 deferredResult 并设置结果
 */
@RestController
@RequestMapping("/api/v1/async")
@Slf4j
public class AsyncController {

    @Autowired
    private RestTemplate restTemplate;

    private static ExecutorService executors = Executors.newFixedThreadPool(10);

    /**
     * 服务端持有的 deferred 连接对象
     * 允许一对多
     */
    private final static Multimap<String, MyCustomDeferredResult> deferredResults =
            Multimaps.newListMultimap(Maps.newConcurrentMap(), ArrayList::new);

    /**
     * DeferredResult 的处理器：{@link DeferredResultMethodReturnValueHandler}
     *
     * @return
     */
    @GetMapping("/deferredResultServer")
    public MyCustomDeferredResult server(HttpServletRequest request) {
        // 构造 超时时间 以及 默认超时返回的结果
        Long timeout = getParam(request, "timeout", Long::parseLong, 1L);
        MyCustomDeferredResult deferredResult = new MyCustomDeferredResult(timeout * 1000);

        String project = getParam(request, "project", Function.identity(), "my-app");

        // 设置 监听
        // 当在超时时间内未 设置 result 则执行此逻辑
        deferredResult.onTimeout(() -> {
            log.info("deferredResultTest onTimeout occur");
            deferredResults.get(project).forEach(myCustomDeferredResult -> {
                myCustomDeferredResult.setResult(MyInfo.defaultTimeoutResult());
            });
        });
        // 当 在超时时间内 设置了 result，表示成功，则从 deferredResults 中移出持有
        deferredResult.onCompletion(() -> {
            log.info("deferredResultTest onCompletion occur");
            deferredResults.removeAll(project);
        });

        Long fakeTakes = getParam(request, "fakeTakes", Long::parseLong, 3L);
        Preconditions.checkNotNull(fakeTakes, "Param fakeTakes is null");
        // 异步处理，模拟耗时
        executors.submit(() -> {
            log.info("begin sleep {} ms", fakeTakes);
            Try.run(() -> TimeUnit.SECONDS.sleep(fakeTakes));
            log.info("done sleep {} ms", fakeTakes);
            deferredResults.get(project).forEach(myCustomDeferredResult -> {
                log.info("----x");
                myCustomDeferredResult.setResult(MyInfo.defaultSuccessResult());
            });
        });

        // 将 deferredResult 放入容器
        deferredResults.put(project, deferredResult);
        log.info("==========持有对象个数：my-app={}", deferredResults.get("my-app").size());
        return deferredResult;
    }

    @GetMapping("/deferredResultClient")
    public Object client(@RequestParam(required = false, defaultValue = "1") Long timeout,
                         @RequestParam(required = false, defaultValue = "3") Long fakeTakes,
                         @RequestParam(required = false, defaultValue = "1") Integer loopTimes) {
        String url = "http://localhost:8080/api/v1/async/deferredResultServer?";
        Map<String, Object> params = new HashMap<>(8);
        params.put("project", "my-app");
        params.put("timeout", timeout);
        params.put("fakeTakes", fakeTakes);
        String uriparams = params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        // 间隔时间 多次发起轮训
        for (int i = 0; i < loopTimes; i++) {
            int finalI = i;
            Try.run(() -> TimeUnit.SECONDS.sleep(1L));

            executors.submit(() -> {
                log.info("deferredResultClient-{} begin for param={}", finalI, params);
                ResponseEntity<MyInfo> response = restTemplate.getForEntity(url + uriparams, MyInfo.class);
                MyInfo myInfo = response.getBody();
                log.info("deferredResultClient-{} myinfo={}", finalI, myInfo);
            });
        }
        return "ok";
    }

    private <T> T getParam(HttpServletRequest request, String key, Function<String, T> mapping, T defaultV) {
        String v = request.getParameter(key);
        if (Strings.isNullOrEmpty(v)) {
            return defaultV;
        }
        return mapping.apply(v);
    }

}
