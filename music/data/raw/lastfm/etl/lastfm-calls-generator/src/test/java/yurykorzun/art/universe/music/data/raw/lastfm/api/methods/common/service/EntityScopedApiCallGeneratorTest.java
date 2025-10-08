package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntityScopedApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;
    @Mock
    private LastfmDataSnapshotService snapshotService;
    @Mock
    private LastfmApiCallEntityService entityService;

    @Test
    void createApiCalls_shouldCreateCallsAndUpdateSnapshots_whenEntitiesFound() {
        // given
        TestableEntityScopedGenerator generator = new TestableEntityScopedGenerator(apiCallService, snapshotService, entityService);
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot();
        
        when(entityService.findAllUnprocessed(any(), any(), any())).thenReturn(List.of(artist));
        when(snapshotService.getOrCreateSnapshotFor(any(LastfmApiCallType.class))).thenReturn(snapshot);
        when(apiCallService.createApiCalls(any())).thenReturn(List.of(1L));

        // when
        generator.createApiCalls();

        // then
        verify(apiCallService).createApiCalls(argThat(requests -> 
            requests.size() == 1 && 
            requests.get(0).getDataSnapshotId() == snapshot.getId()
        ));
        verify(snapshotService).incCreatedCount(List.of(snapshot.getId()));
    }

    @Test
    void createApiCalls_shouldSkipInvalidEntities_whenSomeEntitiesInvalid() {
        // given
        TestableEntityScopedGenerator generator = new TestableEntityScopedGenerator(apiCallService, snapshotService, entityService);
        
        LastfmArtist validArtist = EntityCreationHelper.createArtist(builder -> builder.name("Valid Artist"));
        LastfmArtist invalidArtist = EntityCreationHelper.createArtist(builder -> builder.name(null).mbid(null)); // no name or mbid
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot();
        
        when(entityService.findAllUnprocessed(any(), any(), any())).thenReturn(List.of(validArtist, invalidArtist));
        when(snapshotService.getOrCreateSnapshotFor(any(LastfmApiCallType.class))).thenReturn(snapshot);
        when(apiCallService.createApiCalls(any())).thenReturn(List.of(1L));

        // when
        generator.createApiCalls();

        // then
        verify(apiCallService).createApiCalls(argThat(requests -> requests.size() == 1));
    }

    @Test
    void createApiCalls_shouldDeduplicateEntities_whenDuplicatesFound() {
        // given
        TestableEntityScopedGenerator generator = new TestableEntityScopedGenerator(apiCallService, snapshotService, entityService);
        
        LastfmArtist artist1 = EntityCreationHelper.createArtist(builder -> 
            builder.id(1L).name("Same Artist").listenersCount(1000));
        LastfmArtist artist2 = EntityCreationHelper.createArtist(builder -> 
            builder.id(2L).name("Same Artist").listenersCount(500)); // duplicate with lower priority
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot();
        
        when(entityService.findAllUnprocessed(any(), any(), any())).thenReturn(List.of(artist1, artist2));
        when(snapshotService.getOrCreateSnapshotFor(any(LastfmApiCallType.class))).thenReturn(snapshot);
        when(apiCallService.createApiCalls(any())).thenReturn(List.of(1L));

        // when
        generator.createApiCalls();

        // then
        verify(apiCallService).createApiCalls(argThat(requests -> 
            requests.size() == 1 && 
            requests.get(0).getEntityId() == 1L // higher priority artist
        ));
    }

    @Test
    void generateApiCallCreationRequests_shouldUseTypeLevelSnapshot_whenNotEntityScoped() {
        // given
        TestableEntityScopedGenerator generator = new TestableEntityScopedGenerator(apiCallService, snapshotService, entityService, false);
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot();
        
        when(entityService.findAllUnprocessed(any(), any(), any())).thenReturn(List.of(artist));
        when(snapshotService.getOrCreateSnapshotFor(any(LastfmApiCallType.class))).thenReturn(snapshot);

        // when
        List<LastfmApiCallCreateRequest> result = generator.generateApiCallCreationRequests();

        // then
        verify(snapshotService).getOrCreateSnapshotFor(any(LastfmApiCallType.class));
        verify(snapshotService, never()).getOrCreateSnapshotFor(any(LastfmApiCallType.class), any(LastfmArtist.class));
    }

    @Test
    void generateApiCallCreationRequests_shouldUseEntityLevelSnapshot_whenEntityScoped() {
        // given
        TestableEntityScopedGenerator generator = new TestableEntityScopedGenerator(apiCallService, snapshotService, entityService, true);
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.name("Test Artist"));
        LastfmDataSnapshot snapshot = EntityCreationHelper.createDataSnapshot();
        
        when(entityService.findAllUnprocessed(any(), any(), any())).thenReturn(List.of(artist));
        when(snapshotService.getOrCreateSnapshotFor(any(LastfmApiCallType.class), any(LastfmArtist.class))).thenReturn(snapshot);

        // when
        List<LastfmApiCallCreateRequest> result = generator.generateApiCallCreationRequests();

        // then
        verify(snapshotService).getOrCreateSnapshotFor(any(LastfmApiCallType.class), eq(artist));
        verify(snapshotService, never()).getOrCreateSnapshotFor(any(LastfmApiCallType.class));
    }

    private static class TestableEntityScopedGenerator extends EntityScopedApiCallGenerator<LastfmArtist> {
        private final boolean entityScoped;

        public TestableEntityScopedGenerator(LastfmApiCallService apiCallService, LastfmDataSnapshotService snapshotService, LastfmApiCallEntityService entityService) {
            this(apiCallService, snapshotService, entityService, false);
        }

        public TestableEntityScopedGenerator(LastfmApiCallService apiCallService, LastfmDataSnapshotService snapshotService, LastfmApiCallEntityService entityService, boolean entityScoped) {
            super(apiCallService, snapshotService, entityService);
            this.entityScoped = entityScoped;
        }

        @Override
        public LastfmApiCallType getApiCallType() {
            return LastfmApiCallType.ARTIST_GET_INFO;
        }

        @Override
        protected LastfmEntityType getScopeEntityType() {
            return LastfmEntityType.ARTIST;
        }

        @Override
        protected boolean isApiCallEntityScoped() {
            return entityScoped;
        }

        @Override
        protected int getDueDurationDays() {
            return 7;
        }

        @Override
        protected boolean isValidForApiCall(LastfmArtist artist) {
            return artist.getName() != null || artist.getMbid() != null;
        }

        @Override
        protected boolean hasHigherPriority(LastfmArtist candidate, LastfmArtist existing) {
            if (existing == null) return true;
            return candidate.getListenersCount() != null && 
                   (existing.getListenersCount() == null || candidate.getListenersCount() > existing.getListenersCount());
        }

        @Override
        protected String getApiCallUniqueKey(LastfmArtist entity) {
            return entity.getName() != null ? "name-" + entity.getName() : null;
        }

        @Override
        protected Map<String, String> getCommonApiCallParameters(LastfmArtist artist) {
            return Map.of("artist", artist.getName() != null ? artist.getName() : "");
        }
    }
}
