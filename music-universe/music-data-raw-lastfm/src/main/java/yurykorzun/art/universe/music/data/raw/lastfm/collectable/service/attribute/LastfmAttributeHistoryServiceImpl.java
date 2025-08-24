package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeHistoryRecordRepository;

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
                return setNewValue(candidate, existing);
            } else {
                if (shouldSaveNonChangedValue(candidate, existing)) {
                    return setNewValue(candidate, existing);
                }
                return null;
            }
        } else {
            return attributeHistoryRepository.save(candidate);
        }
    }

    private LastfmAttributeHistoryRecord setNewValue(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {

        LocalDate expirationDate = candidate.getValidFrom().minusDays(1);
        if (hasDuplicateExpiredRecord(candidate, expirationDate) || hasSameDate(candidate, existing)) {
            return replaceOldValue(candidate, existing);
        } else {
            return expireOldValueAndAddNewValue(candidate, existing, expirationDate);
        }
    }

    private LastfmAttributeHistoryRecord expireOldValueAndAddNewValue(
        LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing, LocalDate expirationDate
    ) {
        existing.setValidTill(expirationDate);
        attributeHistoryRepository.saveAndFlush(existing);
        return attributeHistoryRepository.save(candidate);
    }

    private LastfmAttributeHistoryRecord replaceOldValue(LastfmAttributeHistoryRecord newValue, LastfmAttributeHistoryRecord existingValue) {
        attributeHistoryRepository.delete(existingValue);
        attributeHistoryRepository.flush();
        return attributeHistoryRepository.save(newValue);
    }

    private boolean hasDuplicateExpiredRecord(LastfmAttributeHistoryRecord candidate, LocalDate expirationDate) {
        return attributeHistoryRepository.findCurrentValueForCandidate(candidate, expirationDate) != null;
    }

    private static boolean valueHasChanged(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {
        return !(   Objects.equals(existing.getStringValue(),   candidate.getStringValue())
                &&  Objects.equals(existing.getNumericValue(),      candidate.getNumericValue()));
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

    private static boolean hasSameDate(LastfmAttributeHistoryRecord candidate, LastfmAttributeHistoryRecord existing) {
        return Objects.equals(candidate.getValidFrom(), existing.getValidFrom());
    }

}
