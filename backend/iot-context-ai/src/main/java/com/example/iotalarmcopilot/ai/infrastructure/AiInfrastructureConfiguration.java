package com.example.iotalarmcopilot.ai.infrastructure;

import com.example.iotalarmcopilot.ai.infrastructure.gateway.AiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Executor;

/**
 * 其它配置
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AiProperties.class)
public class AiInfrastructureConfiguration {

    /**
     * 独立执行器
     *
     * @return
     */
    @Bean("aiSummaryExecutor")
    public Executor aiSummaryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("ai-summary-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.initialize();
        return executor;
    }

    /**
     * 手动事务模板
     *
     * @param platformTransactionManager
     * @return
     */
    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager platformTransactionManager) {
        return new TransactionTemplate(platformTransactionManager);
    }
}
