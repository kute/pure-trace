package com.kute.agent.async_init_bean.service;

import com.kute.asyncinitbean.AsyncInitBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by kute on 2023/6/8 18:55
 */
@Configuration
public class InitMyBeanConfig {

    @AsyncInitBean
    @Bean(name = "maxBean", initMethod = "init")
    public MaxBean maxBean() throws InterruptedException {
        return new MaxBean();
    }

    @AsyncInitBean
    @Bean(name = "kuteBean", initMethod = "init")
    public KuteBean kuteBean() throws InterruptedException {
        return new KuteBean();
    }
}
