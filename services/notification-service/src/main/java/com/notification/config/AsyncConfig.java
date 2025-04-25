package com.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
public class AsyncConfig {
    private static final int N_CORE = Runtime.getRuntime().availableProcessors();
    private static final int TPTE_CORE_POOL_SIZE = 2 * N_CORE; // initial thread size in pool
    private static final int TPTE_MAX_POOL_SIZE = 128 * TPTE_CORE_POOL_SIZE;
    private static final int TPTE_QUEUE_CAPACITY = 2 * TPTE_MAX_POOL_SIZE;
    private static final int TPTE_KEEP_ALIVE_SECONDS = (int) TimeUnit.MINUTES.toSeconds(1);

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(TPTE_CORE_POOL_SIZE);
        executor.setMaxPoolSize(TPTE_MAX_POOL_SIZE);
        executor.setQueueCapacity(TPTE_QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(TPTE_KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix("BJIT_DigitalBankingThread-");
        executor.initialize();
        return executor;
    }
}
