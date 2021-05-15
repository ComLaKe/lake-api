package com.ulake.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.ulake.api.repository")
@PropertySource("classpath:com.ulake.api.datasource.properties")
@EntityScan(basePackages={ "com.ulake.api.models" })
public class JPAPersistenceConfig {

}
