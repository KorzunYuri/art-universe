package yurykorzun.art.universe.common.messaging.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(KafkaCommonConfig.class)
public class KafkaConsumerConfig {

    private final KafkaCommonConfig kafkaConfig;

    public KafkaConsumerConfig(KafkaCommonConfig kafkaConfig) {
        this.kafkaConfig = kafkaConfig;
    }

    /**
     * Builds consumer properties for a specific group and value type.
     * Each consuming module creates its own consumers with distinct group IDs.
     */
    public <V> Map<String, Object> buildConsumerProperties(String groupId, Class<V> valueType) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "yurykorzun.art.universe.*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, valueType.getName());
        return props;
    }
}
