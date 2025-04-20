package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.LocalDate;

@Repository
public interface LastfmAttributeHistoryRecordRepository extends JpaRepository<LastfmAttributeHistoryRecord, Long> {

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
}
