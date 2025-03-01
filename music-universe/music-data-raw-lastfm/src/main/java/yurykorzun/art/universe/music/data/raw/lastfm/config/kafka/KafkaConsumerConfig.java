package yurykorzun.art.universe.music.data.raw.lastfm.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonConsumerConfig;

import java.util.Map;

@Configuration
@EnableKafka
@Import(KafkaCommonConsumerConfig.class)
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(
            @Qualifier("commonKafkaConsumerProperties") Map<String, Object> configProps,
            @Qualifier("kafkaObjectMapper") ObjectMapper kafkaObjectMapper
    ) {
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "art.universe.music.data.raw.lastfm");

        DefaultKafkaConsumerFactory<String, Object> factory =
                new DefaultKafkaConsumerFactory<>(configProps);
        factory.setValueDeserializer(new JsonDeserializer<>(Object.class, kafkaObjectMapper));
        return factory;
    }
}
