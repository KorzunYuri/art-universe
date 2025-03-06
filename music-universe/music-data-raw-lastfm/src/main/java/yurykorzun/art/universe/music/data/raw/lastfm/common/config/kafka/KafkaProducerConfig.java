package yurykorzun.art.universe.music.data.raw.lastfm.common.config.kafka;

import yurykorzun.art.universe.data.raw.messaging.kafka.config.KafkaCommonApiCallMessageProducer;
import yurykorzun.art.universe.data.raw.messaging.kafka.config.KafkaCommonTaskMessageProducer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {
        KafkaCommonTaskMessageProducer.class,
        KafkaCommonApiCallMessageProducer.class
})
public class KafkaProducerConfig {
}
