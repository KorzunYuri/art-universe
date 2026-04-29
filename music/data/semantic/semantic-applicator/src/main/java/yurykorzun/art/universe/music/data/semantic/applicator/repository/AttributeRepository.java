package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeDataType;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeSourceType;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTemporalType;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AttributeRepository {

    private final JdbcTemplate jdbcTemplate;

    public AttributeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public AttributeDef findDefByCode(String code) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT id, data_type FROM mu.attribute_def WHERE code = ?",
                (rs, rowNum) -> new AttributeDef(rs.getLong("id"), AttributeDataType.fromCode(rs.getInt("data_type"))),
                code
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long createDef(String code, String name, AttributeDataType dataType, int temporalType, int computationType, boolean system) {
        Long id = jdbcTemplate.queryForObject("SELECT nextval('mu.attribute_def_seq')", Long.class);
        jdbcTemplate.update(
            "INSERT INTO mu.attribute_def (id, code, name, data_type, temporal_type, computation_type, is_system, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, code, name, dataType.getCode(), temporalType, computationType, system
        );
        return id;
    }

    public void bindApplicability(Long defId, List<MasterEntityType> applicableTo) {
        for (MasterEntityType type : applicableTo) {
            Long id = jdbcTemplate.queryForObject("SELECT nextval('mu.attribute_def_applicability_seq')", Long.class);
            jdbcTemplate.update(
                "INSERT INTO mu.attribute_def_applicability (id, attribute_def_id, target_type) VALUES (?, ?, ?) "
                    + "ON CONFLICT ON CONSTRAINT attribute_def_applicability_UQ DO NOTHING",
                id, defId, type.getCode()
            );
        }
    }

    public Long createValue(
        MasterEntityType targetType,
        Long targetId,
        Long attributeDefId,
        AttributeDataType dataType,
        String rawValue,
        Short confidence,
        LocalDate eventDate,
        LocalDate validFrom,
        LocalDate validTill,
        String sourceRef
    ) {
        Long id = jdbcTemplate.queryForObject("SELECT nextval('mu.entity_attribute_value_seq')", Long.class);

        Long numericValue = dataType == AttributeDataType.NUMERIC ? parseLong(rawValue) : null;
        String stringValue = dataType == AttributeDataType.STRING ? rawValue : null;
        LocalDate dateValue = dataType == AttributeDataType.DATE ? parseDate(rawValue) : null;
        Boolean boolValue = dataType == AttributeDataType.BOOLEAN ? parseBool(rawValue) : null;

        jdbcTemplate.update(
            "INSERT INTO mu.entity_attribute_value "
                + "(id, target_type, target_id, attribute_def_id, numeric_value, string_value, date_value, bool_value, "
                + " event_date, valid_from, valid_till, source_type, source_ref, confidence, origin, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            id, targetType.getCode(), targetId, attributeDefId,
            numericValue, stringValue, dateValue, boolValue,
            eventDate, validFrom, validTill,
            AttributeSourceType.LLM_EXTRACTED.getCode(), sourceRef, confidence, Origin.AI.getCode()
        );
        return id;
    }

    public int updateValue(
        Long valueId,
        AttributeDataType dataType,
        String rawValue,
        Short confidence,
        String sourceRef
    ) {
        Long numericValue = dataType == AttributeDataType.NUMERIC ? parseLong(rawValue) : null;
        String stringValue = dataType == AttributeDataType.STRING ? rawValue : null;
        LocalDate dateValue = dataType == AttributeDataType.DATE ? parseDate(rawValue) : null;
        Boolean boolValue = dataType == AttributeDataType.BOOLEAN ? parseBool(rawValue) : null;

        return jdbcTemplate.update(
            "UPDATE mu.entity_attribute_value "
                + "SET numeric_value = ?, string_value = ?, date_value = ?, bool_value = ?, "
                + "    confidence = ?, source_ref = ?, updated_at = CURRENT_TIMESTAMP "
                + "WHERE id = ?",
            numericValue, stringValue, dateValue, boolValue,
            confidence, sourceRef, valueId
        );
    }

    public Long findExistingValueId(MasterEntityType targetType, Long targetId, Long attributeDefId) {
        return jdbcTemplate.query(
            "SELECT id FROM mu.entity_attribute_value "
                + "WHERE target_type = ? AND target_id = ? AND attribute_def_id = ? "
                + "ORDER BY updated_at DESC LIMIT 1",
            rs -> rs.next() ? rs.getLong("id") : null,
            targetType.getCode(), targetId, attributeDefId
        );
    }

    public int defaultTemporalType() {
        return AttributeTemporalType.CONSTANT.getCode();
    }

    private Long parseLong(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return (long) Double.parseDouble(raw.trim());
        }
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return LocalDate.parse(raw.trim());
    }

    private Boolean parseBool(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Boolean.parseBoolean(raw.trim());
    }

    public record AttributeDef(Long id, AttributeDataType dataType) {}
}
