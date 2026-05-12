package com.example.iotalarmcopilot.persistence.support;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 持久化自动装配类
 */
@AutoConfiguration
@EnableTransactionManagement
@MapperScan(basePackages = "com.example.iotalarmcopilot", annotationClass = Mapper.class)
public class PersistenceSupportAutoConfiguration {

    @Bean
    ConfigurationCustomizer persistenceMybatisConfigurationCustomizer() {
        return configuration -> configuration.setMapUnderscoreToCamelCase(true);
    }
}
