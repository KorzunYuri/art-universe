package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;

import java.util.function.Function;

@Repository
public interface LastfmEntityRelationRepository extends JpaRepository<LastfmEntityRelation, Long> {

    String ENTITY_RELATION_UPSERT_SQL = """
        INSERT INTO entity_relation (
            scope_entity_type, scope_entity_id, 
            entity_type, entity_id, api_call_id
        ) VALUES (:scopeEntityType, :scopeEntityId, 
                  :entityType, :entityId, :apiCallId)
        ON CONFLICT (scope_entity_type, scope_entity_id, entity_type, entity_id) 
        DO UPDATE SET 
            api_call_id = EXCLUDED.api_call_id,
            updated_at = NOW()
        """;

    @Modifying
    @Query(value = ENTITY_RELATION_UPSERT_SQL, nativeQuery = true)
    void upsertEntityRelation(
            @Param("scopeEntityType")   int     scopeEntityType,
            @Param("scopeEntityId")     long    scopeEntityId,
            @Param("entityType")        int     entityType,
            @Param("entityId")          long    entityId,
            @Param("apiCallId")         long    apiCallId
    );

    default void upsertEntityRelation(LastfmEntityRelation e) {
        upsertEntityRelation(
                e.getScopeEntityType().getCode(),
                e.getScopeEntityId(),
                e.getEntityType().getCode(),
                e.getEntityId(),
                e.getApiCall().getId());
    };

    default Function<LastfmEntityRelation, Object[]> entityToUpsertSqlParamsMapper() {
        return e -> new Object[]{
                e.getScopeEntityType().getCode(),
                e.getScopeEntityId(),
                e.getEntityType().getCode(),
                e.getEntityId(),
                e.getApiCall().getId()
        };
    }

}
