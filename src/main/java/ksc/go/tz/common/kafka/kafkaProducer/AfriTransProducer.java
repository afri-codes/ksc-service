package ksc.go.tz.common.kafka.kafkaProducer;

import ksc.go.tz.common.kafka.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AfriTransProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUpdateCompanyUser(Object message) {

        if (message == null) {
            log.warn("[KAFKA-PRODUCER] Skipping send: null message");
            return;
        }

        String topic = KafkaTopic.COMPANY_USER_REGISTRATION;

        log.info("[KAFKA-PRODUCER] Sending user company update to topic={} message={}", topic, message);

        kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        RecordMetadata meta = result.getRecordMetadata();
                        log.info("[KAFKA-PRODUCER] SENT topic={} partition={} offset={}",
                                meta.topic(), meta.partition(), meta.offset());
                    } else {
                        log.error("[KAFKA-PRODUCER] FAILED topic={} error={}",
                                topic, ex.getMessage(), ex);
                    }
                });
    }
}
