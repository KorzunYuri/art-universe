package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.client.TicketIntakeClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.config.LastfmTriggerProperty;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

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
    @Override protected MasterEntityType entityType() { return MasterEntityType.ARTIST; }
    @Override protected int contentTypeContent() { return 2; } // BIO_CONTENT
    @Override protected int contentTypeSummary() { return 1; } // BIO_SUMMARY
    @Override protected String contentLabel() { return "bio_content"; }
    @Override protected String summaryLabel() { return "bio_summary"; }

    @Override protected List<Integer> expectedProposalTypes() {
        return List.of(
            ProposalType.CREATE_ENTITY.getCode(),
            ProposalType.CREATE_RELATION.getCode(),
            ProposalType.CREATE_ATTRIBUTE.getCode(),
            ProposalType.BIND_ENTITY_CATEGORY.getCode(),
            ProposalType.CREATE_CATEGORY.getCode(),
            ProposalType.BIND_EXTERNAL_ENTITY.getCode()
        );
    }

    @Override protected List<Integer> expectedEntityTypes() {
        return List.of(
            MasterEntityType.ARTIST.getCode(),
            MasterEntityType.ALBUM.getCode(),
            MasterEntityType.PERSON.getCode()
        );
    }

    @Override protected ConfigurableProperty minListenersProperty() { return LastfmTriggerProperty.ARTIST_MIN_LISTENERS; }
    @Override protected ConfigurableProperty batchSizeProperty() { return LastfmTriggerProperty.ARTIST_BATCH_SIZE; }
}
