package com.firisbe.config.dbConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.firisbe.repository.mongo")
public class MongoConfig {
}
