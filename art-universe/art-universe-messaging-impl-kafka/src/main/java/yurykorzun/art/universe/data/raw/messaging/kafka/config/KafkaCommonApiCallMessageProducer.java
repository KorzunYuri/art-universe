package yurykorzun.art.universe.data.raw.messaging.kafka.config;

import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.apiclient.dto.ApiCallRunRequest;
import yurykorzun.art.universe.common.data.raw.apiclient.messaging.ApiCallMessageProducer;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonProducerConfig;

@Component
@Import(value = {
        KafkaCommonProducerConfig.class,
        KafkaDynamicTopicCreationConfig.class,
})
public class KafkaCommonApiCallMessageProducer implements ApiCallMessageProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaDynamicTopicCreationConfig dynamicTopicsConfig;

    public KafkaCommonApiCallMessageProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            KafkaDynamicTopicCreationConfig dynamicTopicsConfig
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.dynamicTopicsConfig = dynamicTopicsConfig;
    }

    @Override
    public void send(ApiCallRunRequest message) {
        this.kafkaTemplate.send(getTopicName(message), message);
    }

    protected String getTopicName(ApiCallRunRequest message) {
        return dynamicTopicsConfig.getTopicNameForType(message.getType());
    }
}
