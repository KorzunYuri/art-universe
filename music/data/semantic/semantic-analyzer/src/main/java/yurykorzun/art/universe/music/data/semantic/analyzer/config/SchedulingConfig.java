package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.music.data.semantic.analyzer.scheduler.TicketPollingScheduler;
import yurykorzun.art.universe.music.data.semantic.model.SemanticNotifyChannel;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop ticketsNotificationLoop(
        TicketPollingScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            SemanticNotifyChannel.TICKETS_READY,
            scheduler::pollAndProcess,
            () -> configPropertyHolder.getInt(SemanticAnalyzerProperty.POLLING_FALLBACK_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
