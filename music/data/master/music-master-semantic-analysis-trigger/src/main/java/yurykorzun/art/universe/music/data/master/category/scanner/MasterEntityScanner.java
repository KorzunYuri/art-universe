package yurykorzun.art.universe.music.data.master.category.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.category.client.TicketIntakeClient;
import yurykorzun.art.universe.music.data.master.category.config.MasterTriggerProperty;
import yurykorzun.art.universe.music.data.master.category.model.TextSample;
import yurykorzun.art.universe.music.data.master.category.model.TicketRequest;
import yurykorzun.art.universe.music.data.master.category.model.TicketSubject;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.ArrayList;
import java.util.List;

@Component
public class MasterEntityScanner {

    private static final Logger log = LoggerFactory.getLogger(MasterEntityScanner.class);

    private static final List<Integer> EXPECTED_PROPOSAL_TYPES = List.of(
        ProposalType.BIND_ENTITY_CATEGORY.getCode(),
        ProposalType.CREATE_CATEGORY.getCode()
    );

    private final JdbcTemplate jdbcTemplate;
    private final TicketIntakeClient intakeClient;
    private final ConfigPropertyHolder configPropertyHolder;

    public MasterEntityScanner(
        JdbcTemplate jdbcTemplate,
        TicketIntakeClient intakeClient,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.intakeClient = intakeClient;
        this.configPropertyHolder = configPropertyHolder;
    }

    public int scanArtists() {
        return scanEntities("artist", MasterEntityType.ARTIST);
    }

    private int scanEntities(String tableName, MasterEntityType entityType) {
        int batchSize = configPropertyHolder.getInt(MasterTriggerProperty.BATCH_SIZE);
        List<TicketRequest> tickets = new ArrayList<>();

        String sql = String.format("""
                SELECT id, name FROM mu.%s e
                WHERE NOT EXISTS (
                    SELECT 1 FROM mu_semantic_analysis.analysis_ticket at
                    WHERE at.data_source = ?
                      AND at.subject_type = ?
                      AND at.subject_id = e.id
                      AND at.analysis_mode = ?
                )
                ORDER BY e.id
                LIMIT ?
                """, tableName);

        jdbcTemplate.query(
            sql,
            rs -> {
                String name = rs.getString("name");
                tickets.add(TicketRequest.builder()
                    .dataSource(DataSource.MASTER)
                    .analysisMode(AnalysisMode.CREATIVE_CATEGORIZATION)
                    .subject(new TicketSubject(entityType, rs.getLong("id"), name))
                    .textSamples(List.of(new TextSample(name, entityType.getName() + "_name")))
                    .expectedProposalTypes(EXPECTED_PROPOSAL_TYPES)
                    .build());
            },
            DataSource.MASTER.getCode(),
            entityType.getCode(),
            AnalysisMode.CREATIVE_CATEGORIZATION.getCode(),
            batchSize
        );

        if (!tickets.isEmpty()) {
            log.info("Scanned {} {} entities without category bindings", tickets.size(), entityType.getName());
            intakeClient.submitBatch(tickets);
        }

        return tickets.size();
    }
}
