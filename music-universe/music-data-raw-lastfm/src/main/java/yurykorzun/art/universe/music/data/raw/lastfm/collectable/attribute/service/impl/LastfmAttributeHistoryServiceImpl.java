package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class LastfmAttributeHistoryServiceImpl implements LastfmAttributeHistoryService {

    private final LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    public LastfmAttributeHistoryServiceImpl(LastfmAttributeHistoryRecordRepository attributeHistoryRepository) {
        this.attributeHistoryRepository = attributeHistoryRepository;
    }

    @Override
    @Transactional
    public List<LastfmAttributeHistoryRecord> upsertCandidateValues(List<LastfmAttributeHistoryRecord> candidates) {
        return candidates.stream()
            .map(this::upsertCandidateValue)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Nullable
    @Transactional
    @Override
    public LastfmAttributeHistoryRecord upsertCandidateValue(LastfmAttributeHistoryRecord candidate) {
        LastfmAttributeHistoryRecord existing = attributeHistoryRepository.findCurrentValueForCandidate(candidate);
        if (existing != null) {
            if (valueHasChanged(candidate, existing)) {
                if (isSnapshotAttribute(candidate) && belongsToTheSameSnapshot(candidate, existing)) {
                    return null; // don't save the new value for the same snapshot
                }
                return expireOldAndSaveNewValue(candidate, existing);
            } else {
                if (shouldSaveNonChangedValue(candidate, existing)) {
                    return expireOldAndSaveNewValue(candidate, existing);
                }
                return null;
            }
        } else {
            return attributeHistoryRepository.save(candidate);
        }
    }

    private LastfmAttributeHistoryRecord expireOldAndSaveNewValue(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {

        // 'duplicate' variable relates to a temporary fix for attribute_history duplication
        // TODO fix attribute_history duplication
        LocalDate expirationDate = candidate.getValidFrom().minusDays(1);
        LastfmAttributeHistoryRecord duplicate = attributeHistoryRepository.findCurrentValueForCandidate(candidate, expirationDate);
        if (duplicate == null) {
            existing.setValidTill(expirationDate);
            attributeHistoryRepository.saveAndFlush(existing);
            return attributeHistoryRepository.save(candidate);
        } else {
            return existing;
        }
    }

    private static boolean valueHasChanged(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {
        return !(   Objects.equals(existing.getStringValue(),   candidate.getStringValue())
                &&  Objects.equals(existing.getIntValue(),      candidate.getIntValue()));
    }

    private static boolean shouldSaveNonChangedValue(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {
        return isSnapshotAttribute(candidate) && ! belongsToTheSameSnapshot(candidate, existing);
    }

    private static boolean isSnapshotAttribute(LastfmAttributeHistoryRecord candidate) {
        return candidate.getAttribute().getHistoryType() == LastfmAttribute.HistoryType.SNAPSHOT;
    }

    private static boolean belongsToTheSameSnapshot(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {
        return Objects.equals(candidate.getApiCallId(), existing.getApiCallId());
    }

}
