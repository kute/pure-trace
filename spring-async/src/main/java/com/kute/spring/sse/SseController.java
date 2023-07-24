package com.kute.spring.sse;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by kute on 2023/7/24 14:58
 * https://juejin.cn/post/7224060318652153913
 */
@RestController
@RequestMapping("/api/v1/async")
@Slf4j
public class SseController {

    @Resource
    private SseService sseService;

    @GetMapping(value = "/test/{clientId}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter test(@PathVariable("clientId") String clientId) {
        final SseEmitter emitter = sseService.getConn(clientId);

        // 模拟服务端的多次数据推送
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                    sseService.send(clientId, i);
                } catch (Exception e) {
                    throw new RuntimeException("推送数据异常");
                }
            }
            log.info("服务端完成的所有的数据推送，准备关闭此次订阅推送");
            // 结束
            sseService.closeConn(clientId);
        });

        return emitter;
    }

    /**
     * 当客户端不需要再接收 服务端的推送时 调用此接口
     *
     * @param clientId
     * @return
     */
    @GetMapping("closeConn/{clientId}")
    public String closeConn(@PathVariable("clientId") String clientId) {
        sseService.closeConn(clientId);
        return "连接已关闭";
    }
}
