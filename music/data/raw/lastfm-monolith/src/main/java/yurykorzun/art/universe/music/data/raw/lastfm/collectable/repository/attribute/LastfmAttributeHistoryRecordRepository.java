package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LastfmAttributeHistoryRecordRepository extends JpaRepository<LastfmAttributeHistoryRecord, Long> {

    /**
     * Returns all attribute values by uniting scoped and non-scoped selections.
     */
    @Query(value = """
        SELECT r
        FROM attribute_history r
        WHERE   1=1
            AND r.entityType        = :entityType
            AND r.entityId          = :entityId
            AND r.attribute         = :attribute
            AND r.validTill         = :validTill
            AND :scopeEntityType    IS NULL AND :scopeEntityId  IS NULL
            AND r.scopeEntityType   IS NULL AND r.scopeEntityId IS NULL
        
        UNION ALL
        
        SELECT r
        FROM attribute_history r
        WHERE   1=1
            AND r.entityType        = :entityType
            AND r.entityId          = :entityId
            AND r.attribute         = :attribute
            AND r.validTill         = :validTill
            AND r.scopeEntityType   = :scopeEntityType
            AND r.scopeEntityId     = :scopeEntityId
        """)
    LastfmAttributeHistoryRecord findValue(
                    @Param("entityType")        LastfmEntityType entityType,
                    @Param("entityId")          Long entityId,
        @Nullable   @Param("scopeEntityType")   LastfmEntityType scopeEntityType,
        @Nullable   @Param("scopeEntityId")     Long scopeEntityId,
                    @Param("attribute")         LastfmAttribute attribute,
                    @Param("validTill")         LocalDate validTill
    );

    default LastfmAttributeHistoryRecord findCurrentValueForCandidate(LastfmAttributeHistoryRecord candidate) {
        return findCurrentValueForCandidate(candidate, LastfmConstants.END_OF_TIME);
    }

    /**
     * Temporary fix to detect duplicating attribute value records
     // TODO figure out the reason of attribute_history duplication
     */
    default LastfmAttributeHistoryRecord findCurrentValueForCandidate(LastfmAttributeHistoryRecord candidate, LocalDate expirationDate) {
        return findValue(
            candidate.getEntityType(),
            candidate.getEntityId(),
            candidate.getScopeEntityType(),
            candidate.getScopeEntityId(),
            candidate.getAttribute(),
            expirationDate
        );
    }
    
    /**
     * Find all attribute history records for a specific entity and attribute (non-scoped)
     */
    @Query(value = """
            SELECT  ah
            FROM    attribute_history ah
            WHERE   1=1
                AND ah.attribute    = :attribute
                AND ah.entityType   = :entityType
                AND ah.entityId     = :entityId
                AND ah.scopeEntityType  IS NULL
                AND ah.scopeEntityId    IS NULL
        """)
    List<LastfmAttributeHistoryRecord> findAttributeValuesForEntity(
        @Param("attribute")     LastfmAttribute attribute,
        @Param("entityType")    LastfmEntityType entityType,
        @Param("entityId")      Long entityId
    );
    
    /**
     * Find all attribute history records for a specific entity and attribute with scope
     */
    @Query(value = """
            SELECT  ah
            FROM    attribute_history ah
            WHERE   1=1
                AND ah.attribute        = :attribute
                AND ah.entityType       = :entityType
                AND ah.entityId         = :entityId
                AND ah.scopeEntityType  = :scopeEntityType
                AND ah.scopeEntityId    = :scopeEntityId
        """)
    List<LastfmAttributeHistoryRecord> findAttributeValuesForEntityWithScope(
        @Param("attribute")         LastfmAttribute attribute,
        @Param("entityType")        LastfmEntityType entityType,
        @Param("entityId")          Long entityId,
        @Param("scopeEntityType")   LastfmEntityType scopeEntityType,
        @Param("scopeEntityId")     Long scopeEntityId
    );
    
    /**
     * Find all attribute history records for a specific entity
     */
    @Query(value = """
            SELECT  ah
            FROM    attribute_history ah
            WHERE   1=1
                AND ah.entityType   = :entityType
                AND ah.entityId     = :entityId
        """)
    List<LastfmAttributeHistoryRecord> findByEntityTypeAndEntityId(
        @Param("entityType")    LastfmEntityType entityType,
        @Param("entityId")      Long entityId
    );
}
