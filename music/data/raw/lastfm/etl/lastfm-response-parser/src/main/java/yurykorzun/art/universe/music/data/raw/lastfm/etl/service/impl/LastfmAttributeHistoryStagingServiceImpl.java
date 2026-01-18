package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmAttributeHistoryCandidate;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmAttributeHistoryStagingService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor.LastfmAttributeHistoryProcessor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Service
public class LastfmAttributeHistoryStagingServiceImpl implements LastfmAttributeHistoryStagingService {

    private final JdbcTemplate jdbcTemplate;
    private final LastfmAttributeHistoryProcessor processor;

    public LastfmAttributeHistoryStagingServiceImpl(
            JdbcTemplate jdbcTemplate,
            LastfmAttributeHistoryProcessor processor) {
        this.jdbcTemplate = jdbcTemplate;
        this.processor = processor;
    }

    @Override
    @Transactional
    public void upsertCandidateValues(List<LastfmAttributeHistoryCandidate> candidates) {
        if (candidates.isEmpty()) {
            return;
        }

        String currentTable = processor.getCurrentStagingTable();
        
        String sql = """
            INSERT INTO %s
                (api_call_id, entity_type, entity_id, attribute_id, scope_entity_type, scope_entity_id,
                string_value, numeric_value, collection_ts, valid_from)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
            ON CONFLICT (entity_type, entity_id, attribute_id, COALESCE(scope_entity_type, -1), COALESCE(scope_entity_id, -1)) 
            DO UPDATE SET 
                api_call_id = EXCLUDED.api_call_id, 
                string_value = EXCLUDED.string_value, 
                numeric_value = EXCLUDED.numeric_value, 
                collection_ts = EXCLUDED.collection_ts, 
                valid_from = EXCLUDED.valid_from
            """.formatted(currentTable);
            
        jdbcTemplate.batchUpdate(sql, candidates, candidates.size(), (ps, record) -> {
            ps.setLong(1, record.apiCallId());
            ps.setInt(2, record.entityType().getCode());
            ps.setLong(3, record.entityId());
            ps.setInt(4, record.attribute().getCode());
            ps.setObject(5, record.scopeEntityType() != null ? record.scopeEntityType().getCode() : null);
            ps.setObject(6, record.scopeEntityId());
            ps.setString(7, record.stringValue());
            ps.setObject(8, record.numericValue());
            ps.setTimestamp(9, Timestamp.from(record.collectionTs()));
            ps.setDate(10, Date.valueOf(record.validFrom()));
        });
    }
}
