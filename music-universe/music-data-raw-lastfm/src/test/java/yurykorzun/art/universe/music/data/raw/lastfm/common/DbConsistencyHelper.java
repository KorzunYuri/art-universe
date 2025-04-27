package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * While testing persistence/service layer, it's often needed to create related entities to maintain db consistency.
 * Current class provides methods to do this.
 * TODO consider splitting create and saveAndCreate logic
 */
@Component
@Profile("test")
@Slf4j
public class DbConsistencyHelper {

    private final LastfmDataSnapshotRepository snapshotRepository;
    private final LastfmApiCallRepository apiCallRepository;
    private final LastfmTagRepository tagRepository;
    private final LastfmArtistRepository artistRepository;
    private final LastfmAlbumRepository albumRepository;

    public static final LastfmApiCallType DUMMY_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_TAGS;

    public DbConsistencyHelper(
        LastfmDataSnapshotRepository snapshotRepository,
        LastfmApiCallRepository apiCallRepository,
        LastfmTagRepository tagRepository,
        LastfmArtistRepository artistRepository,
        LastfmAlbumRepository albumRepository
    ) {
        this.snapshotRepository = snapshotRepository;
        this.apiCallRepository = apiCallRepository;
        this.tagRepository = tagRepository;
        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
    }

    public void cleanup() {
        artistRepository.deleteAll();
        tagRepository.deleteAll();
        apiCallRepository.deleteAll();
        snapshotRepository.deleteAll();
    }

    private String randomString() {
        return UUID.randomUUID().toString();
    }

    public LastfmDataSnapshot createDummyDataSnapshot(LastfmApiCallType apiCallType) {
        return snapshotRepository.save(new LastfmDataSnapshot(apiCallType, LocalDate.now()));
    }

    public LastfmDataSnapshot createDummyDataSnapshot() {
        return createDummyDataSnapshot(DUMMY_API_CALL_TYPE);
    }

    public LastfmApiCall createAndSaveApiCall(Consumer<LastfmApiCall.LastfmApiCallBuilder<?, ?>> customizer) {
        return apiCallRepository.save(createApiCall(customizer));
    }

    public LastfmApiCall createApiCall(Consumer<LastfmApiCall.LastfmApiCallBuilder<?, ?>> customizer) {
        LastfmApiCall.LastfmApiCallBuilder<?, ?> builder = LastfmApiCall.builder()
                .dataSnapshotId(createDummyDataSnapshot().getId())
                .entityType(LastfmEntityType.ARTIST)
                .entityId(null)
                .type(DUMMY_API_CALL_TYPE)
                .dueDttm(TimeUtil.calcDueDttm(1))
                .params(Map.of());
        customizer.accept(builder);
        return builder.build();
    }

    public LastfmApiCall createDummyApiCall(LastfmApiCallType apiCallType) {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot(apiCallType);
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(apiCallType)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
            .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    public LastfmApiCall createDummyApiCall(LastfmApiCallType apiCallType, BaseLastfmEntity entity) {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot(apiCallType);
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(apiCallType)
                .dataSnapshotId(snapshot.getId())
                .entityType(entity.getType())
                .entityId(entity.getId())
                .dueDttm(Instant.now())
            .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    public LastfmApiCall createDummyApiCall() {
        return createDummyApiCall(DUMMY_API_CALL_TYPE);
    }

    public LastfmApiResponse createDummyApiResponse(String responseBody, LastfmApiCallType apiCallType) {
        LastfmApiCall apiCall = createDummyApiCall(apiCallType);
        return LastfmApiResponse.builder()
                .responseBody(responseBody)
                .apiCall(apiCall)
            .build();
    }

    public LastfmApiResponse createDummyApiResponse(String responseBody, LastfmApiCallType apiCallType, BaseLastfmEntity scopeEntity) {
        LastfmApiCall apiCall = createDummyApiCall(apiCallType, scopeEntity);
        return LastfmApiResponse.builder()
                .responseBody(responseBody)
                .apiCall(apiCall)
            .build();
    }

    public LastfmApiResponse createDummyApiResponse(String responseBody) {
        return createDummyApiResponse(responseBody, DUMMY_API_CALL_TYPE);
    }

    public BaseLastfmEntity createDummyEntity() {
        return createAndSaveTag();
    }

    public BaseLastfmEntity createDummyEntity(LastfmApiCallType sourceApiCallType) {
        return createAndSaveTag(sourceApiCallType);
    }

    private static LastfmTag createTag(LastfmApiCall dummyApiCall) {
        return LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .apiCall(dummyApiCall)
            .build();
    }

    public LastfmTag createAndSaveTag() {
        return createAndSaveTag(LastfmApiCallType.TAG_TOP_TAGS);
    }

    public LastfmTag createAndSaveTag(LastfmApiCallType sourceApiCallType) {
        LastfmApiCall dummyApiCall = createDummyApiCall(sourceApiCallType);
        LastfmTag tag = createTag(dummyApiCall);
        return tagRepository.save(tag);
    }

    public LastfmArtist createArtist() {
        return createArtist(builder -> {});
    }

    public LastfmArtist createArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        LastfmArtist.LastfmArtistBuilder<?, ?> builder = LastfmArtist.builder()
            .name(randomString())
            .apiCall(createDummyApiCall());
        customizer.accept(builder);
        return builder.build();
    }

    public LastfmArtist createAndSaveArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        return artistRepository.save(createArtist(customizer));
    }

    public LastfmArtist createAndSaveArtist() {
        return createAndSaveArtist(builder -> {});
    }

    public LastfmAlbum createAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        LastfmAlbum.LastfmAlbumBuilder<?, ?> builder = LastfmAlbum.builder()
            .name(randomString())
            .apiCall(createDummyApiCall());
        customizer.accept(builder);
        return builder.build();
    }

    public LastfmAlbum createAlbum() {
        return createAlbum(builder -> {});
    }

    public LastfmAlbum createAndSaveAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        return albumRepository.save(createAlbum(customizer));
    }

    public LastfmAlbum createAndSaveAlbum() {
        return createAndSaveAlbum(builder -> {});
    }

    public LastfmTrack createTrack(String url, LastfmApiCall apiCall) {
        return LastfmTrack.builder()
                .url(   url)
                .name(  randomString())
                .mbid(  randomString())
                .duration(100)
                .streamable(true)
                .apiCall(apiCall)
            .build();
    }

    public LastfmTrack createTrack(String url) {
        return createTrack(url, createDummyApiCall());
    }

    public LastfmTrack createTrack() {
        return createTrack(randomString(), createDummyApiCall());
    }
}
