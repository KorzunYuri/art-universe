package yurykorzun.art.universe.music.data.raw.lastfm.config.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.messaging.kafka.KafkaCommonProducer;

@Configuration
@Import(KafkaCommonProducer.class)
public class KafkaProducerConfig {
}
