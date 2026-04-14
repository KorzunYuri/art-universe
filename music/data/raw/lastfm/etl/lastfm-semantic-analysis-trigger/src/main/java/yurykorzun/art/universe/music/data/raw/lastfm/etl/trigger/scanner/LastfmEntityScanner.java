package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.config.client.ConfigurableProperty;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.client.TicketIntakeClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.model.TextSample;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.model.TicketRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.model.TicketSubject;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.ArrayList;
import java.util.List;

public abstract class LastfmEntityScanner {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final JdbcTemplate jdbcTemplate;
    private final TicketIntakeClient intakeClient;
    private final ConfigPropertyHolder configPropertyHolder;

    protected LastfmEntityScanner(
        JdbcTemplate jdbcTemplate,
        TicketIntakeClient intakeClient,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.intakeClient = intakeClient;
        this.configPropertyHolder = configPropertyHolder;
    }

    protected abstract String entityTable();

    protected abstract MasterEntityType entityType();

    protected abstract int contentTypeContent();

    protected abstract int contentTypeSummary();

    protected abstract String contentLabel();

    protected abstract String summaryLabel();

    protected abstract List<Integer> expectedProposalTypes();

    protected abstract List<Integer> expectedEntityTypes();

    protected abstract ConfigurableProperty minListenersProperty();

    protected abstract ConfigurableProperty batchSizeProperty();

    public int scanAndSubmit() {
        int minListeners = configPropertyHolder.getInt(minListenersProperty());
        int batchSize = configPropertyHolder.getInt(batchSizeProperty());
        int entityTypeCode = entityType().getCode();

        List<TicketRequest> tickets = new ArrayList<>();

        String sql = """
                SELECT e.id, e.name,
                       cnt.content     AS text_content,
                       summary.content AS text_summary
                FROM mu_raw_lastfm.%s e
                JOIN mu_raw_lastfm.entity_text_content cnt
                  ON cnt.entity_type = ? AND cnt.entity_id = e.id AND cnt.content_type = ?
                LEFT JOIN mu_raw_lastfm.entity_text_content summary
                  ON summary.entity_type = ? AND summary.entity_id = e.id AND summary.content_type = ?
                WHERE e.listeners_count >= ?
                  AND NOT EXISTS (
                      SELECT 1 FROM mu_semantic_analysis.analysis_ticket at
                      WHERE at.data_source = ?
                        AND at.subject_type = ?
                        AND at.subject_id = e.id
                        AND at.analysis_mode = ?
                  )
                ORDER BY e.listeners_count DESC
                LIMIT ?
                """.formatted(entityTable());

        jdbcTemplate.query(
            sql,
            rs -> {
                List<TextSample> samples = new ArrayList<>();

                String content = rs.getString("text_content");
                if (content != null) {
                    samples.add(new TextSample(content, contentLabel()));
                }

                String summary = rs.getString("text_summary");
                if (summary != null) {
                    samples.add(new TextSample(summary, summaryLabel()));
                }

                tickets.add(TicketRequest.builder()
                    .dataSource(DataSource.LASTFM)
                    .analysisMode(AnalysisMode.FULL_EXTRACTION)
                    .subject(new TicketSubject(entityType(), rs.getLong("id"), rs.getString("name")))
                    .textSamples(samples)
                    .expectedProposalTypes(expectedProposalTypes())
                    .expectedEntityTypes(expectedEntityTypes())
                    .build());
            },
            entityTypeCode, contentTypeContent(),
            entityTypeCode, contentTypeSummary(),
            minListeners,
            DataSource.LASTFM.getCode(), entityTypeCode, AnalysisMode.FULL_EXTRACTION.getCode(),
            batchSize
        );

        if (!tickets.isEmpty()) {
            log.info("Scanned {} eligible LastFM {}s (minListeners={}, batchSize={})",
                    tickets.size(), entityType().getName(), minListeners, batchSize);
            intakeClient.submitBatch(tickets);
        }

        return tickets.size();
    }
}
