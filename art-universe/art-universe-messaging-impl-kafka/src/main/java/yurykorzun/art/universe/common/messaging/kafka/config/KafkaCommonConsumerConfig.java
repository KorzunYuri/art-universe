package yurykorzun.art.universe.common.messaging.kafka.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import(KafkaCommonConfig.class)
public class KafkaCommonConsumerConfig {

    private final KafkaCommonConfig kafkaConfig;

    public KafkaCommonConsumerConfig(KafkaCommonConfig kafkaCommonConfig) {
        this.kafkaConfig = kafkaCommonConfig;
    }

    @Bean("commonKafkaConsumerProperties")
    public Map<String, Object> getStringObjectMap() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "yurykorzun.art.universe.*");
        return configProps;
    }
}
