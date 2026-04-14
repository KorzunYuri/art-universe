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
public class LastfmTrackScanner extends LastfmEntityScanner {

    public LastfmTrackScanner(
        JdbcTemplate jdbcTemplate,
        TicketIntakeClient intakeClient,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(jdbcTemplate, intakeClient, configPropertyHolder);
    }

    @Override protected String entityTable() { return "track"; }
    @Override protected MasterEntityType entityType() { return MasterEntityType.TRACK; }
    @Override protected int contentTypeContent() { return 4; } // WIKI_CONTENT
    @Override protected int contentTypeSummary() { return 3; } // WIKI_SUMMARY
    @Override protected String contentLabel() { return "wiki_content"; }
    @Override protected String summaryLabel() { return "wiki_summary"; }

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
            MasterEntityType.TRACK.getCode(),
            MasterEntityType.PERSON.getCode()
        );
    }

    @Override protected ConfigurableProperty minListenersProperty() { return LastfmTriggerProperty.TRACK_MIN_LISTENERS; }
    @Override protected ConfigurableProperty batchSizeProperty() { return LastfmTriggerProperty.TRACK_BATCH_SIZE; }
}
