package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.client.TicketIntakeClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.config.LastfmTriggerProperty;

import java.util.List;

@Component
public class LastfmArtistScanner extends LastfmEntityScanner {

    public LastfmArtistScanner(
        JdbcTemplate jdbcTemplate,
        TicketIntakeClient intakeClient,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(jdbcTemplate, intakeClient, configPropertyHolder);
    }

    @Override protected String entityTable() { return "artist"; }
    @Override protected String entityTypeName() { return "artist"; }
    @Override protected int entityTypeCode() { return 1; }
    @Override protected int contentTypeContent() { return 2; } // BIO_CONTENT
    @Override protected int contentTypeSummary() { return 1; } // BIO_SUMMARY
    @Override protected String contentLabel() { return "bio_content"; }
    @Override protected String summaryLabel() { return "bio_summary"; }
    @Override protected List<Integer> expectedProposalTypes() { return List.of(1, 2, 3, 6, 7, 9); }
    @Override protected List<Integer> expectedEntityTypes() { return List.of(1, 2, 101); }
    @Override protected ConfigurableProperty minListenersProperty() { return LastfmTriggerProperty.ARTIST_MIN_LISTENERS; }
    @Override protected ConfigurableProperty batchSizeProperty() { return LastfmTriggerProperty.ARTIST_BATCH_SIZE; }
}
