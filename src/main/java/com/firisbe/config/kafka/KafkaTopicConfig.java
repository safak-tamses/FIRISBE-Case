package com.firisbe.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic success() {
        return TopicBuilder.name("successful_logs")
                .build();
    }

    @Bean
    public NewTopic error() {
        return TopicBuilder.name("error_logs")
                .build();
    }

    @Bean
    public NewTopic paymentLog() {
        return TopicBuilder.name("payment-log")
                .build();
    }
}

