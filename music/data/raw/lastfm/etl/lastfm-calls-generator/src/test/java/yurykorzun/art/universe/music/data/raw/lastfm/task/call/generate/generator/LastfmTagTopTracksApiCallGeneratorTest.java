package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmTagTopTracksApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmAttributeSnapshotService attributeSnapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;
    @Mock
    private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private LastfmTagTopTracksApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnCorrectType() {
        // when
        LastfmApiCallType result = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.TAG_TOP_TRACKS, result);
    }

    @Test
    void getDueDurationDays_shouldReturnConfiguredValue() {
        // given
        int expectedDays = 7;
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TRACKS)).thenReturn(expectedDays);

        // when
        int result = generator.getDueDurationDays();

        // then
        assertEquals(expectedDays, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldCreateOnePageRequest_whenTagHasLowUsage() {
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("rock").usageCount(500));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.TAG_TOP_TRACKS);
        when(entityService.findAllUnprocessed(eq(LastfmEntityType.TAG), eq(LastfmApiCallType.TAG_TOP_TRACKS), any(LastfmApiCallEntityQueryConfig.class)))
            .thenReturn(List.of(tag));
        when(snapshotService.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS, tag)).thenReturn(snapshot);
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TRACKS)).thenReturn(7);
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.USAGE_TO_PAGE_RATIO_TAG_TOP_TRACKS)).thenReturn(1000);

        generator.createApiCalls();

        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldCreateMultiplePageRequests_whenTagHasHighUsage() {
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("pop").usageCount(3000));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.TAG_TOP_TRACKS);
        when(entityService.findAllUnprocessed(eq(LastfmEntityType.TAG), eq(LastfmApiCallType.TAG_TOP_TRACKS), any(LastfmApiCallEntityQueryConfig.class)))
            .thenReturn(List.of(tag));
        when(snapshotService.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS, tag)).thenReturn(snapshot);
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TRACKS)).thenReturn(7);
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.USAGE_TO_PAGE_RATIO_TAG_TOP_TRACKS)).thenReturn(1000);

        generator.createApiCalls();

        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        assertThat(captor.getValue()).hasSize(3); // max(1, 3000/1000) = 3
    }

    @Test
    @SuppressWarnings("unchecked")
    void createApiCalls_shouldCreateOnePageRequest_whenTagUsageCountIsNull() {
        LastfmTag tag = EntityCreationHelper.createTag(b -> b.name("metal").usageCount(null));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot(LastfmApiCallType.TAG_TOP_TRACKS);
        when(entityService.findAllUnprocessed(eq(LastfmEntityType.TAG), eq(LastfmApiCallType.TAG_TOP_TRACKS), any(LastfmApiCallEntityQueryConfig.class)))
            .thenReturn(List.of(tag));
        when(snapshotService.getOrCreateSnapshotFor(LastfmApiCallType.TAG_TOP_TRACKS, tag)).thenReturn(snapshot);
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TRACKS)).thenReturn(7);
        when(configPropertyHolder.getInt(LastfmGeneratorProperty.USAGE_TO_PAGE_RATIO_TAG_TOP_TRACKS)).thenReturn(1000);

        generator.createApiCalls();

        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        assertThat(captor.getValue()).hasSize(1); // max(1, 0/1000) = 1
    }
}
