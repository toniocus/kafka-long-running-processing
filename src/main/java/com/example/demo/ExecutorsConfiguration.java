package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ExecutorsConfiguration.class);

    @Value("${ta.conciliador.request.thread.pool.timeout.seconds:45}")
    private int poolQueueTimeout;

    @Value("${ta.conciliador.request.thread.pool.size:1}")
    private int poolSize;

    @Bean("conciliadorRequestThreadPool")
    public ThreadPoolTaskExecutor conciliatorRequestThreadPool() {

        log.info("Creating kafka consumer pool with {} threads and blocking queue timeout of {} seconds"
                , this.poolSize
                , this.poolQueueTimeout
                );

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(this.poolSize);
        executor.setCorePoolSize(this.poolSize);
        executor.setQueueCapacity(this.poolSize);
        executor.setThreadNamePrefix("ta-conciliador-request");
        executor.setRejectedExecutionHandler(new ExecutionPolicyBlockCaller(this.poolQueueTimeout));
        executor.initialize();

        return executor;
    }

}