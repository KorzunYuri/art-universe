package yurykorzun.art.universe.data.raw.messaging.kafka.config;

import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.task.messaging.TaskMessageProducer;
import yurykorzun.art.universe.common.data.raw.task.dto.TaskRunRequest;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonProducerConfig;

@Component
@Import(value = {
        KafkaCommonProducerConfig.class,
        KafkaDynamicTopicCreationConfig.class,
})
public class KafkaCommonTaskMessageProducer implements TaskMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaDynamicTopicCreationConfig dynamicTopicsConfig;

    public KafkaCommonTaskMessageProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaDynamicTopicCreationConfig dynamicTopicsConfig
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.dynamicTopicsConfig = dynamicTopicsConfig;
    }

    @Override
    public void send(TaskRunRequest message) {
        this.kafkaTemplate.send(getTopicName(message), message);
    }

    protected String getTopicName(TaskRunRequest message) {
        return dynamicTopicsConfig.getTopicNameForType(message.type());
    }
}
