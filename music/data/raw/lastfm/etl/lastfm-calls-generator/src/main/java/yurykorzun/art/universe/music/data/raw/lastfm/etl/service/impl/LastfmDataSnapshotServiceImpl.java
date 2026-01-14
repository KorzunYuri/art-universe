package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;

import java.time.LocalDate;
import java.util.List;

@Service
public class LastfmDataSnapshotServiceImpl implements LastfmDataSnapshotService {

    private final LastfmDataSnapshotRepository snapshotRepository;

    public LastfmDataSnapshotServiceImpl(LastfmDataSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType) {

        if (apiCallType.getScopeEntityType() != null) {
            throw new IllegalArgumentException(
                    String.format("Snapshot for api call of type %s must be bound to an entity of type %s, but no entity provided",
                            apiCallType,
                            apiCallType.getScopeEntityType()));
        }

        LastfmDataSnapshot snapshot = snapshotRepository.findForApiCallType(apiCallType);
        if (snapshot == null) {
            snapshot = new LastfmDataSnapshot(apiCallType, LocalDate.now());
            snapshot = snapshotRepository.save(snapshot);
        }
        return snapshot;
    }

    @Override
    public LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType, BaseLastfmEntity entity) {

        if (!entity.getClass().equals(apiCallType.getScopeEntityType())) {
            throw new IllegalArgumentException(
                    String.format("Snapshot for api call of type %s must belong to an entity of type %s, provided instead: %s",
                            apiCallType,
                            apiCallType.getScopeEntityType(),
                            entity.getClass()));
        }

        LastfmDataSnapshot snapshot = snapshotRepository.findForApiCallTypeAndEntity(apiCallType, entity);
        if (snapshot == null) {
            snapshot = new LastfmDataSnapshot(apiCallType, LocalDate.now(), entity);
            snapshot = snapshotRepository.save(snapshot);
        }
        return snapshot;
    }

    @Transactional
    @Override
    public void incCreatedCount(long id) {
        snapshotRepository.incCreatedCount(id);
    }

    @Transactional
    @Override
    public void incCreatedCount(List<Long> ids) {
        snapshotRepository.incCreatedCount(ids);
    }

    @Transactional
    @Override
    public void incCreatedCountByNumber(long id, int number) {
        snapshotRepository.incCreatedCountByNumber(id, number);
    }

    @Transactional
    @Override
    public void incCreatedCountByNumber(List<Long> ids, int number) {
        snapshotRepository.incCreatedCountByNumber(ids, number);
    }
}
