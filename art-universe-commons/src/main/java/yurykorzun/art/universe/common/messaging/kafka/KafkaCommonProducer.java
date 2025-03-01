package yurykorzun.art.universe.common.messaging.kafka;

import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.messaging.dto.DataCollectionTaskMessage;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonProducerConfig;
import yurykorzun.art.universe.common.messaging.MessageProducer;

@Component
@Import(KafkaCommonProducerConfig.class)
public class KafkaCommonProducer implements MessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaCommonProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(DataCollectionTaskMessage message) {
        this.kafkaTemplate.send(message.type().getCode(), message);
    }
}
