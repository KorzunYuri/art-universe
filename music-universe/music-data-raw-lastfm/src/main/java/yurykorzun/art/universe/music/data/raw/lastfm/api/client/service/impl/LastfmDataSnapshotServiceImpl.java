package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;

import java.util.Date;

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

        LastfmDataSnapshot snapshot = snapshotRepository.findForApiCallType((apiCallType));
        if (snapshot == null) {
            snapshot = new LastfmDataSnapshot(apiCallType, new Date());
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
            snapshot = new LastfmDataSnapshot(apiCallType, new Date(), entity);
            snapshot = snapshotRepository.save(snapshot);
        }
        return snapshot;
    }

    @Transactional
    @Override
    public void incCreatedCount(long id) {
        snapshotRepository.incCompletedCount(id);
    }

    @Transactional
    @Override
    public void incCreatedCountByNumber(long id, int number) {
        snapshotRepository.incCreatedCountByNumber(id, number);
    }

    @Transactional
    @Override
    public void incCompletedCount(long id) {
        snapshotRepository.incCompletedCount(id);
    }

    @Transactional
    @Override
    public void incCompletedCountByNumber(long id, int number) {
        snapshotRepository.incCompletedCountByNumber(id, number);
    }

    @Transactional
    @Override
    public void incParsedCount(long id) {
        snapshotRepository.incParsedCount(id);
    }

    @Transactional
    @Override
    public void incParsedCountByNumber(long id, int number) {
        snapshotRepository.incParsedCountByNumber(id, number);
    }

}
