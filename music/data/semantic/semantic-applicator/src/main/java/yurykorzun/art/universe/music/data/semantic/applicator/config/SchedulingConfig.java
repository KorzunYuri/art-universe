package yurykorzun.art.universe.music.data.semantic.applicator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.music.data.semantic.applicator.scheduler.ApplicatorScheduler;
import yurykorzun.art.universe.music.data.semantic.model.SemanticNotifyChannel;

import javax.sql.DataSource;

@Configuration
public class SchedulingConfig {

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop proposalsNotificationLoop(
        ApplicatorScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            SemanticNotifyChannel.PROPOSALS_READY,
            scheduler::evaluateAndApply,
            () -> configPropertyHolder.getInt(SemanticApplicatorProperty.POLLING_FALLBACK_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
