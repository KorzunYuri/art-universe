package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository;

import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.LocalDate;

@Repository
public interface LastfmAttributeHistoryRecordRepository extends JpaRepository<LastfmAttributeHistoryRecord, Long> {

    LastfmAttributeHistoryRecord findByEntityTypeAndEntityIdAndScopeEntityTypeAndScopeEntityIdAndAttributeAndValidTill(
        @NonNull    LastfmEntityType entityType,
        @NonNull    long entityId,
        @Nullable   LastfmEntityType scopeEntityType,
        @Nullable   long scopeEntityId,
        @NonNull    LastfmAttribute attribute,
        @NonNull    LocalDate validTill
    );

    default LastfmAttributeHistoryRecord findCurrentValueForCandidate(LastfmAttributeHistoryRecord candidate) {
        return findByEntityTypeAndEntityIdAndScopeEntityTypeAndScopeEntityIdAndAttributeAndValidTill(
            candidate.getEntityType(),
            candidate.getEntityId(),
            candidate.getScopeEntityType(),
            candidate.getScopeEntityId(),
            candidate.getAttribute(),
            LastfmConstants.END_OF_TIME
        );
    }

}
