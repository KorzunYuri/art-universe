package yurykorzun.art.universe.music.data.raw.lastfm.common;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static yurykorzun.art.universe.music.data.raw.lastfm.common.utils.TestStringUtils.randomString;

public class EntityCreationHelper {

    private EntityCreationHelper(){
    }

    public static final LastfmApiCallType DEFAULT_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_TAGS;
    public static final LastfmApiCallType DEFAULT_ARTIST_SOURCE_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_ARTISTS;
    public static final LastfmApiCallType DEFAULT_TAG_SOURCE_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_TAGS;

    //  data snapshot

    public static LastfmDataSnapshot createDataSnapshot() {
        return createDataSnapshot(DEFAULT_API_CALL_TYPE);
    }

    public static LastfmDataSnapshot createDataSnapshot(LastfmApiCallType apiCallType) {
        return new LastfmDataSnapshot(apiCallType, LocalDate.now());
    }


    //  API call

    public static LastfmApiCall createApiCall(Consumer<LastfmApiCall.LastfmApiCallBuilder<?, ?>> customizer) {
        // init with default values
        LastfmDataSnapshot snapshot = createDataSnapshot();
        LastfmApiCall.LastfmApiCallBuilder<?, ?> builder = LastfmApiCall.builder()
            .dataSnapshotId(snapshot.getId())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(null)
            .type(DEFAULT_API_CALL_TYPE)
            .dueDttm(TimeUtil.calcDueDttm(1))
            .params(Map.of());
        // apply custom values
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmApiCall createApiCall() {
        return createApiCall(DEFAULT_API_CALL_TYPE);
    }

    public static LastfmApiCall createApiCall(LastfmApiCallType apiCallType) {
        return createApiCall(builder -> builder.type(apiCallType));
    }

    public static LastfmApiCall createApiCall(LastfmApiCallType apiCallType, BaseLastfmEntity scopeEntity) {
        return createApiCall(builder -> builder
            .type(apiCallType)
            .entityType(scopeEntity.getType())
            .entityId(scopeEntity.getId())
        );
    }


    //  API response

    public static LastfmApiResponse createApiResponse(String responseBody, LastfmApiCall apiCall) {
        return LastfmApiResponse.builder()
                .responseBody(responseBody)
                .apiCall(apiCall)
            .build();
    }

    public static LastfmApiResponse createApiResponse(String responseBody, LastfmApiCallType apiCallType) {
        LastfmApiCall apiCall = createApiCall(apiCallType);
        return createApiResponse(responseBody, apiCall);
    }

    public static LastfmApiResponse createApiResponse(String responseBody, LastfmApiCallType apiCallType, BaseLastfmEntity scopeEntity) {
        LastfmApiCall apiCall = createApiCall(apiCallType, scopeEntity);
        return createApiResponse(responseBody, apiCall);
    }

    public LastfmApiResponse createApiResponse(String responseBody) {
        return createApiResponse(responseBody, DEFAULT_API_CALL_TYPE);
    }


    //  tag

    public static LastfmTag createTag() {
        return createTag(createApiCall(DEFAULT_TAG_SOURCE_API_CALL_TYPE));
    }

    public static LastfmTag createTag(LastfmApiCallType sourceApiCallType) {
        return createTag(createApiCall(sourceApiCallType));
    }

    public static LastfmTag createTag(LastfmApiCall sourceApiCall) {
        return createTag(builder -> builder
            .apiCall(sourceApiCall));
    }

    public static LastfmTag createTag(Consumer<LastfmTag.LastfmTagBuilder<?, ?>> customizer) {
        LastfmTag.LastfmTagBuilder<?, ?> builder = LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .apiCall(createApiCall());
        customizer.accept(builder);
        return builder.build();
    }


    //  artist

    public static LastfmArtist createArtist() {
        return createArtist(DEFAULT_ARTIST_SOURCE_API_CALL_TYPE);
    }

    public static LastfmArtist createArtist(LastfmApiCallType sourceApiCallType) {
        return createArtist(builder -> builder
            .apiCall(createApiCall(sourceApiCallType)));
    }

    public static LastfmArtist createArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        LastfmArtist.LastfmArtistBuilder<?, ?> builder = LastfmArtist.builder()
            .name(randomString())
            .apiCall(createApiCall());
        customizer.accept(builder);
        return builder.build();
    }


    //  album

    public static LastfmAlbum createAlbum() {
        return createAlbum(builder -> {});
    }

    public static LastfmAlbum createAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        LastfmAlbum.LastfmAlbumBuilder<?, ?> builder = LastfmAlbum.builder()
            .name(randomString())
            .apiCall(createApiCall());
        customizer.accept(builder);
        return builder.build();
    }


    //  track

    public static LastfmTrack createTrack() {
        return createTrack(randomString(), createApiCall());
    }

    public static LastfmTrack createTrack(String url) {
        return createTrack(url, createApiCall());
    }

    public static LastfmTrack createTrack(String url, LastfmApiCall apiCall) {
        return createTrack(builder -> builder
            .url(url)
            .apiCall(apiCall));
    }

    public static LastfmTrack createTrack(Consumer<LastfmTrack.LastfmTrackBuilder<?, ?>> customizer) {
        LastfmTrack.LastfmTrackBuilder<?, ?> builder = LastfmTrack.builder()
            .name(randomString())
            .mbid(randomString());
        customizer.accept(builder);
        return builder.build();
    }

}
