package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository.LastfmTrackRepository;

import java.util.function.Consumer;

import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.TestStringUtils.randomString;

/**
 * Provides methods for creating entities for testing while maintaining DB consistency. Has two types of methods:
 * <ul>
 *     <li>createForPersistence - create entity that will be persisted later, create and save all the underlying entities</li>
 *     <li>createAndSave - create and save entity and all the underlying entities it depends on</li>
 * </ul>
 */
@Component
@Profile("test")
@Slf4j
public class DbConsistencyHelper {

    private final LastfmDataSnapshotRepository snapshotRepository;
    private final LastfmApiCallRepository apiCallRepository;
    private final LastfmApiResponseRepository apiResponseRepository;
    private final LastfmTagRepository tagRepository;
    private final LastfmArtistRepository artistRepository;
    private final LastfmAlbumRepository albumRepository;
    private final LastfmTrackRepository trackRepository;

    public DbConsistencyHelper(
        LastfmDataSnapshotRepository snapshotRepository,
        LastfmApiCallRepository apiCallRepository,
        LastfmApiResponseRepository apiResponseRepository,
        LastfmTagRepository tagRepository,
        LastfmArtistRepository artistRepository,
        LastfmAlbumRepository albumRepository,
        LastfmTrackRepository trackRepository
    ) {
        this.snapshotRepository = snapshotRepository;
        this.apiCallRepository = apiCallRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.tagRepository = tagRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
    }

    public void cleanup() {
        trackRepository.deleteAll();
        artistRepository.deleteAll();
        tagRepository.deleteAll();
        apiResponseRepository.deleteAll();
        apiCallRepository.deleteAll();
        snapshotRepository.deleteAll();
    }

    public BaseLastfmEntity createAndSaveDummyEntity() {
        return createAndSaveTag();
    }

    //  Data snapshot

    public LastfmDataSnapshot createAndSaveDataSnapshot() {
        return snapshotRepository.save(EntityCreationHelper.createDataSnapshot(EntityCreationHelper.DEFAULT_API_CALL_TYPE));
    }

    public LastfmDataSnapshot createAndSaveDataSnapshot(LastfmApiCallType apiCallType) {
        return snapshotRepository.save(EntityCreationHelper.createDataSnapshot(apiCallType));
    }


    //  API call

    public LastfmApiCall createAndSaveApiCall(Consumer<LastfmApiCall.LastfmApiCallBuilder<?, ?>> customizer) {
        return apiCallRepository.save(EntityCreationHelper.createApiCall(customizer));
    }

    public LastfmApiCall createAndSaveApiCall(LastfmApiCallType apiCallType) {
        LastfmDataSnapshot snapshot = createAndSaveDataSnapshot(apiCallType);
        LastfmApiCall dummyApiCall = EntityCreationHelper.createApiCall(builder -> builder
                .type(apiCallType)
                .dataSnapshotId(snapshot.getId())
        );
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    public LastfmApiCall createAndSaveApiCall(LastfmApiCallType apiCallType, BaseLastfmEntity entity) {
        LastfmDataSnapshot snapshot = createAndSaveDataSnapshot(apiCallType);
        LastfmApiCall dummyApiCall = EntityCreationHelper.createApiCall(builder -> builder
                .type(apiCallType)
                .dataSnapshotId(snapshot.getId())
                .entityType(entity.getType())
                .entityId(entity.getId())
        );
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    public LastfmApiCall createAndSaveApiCall() {
        return createAndSaveApiCall(EntityCreationHelper.DEFAULT_API_CALL_TYPE);
    }


    //  API response

    public LastfmApiResponse createAndSaveApiResponse(String responseString, LastfmApiCallType apiCallType, BaseLastfmEntity scopeEntity) {
        return createAndSaveApiResponse(responseString, createAndSaveApiCall(apiCallType, scopeEntity));
    }

    public LastfmApiResponse createAndSaveApiResponse(String responseString, LastfmApiCallType apiCallType) {
        return createAndSaveApiResponse(responseString, createAndSaveApiCall(apiCallType));
    }

    public LastfmApiResponse createAndSaveApiResponse(String responseString, LastfmApiCall sourceApiCall) {
        LastfmApiResponse apiResponse = EntityCreationHelper.createApiResponse(responseString, sourceApiCall);
        return apiResponseRepository.save(apiResponse);
    }

    //  tag

    public LastfmTag createAndSaveTag() {
        return createAndSaveTag(LastfmApiCallType.TAG_TOP_TAGS);
    }

    public LastfmTag createAndSaveTag(LastfmApiCallType sourceApiCallType) {
        LastfmApiCall dummyApiCall = createAndSaveApiCall(sourceApiCallType);
        LastfmTag tag = EntityCreationHelper.createTag(dummyApiCall);
        return tagRepository.save(tag);
    }


    //  artist

    public LastfmArtist createAndSaveArtist() {
        return createAndSaveArtist(builder -> {});
    }

    public LastfmArtist createAndSaveArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        LastfmArtist artist = createArtistForPersistence(customizer);
        return artistRepository.save(artist);
    }

    public LastfmArtist createArtistForPersistence() {
        return createArtistForPersistence(builder -> {});
    }

    public LastfmArtist createArtistForPersistence(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> apiCallSetter = builder -> builder
            .apiCall(createAndSaveApiCall());
        return EntityCreationHelper.createArtist(customizer.andThen(apiCallSetter));
    }


    //  album

    public LastfmAlbum createAndSaveAlbum() {
        return createAndSaveAlbum(builder -> {});
    }

    public LastfmAlbum createAndSaveAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        LastfmAlbum album = createAlbumForPersistence(customizer);
        return albumRepository.save(album);
    }

    public LastfmAlbum createAlbumForPersistence() {
        return createAlbumForPersistence(builder -> {});
    }

    public LastfmAlbum createAlbumForPersistence(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> apiCallSetter = builder -> builder
            .apiCall(createAndSaveApiCall());
        return EntityCreationHelper.createAlbum(customizer.andThen(apiCallSetter));
    }


    //  track

    public LastfmTrack createTrackForPersistence() {
        return createTrackForPersistence(randomString());
    }

    public LastfmTrack createTrackForPersistence(String url) {
        return createTrackForPersistence(builder -> builder.url(url));
    }

    public LastfmTrack createTrackForPersistence(Consumer<LastfmTrack.LastfmTrackBuilder<?, ?>> customizer) {
        Consumer<LastfmTrack.LastfmTrackBuilder<?, ?>> apiCallSetter = builder -> builder
            .apiCall(createAndSaveApiCall());
        return EntityCreationHelper.createTrack(customizer.andThen(apiCallSetter));
    }
    
    public LastfmTrack createAndSaveTrack() {
        return createAndSaveTrack(builder -> {});
    }
    
    public LastfmTrack createAndSaveTrack(String url) {
        return createAndSaveTrack(builder -> builder.url(url));
    }
    
    public LastfmTrack createAndSaveTrack(Consumer<LastfmTrack.LastfmTrackBuilder<?, ?>> customizer) {
        LastfmTrack track = createTrackForPersistence(customizer);
        return trackRepository.save(track);
    }
}
