package ksc.go.tz.common.kafka.kafkaConsumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AfriTransConsumer {

//    @KafkaListener(topics = KafkaTopic.CREATE_CIVIL, groupId = "hearing-service-create-civil-group")
//    public void consumeCreateCivil(CivilMessage message) {
//        log.info("Received CREATE Civil message: {}", message);
//        saveCivil(message);
//        saveRemark(message.getRemarkMessage());
//    }

}
