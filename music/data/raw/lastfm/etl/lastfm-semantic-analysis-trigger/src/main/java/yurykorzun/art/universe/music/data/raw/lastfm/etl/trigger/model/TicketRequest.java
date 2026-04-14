package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.List;

@Data
@Builder
public class TicketRequest {

    @JsonProperty("data_source")
    private DataSource dataSource;

    @JsonProperty("analysis_mode")
    private AnalysisMode analysisMode;

    private TicketSubject subject;

    @JsonProperty("text_samples")
    private List<TextSample> textSamples;

    @JsonProperty("expected_proposal_types")
    private List<Integer> expectedProposalTypes;

    @JsonProperty("expected_entity_types")
    private List<Integer> expectedEntityTypes;
}
