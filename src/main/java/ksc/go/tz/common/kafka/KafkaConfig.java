package ksc.go.tz.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@Slf4j
@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic auditLogsTopic() {
        return TopicBuilder.name(KafkaTopic.AUDIT_LOGS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic companyUserTopic() {
        return TopicBuilder.name(KafkaTopic.COMPANY_USER_REGISTRATION)
                .partitions(3)
                .replicas(1)
                .build();
    }
}