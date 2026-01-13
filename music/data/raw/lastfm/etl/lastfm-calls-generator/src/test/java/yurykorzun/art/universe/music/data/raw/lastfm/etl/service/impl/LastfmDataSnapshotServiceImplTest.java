package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmDataSnapshotServiceImplTest {

    @Mock
    private LastfmDataSnapshotRepository snapshotRepository;

    @InjectMocks
    private LastfmDataSnapshotServiceImpl service;

    @Test
    void getOrCreateSnapshotFor_shouldReturnExistingSnapshot_whenSnapshotExists() {
        // given
        LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_TAGS;
        LastfmDataSnapshot existingSnapshot = EntityCreationHelper.createDataSnapshot();
        when(snapshotRepository.findForApiCallType(apiCallType)).thenReturn(existingSnapshot);

        // when
        LastfmDataSnapshot result = service.getOrCreateSnapshotFor(apiCallType);

        // then
        assertEquals(existingSnapshot, result);
        verify(snapshotRepository).findForApiCallType(apiCallType);
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void getOrCreateSnapshotFor_shouldCreateNewSnapshot_whenSnapshotDoesNotExist() {
        // given
        LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_TAGS;
        LastfmDataSnapshot newSnapshot = EntityCreationHelper.createDataSnapshot();
        when(snapshotRepository.findForApiCallType(apiCallType)).thenReturn(null);
        when(snapshotRepository.save(any(LastfmDataSnapshot.class))).thenReturn(newSnapshot);

        // when
        LastfmDataSnapshot result = service.getOrCreateSnapshotFor(apiCallType);

        // then
        assertEquals(newSnapshot, result);
        verify(snapshotRepository).findForApiCallType(apiCallType);
        verify(snapshotRepository).save(any(LastfmDataSnapshot.class));
    }

    @Test
    void incCreatedCount_shouldCallRepository() {
        // given
        long id = 1L;

        // when
        service.incCreatedCount(id);

        // then
        verify(snapshotRepository).incCreatedCount(id);
    }

    @Test
    void incCreatedCountByNumber_shouldCallRepository() {
        // given
        List<Long> ids = List.of(1L, 2L);
        int number = 5;

        // when
        service.incCreatedCountByNumber(ids, number);

        // then
        verify(snapshotRepository).incCreatedCountByNumber(ids, number);
    }
}
