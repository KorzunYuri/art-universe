package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Test
    void getOrCreateSnapshotFor_type_shouldThrow_whenTypeHasScopeEntityType() {
        // TAG_TOP_TRACKS has scopeEntityType = LastfmTag.class → must provide entity
        assertThatThrownBy(() -> service.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getOrCreateSnapshotFor_typeAndEntity_shouldReturnExisting_whenSnapshotExists() {
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("rock"));
        LastfmDataSnapshot existing = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.TAG_TOP_TRACKS);
        when(snapshotRepository.findForApiCallTypeAndEntity(LastfmApiCallType.TAG_TOP_TRACKS, tag)).thenReturn(existing);

        LastfmDataSnapshot result = service.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS, tag);

        assertEquals(existing, result);
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    void getOrCreateSnapshotFor_typeAndEntity_shouldCreate_whenSnapshotDoesNotExist() {
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("jazz"));
        LastfmDataSnapshot newSnapshot = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.TAG_TOP_TRACKS);
        when(snapshotRepository.findForApiCallTypeAndEntity(LastfmApiCallType.TAG_TOP_TRACKS, tag)).thenReturn(null);
        when(snapshotRepository.save(any(LastfmDataSnapshot.class))).thenReturn(newSnapshot);

        LastfmDataSnapshot result = service.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS, tag);

        assertEquals(newSnapshot, result);
        verify(snapshotRepository).save(any(LastfmDataSnapshot.class));
    }

    @Test
    void getOrCreateSnapshotFor_typeAndEntity_shouldThrow_whenEntityClassMismatch() {
        // TAG_TOP_TRACKS expects LastfmTag but we pass a mock of a different type
        yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist artist =
            mock(yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist.class);

        assertThatThrownBy(() -> service.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS, artist))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void incCreatedCount_list_shouldCallRepository() {
        List<Long> ids = List.of(1L, 2L, 3L);
        service.incCreatedCount(ids);
        verify(snapshotRepository).incCreatedCount(ids);
    }

    @Test
    void incCreatedCountByNumber_single_shouldCallRepository() {
        service.incCreatedCountByNumber(5L, 3);
        verify(snapshotRepository).incCreatedCountByNumber(5L, 3);
    }
}
