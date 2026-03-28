package yurykorzun.art.universe.music.data.semantic.parser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.music.data.semantic.model.SemanticNotifyChannel;
import yurykorzun.art.universe.music.data.semantic.parser.scheduler.ResponseParsingScheduler;

import javax.sql.DataSource;

@Configuration
public class SchedulingConfig {

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop analysisCompletedNotificationLoop(
        ResponseParsingScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            SemanticNotifyChannel.ANALYSIS_COMPLETED,
            scheduler::pollAndParse,
            () -> configPropertyHolder.getInt(SemanticParserProperty.POLLING_FALLBACK_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
