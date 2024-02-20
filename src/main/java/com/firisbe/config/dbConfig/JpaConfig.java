package com.firisbe.config.dbConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.firisbe.repository.jpa")
public class JpaConfig {
}
