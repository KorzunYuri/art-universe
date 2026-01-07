package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;

public interface TestLastfmAttributeHistoryRecordRepository extends BaseLastfmAttributeHistoryRecordRepository {

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
}
