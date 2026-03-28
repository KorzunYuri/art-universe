package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.client.TicketIntakeClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.config.LastfmTriggerProperty;

import java.util.List;

@Component
public class LastfmTrackScanner extends LastfmEntityScanner {

    public LastfmTrackScanner(
        JdbcTemplate jdbcTemplate,
        TicketIntakeClient intakeClient,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(jdbcTemplate, intakeClient, configPropertyHolder);
    }

    @Override protected String entityTable() { return "track"; }
    @Override protected String entityTypeName() { return "track"; }
    @Override protected int entityTypeCode() { return 3; }
    @Override protected int contentTypeContent() { return 4; } // WIKI_CONTENT
    @Override protected int contentTypeSummary() { return 3; } // WIKI_SUMMARY
    @Override protected String contentLabel() { return "wiki_content"; }
    @Override protected String summaryLabel() { return "wiki_summary"; }
    @Override protected List<Integer> expectedProposalTypes() { return List.of(1, 2, 3, 6, 7, 9); }
    @Override protected List<Integer> expectedEntityTypes() { return List.of(1, 3, 101); }
    @Override protected ConfigurableProperty minListenersProperty() { return LastfmTriggerProperty.TRACK_MIN_LISTENERS; }
    @Override protected ConfigurableProperty batchSizeProperty() { return LastfmTriggerProperty.TRACK_BATCH_SIZE; }
}
