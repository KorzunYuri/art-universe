package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.EntityTextContent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentType;

import java.util.List;
import java.util.Optional;

public interface EntityTextContentRepository extends JpaRepository<EntityTextContent, Long> {

    @Modifying
    @Query(value = """
        INSERT INTO entity_text_content (entity_type, entity_id, content_type, content, published_at, api_call_id, data_snapshot_id, created_at, updated_at)
        VALUES (:entityType, :entityId, :contentType, :content, :publishedAt, :apiCallId, :dataSnapshotId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (entity_type, entity_id, content_type) DO UPDATE
            SET content          = EXCLUDED.content,
                published_at     = EXCLUDED.published_at,
                api_call_id      = EXCLUDED.api_call_id,
                data_snapshot_id = EXCLUDED.data_snapshot_id,
                updated_at       = CURRENT_TIMESTAMP
            WHERE entity_text_content.data_snapshot_id IS NULL
               OR EXCLUDED.data_snapshot_id IS NULL
               OR EXCLUDED.data_snapshot_id > entity_text_content.data_snapshot_id
        """, nativeQuery = true)
    void upsert(
            @Param("entityType") int entityType,
            @Param("entityId") long entityId,
            @Param("contentType") int contentType,
            @Param("content") String content,
            @Param("publishedAt") String publishedAt,
            @Param("apiCallId") long apiCallId,
            @Param("dataSnapshotId") Long dataSnapshotId
    );

    List<EntityTextContent> findByEntityTypeAndEntityId(LastfmEntityType entityType, long entityId);

    Optional<EntityTextContent> findByEntityTypeAndEntityIdAndContentType(
            LastfmEntityType entityType, long entityId, TextContentType contentType);
}
