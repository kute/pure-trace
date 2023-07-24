package com.kute.spring.callable;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by kute on 2023/7/24 16:55
 */
@RestController
@RequestMapping("/api/v1/async")
@Slf4j
public class CallableController {

    /**
     * 控制器先返回一个Callable对象
     * Spring MVC开始进行异步处理，并把该Callable对象提交给另一个独立线程的执行器TaskExecutor处理
     * DispatcherServlet和所有过滤器都退出Servlet容器线程，但此时方法的响应对象仍未返回
     * Callable对象最终产生一个返回结果，此时Spring MVC会重新把请求分派回Servlet容器，恢复处理
     * DispatcherServlet再次被调用，恢复对Callable异步处理所返回结果的处理
     * <p>
     * 不占用请求线程，用额外的独立线程执行逻辑
     * <p>
     * 但是无超时 和 异常处理，可以使用 WebAsyncTask 替代
     *
     * @return
     */
    @GetMapping("/callable")
    public Callable<String> callable() {
        System.out.println(LocalDateTime.now().toString() + "--->主线程开始");
        Callable<String> callable = () -> {
            String result = "return callable";
            // 执行业务耗时 5s
            Thread.sleep(5000);
            System.out.println(LocalDateTime.now().toString() + "--->子任务线程(" + Thread.currentThread().getName() + ")");
            return result;
        };
        System.out.println(LocalDateTime.now().toString() + "--->主线程结束");
        return callable;
    }

    /**
     * 不占用请求线程，用额外的独立线程执行逻辑
     *
     * @return
     */
    @GetMapping("completableFuture")
    public CompletableFuture<String> completableFuture() {
        // 线程池一般不会放在这里，会使用static声明，这只是演示
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println(LocalDateTime.now().toString() + "--->主线程开始");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "ok";
        }, executor);
        System.out.println(LocalDateTime.now().toString() + "--->主线程结束");
        return completableFuture;
    }

    /**
     * 不占用请求线程，用额外的独立线程执行逻辑
     * <p>
     * ListenableFuture 已过时，用 CompletableFuture 替代
     *
     * @return
     */
    @GetMapping("listenableFuture")
    public ListenableFuture<String> listenableFuture() {
        // 线程池一般不会放在这里，会使用static声明，这只是演示
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println(LocalDateTime.now().toString() + "--->主线程开始");
        ListenableFutureTask<String> listenableFuture = new ListenableFutureTask<>(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "ok";
        });
        executor.execute(listenableFuture);
        System.out.println(LocalDateTime.now().toString() + "--->主线程结束");
        return listenableFuture;
    }

    /**
     * WebAsyncTask 对 callable 和 executor 的封装
     *
     * @return
     */
    @GetMapping("asynctask")
    public WebAsyncTask<String> asyncTask(HttpServletRequest request) {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        System.out.println(LocalDateTime.now().toString() + "--->主线程开始");
        WebAsyncTask<String> task = new WebAsyncTask<>(Long.parseLong(request.getParameter("timeout")), executor, () -> {
            try {
                Thread.sleep(Long.parseLong(request.getParameter("sleep")));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "ok";
        });
        task.onCompletion(() -> {
            System.out.println(LocalDateTime.now().toString() + "--->调用完成");
        });
        task.onTimeout(() -> {
            System.out.println("onTimeout");
            return "onTimeout";
        });
        System.out.println(LocalDateTime.now().toString() + "--->主线程结束");
        return task;
    }


}
