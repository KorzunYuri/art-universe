package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmAttributeSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAttributeSnapshotServiceImplTest {

    @Mock private LastfmAttributeSnapshotRepository attributeSnapshotRepository;
    @InjectMocks private LastfmAttributeSnapshotServiceImpl service;

    private static final LastfmAttribute ATTRIBUTE = LastfmAttribute.USAGE_COUNT;
    private static final LastfmEntityType ENTITY_TYPE = LastfmEntityType.TAG;

    // ── getOrCreateForEntityType ──────────────────────────────────────────────

    @Test
    void getOrCreateForEntityType_shouldCreateAndSave_whenNoExistingSnapshot() {
        LastfmDataSnapshot snapshot = mock(LastfmDataSnapshot.class);
        when(snapshot.getId()).thenReturn(1L);
        when(attributeSnapshotRepository.findTypeLevelSnapshot(ATTRIBUTE, ENTITY_TYPE)).thenReturn(null);
        LastfmAttributeSnapshot saved = mock(LastfmAttributeSnapshot.class);
        when(attributeSnapshotRepository.save(any())).thenReturn(saved);

        LastfmAttributeSnapshot result = service.getOrCreateForEntityType(snapshot, ENTITY_TYPE, ATTRIBUTE);

        assertThat(result).isSameAs(saved);
        verify(attributeSnapshotRepository).save(any(LastfmAttributeSnapshot.class));
    }

    @Test
    void getOrCreateForEntityType_shouldReturnWithoutSaving_whenCurrentSnapshotUnchanged() {
        LastfmDataSnapshot snapshot = mock(LastfmDataSnapshot.class);
        when(snapshot.getId()).thenReturn(5L);
        LastfmAttributeSnapshot existing = mock(LastfmAttributeSnapshot.class);
        when(existing.getCurrentSnapshotId()).thenReturn(5L);
        when(attributeSnapshotRepository.findTypeLevelSnapshot(ATTRIBUTE, ENTITY_TYPE)).thenReturn(existing);

        LastfmAttributeSnapshot result = service.getOrCreateForEntityType(snapshot, ENTITY_TYPE, ATTRIBUTE);

        assertThat(result).isSameAs(existing);
        verify(attributeSnapshotRepository, never()).save(any());
    }

    @Test
    void getOrCreateForEntityType_shouldUpdateAndSave_whenSnapshotIdChanged() {
        LastfmDataSnapshot snapshot = mock(LastfmDataSnapshot.class);
        when(snapshot.getId()).thenReturn(10L);
        LastfmAttributeSnapshot existing = mock(LastfmAttributeSnapshot.class);
        when(existing.getCurrentSnapshotId()).thenReturn(5L);
        when(attributeSnapshotRepository.findTypeLevelSnapshot(ATTRIBUTE, ENTITY_TYPE)).thenReturn(existing);
        when(attributeSnapshotRepository.save(existing)).thenReturn(existing);

        service.getOrCreateForEntityType(snapshot, ENTITY_TYPE, ATTRIBUTE);

        verify(existing).setNewSnapshot(snapshot);
        verify(attributeSnapshotRepository).save(existing);
    }

    // ── getOrCreateForEntity ──────────────────────────────────────────────────

    @Test
    void getOrCreateForEntity_shouldCreateAndSave_whenNoExistingSnapshot() {
        LastfmDataSnapshot snapshot = mock(LastfmDataSnapshot.class);
        when(snapshot.getId()).thenReturn(1L);
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("rock"));
        when(attributeSnapshotRepository.findEntityLevelSnapshot(ATTRIBUTE, ENTITY_TYPE, tag)).thenReturn(null);
        LastfmAttributeSnapshot saved = mock(LastfmAttributeSnapshot.class);
        when(attributeSnapshotRepository.save(any())).thenReturn(saved);

        LastfmAttributeSnapshot result = service.getOrCreateForEntity(snapshot, ENTITY_TYPE, ATTRIBUTE, tag);

        assertThat(result).isSameAs(saved);
        verify(attributeSnapshotRepository).save(any(LastfmAttributeSnapshot.class));
    }

    @Test
    void getOrCreateForEntity_shouldReturnWithoutSaving_whenCurrentSnapshotUnchanged() {
        LastfmDataSnapshot snapshot = mock(LastfmDataSnapshot.class);
        when(snapshot.getId()).thenReturn(7L);
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("jazz"));
        LastfmAttributeSnapshot existing = mock(LastfmAttributeSnapshot.class);
        when(existing.getCurrentSnapshotId()).thenReturn(7L);
        when(attributeSnapshotRepository.findEntityLevelSnapshot(ATTRIBUTE, ENTITY_TYPE, tag)).thenReturn(existing);

        LastfmAttributeSnapshot result = service.getOrCreateForEntity(snapshot, ENTITY_TYPE, ATTRIBUTE, tag);

        assertThat(result).isSameAs(existing);
        verify(attributeSnapshotRepository, never()).save(any());
    }

    @Test
    void getOrCreateForEntity_shouldUpdateAndSave_whenSnapshotIdChanged() {
        LastfmDataSnapshot snapshot = mock(LastfmDataSnapshot.class);
        when(snapshot.getId()).thenReturn(20L);
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("pop"));
        LastfmAttributeSnapshot existing = mock(LastfmAttributeSnapshot.class);
        when(existing.getCurrentSnapshotId()).thenReturn(15L);
        when(attributeSnapshotRepository.findEntityLevelSnapshot(ATTRIBUTE, ENTITY_TYPE, tag)).thenReturn(existing);
        when(attributeSnapshotRepository.save(existing)).thenReturn(existing);

        service.getOrCreateForEntity(snapshot, ENTITY_TYPE, ATTRIBUTE, tag);

        verify(existing).setNewSnapshot(snapshot);
        verify(attributeSnapshotRepository).save(existing);
    }
}
