package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Service
public class LastfmAttributeHistoryServiceImpl implements LastfmAttributeHistoryService {

    private final JdbcTemplate jdbcTemplate;
    private final LastfmAttributeHistoryProcessor processor;

    public LastfmAttributeHistoryServiceImpl(
            JdbcTemplate jdbcTemplate,
            LastfmAttributeHistoryProcessor processor) {
        this.jdbcTemplate = jdbcTemplate;
        this.processor = processor;
    }

    @Override
    @Transactional
    public void upsertCandidateValues(List<LastfmAttributeHistoryRecord> candidates) {
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
            ps.setLong(1, record.getApiCallId());
            ps.setInt(2, record.getEntityType().getCode());
            ps.setLong(3, record.getEntityId());
            ps.setInt(4, record.getAttribute().getCode());
            ps.setObject(5, record.getScopeEntityType() != null ? record.getScopeEntityType().getCode() : null);
            ps.setObject(6, record.getScopeEntityId());
            ps.setString(7, record.getStringValue());
            ps.setObject(8, record.getNumericValue());
            ps.setTimestamp(9, Timestamp.from(record.getCollectionTs()));
            ps.setDate(10, Date.valueOf(record.getValidFrom()));
        });
    }
}
