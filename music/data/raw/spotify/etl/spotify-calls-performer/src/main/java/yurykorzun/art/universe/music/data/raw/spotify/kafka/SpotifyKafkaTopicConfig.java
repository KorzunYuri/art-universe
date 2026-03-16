package yurykorzun.art.universe.music.data.raw.spotify.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.messaging.SpotifyKafkaTopics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class SpotifyKafkaTopicConfig {

    @Bean
    public KafkaAdmin.NewTopics spotifyTopics() {
        List<NewTopic> topics = new ArrayList<>();

        for (SpotifyApiCallType type : SpotifyApiCallType.values()) {
            int partitions = type.name().startsWith("SEARCH_") ? 1 : 3;
            topics.add(TopicBuilder.name(SpotifyKafkaTopics.callTopicFor(type))
                    .partitions(partitions)
                    .replicas(1)
                    .config(TopicConfig.RETENTION_MS_CONFIG,
                            String.valueOf(TimeUnit.DAYS.toMillis(3)))
                    .build());
        }

        topics.add(TopicBuilder.name(SpotifyKafkaTopics.RESPONSES_TOPIC)
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG,
                        String.valueOf(TimeUnit.DAYS.toMillis(3)))
                .build());

        return new KafkaAdmin.NewTopics(topics.toArray(NewTopic[]::new));
    }
}
