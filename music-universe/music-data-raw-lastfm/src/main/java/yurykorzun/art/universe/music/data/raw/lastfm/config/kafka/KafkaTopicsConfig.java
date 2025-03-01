package yurykorzun.art.universe.music.data.raw.lastfm.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonAdminConfig;

@Configuration
@EnableKafka
@ConfigurationProperties(prefix = "messaging.lastfm.topics")
@Import(KafkaCommonAdminConfig.class)
public class KafkaTopicsConfig {

    @Value("${messaging.lastfm.topics.tags.topTags}")  private String topTagsTopicName;

    @Bean
    public NewTopic tagsRequestTopic() {
        return TopicBuilder.name(topTagsTopicName)
                .partitions(3)
                .replicas(1)
            .build();
    }
}
