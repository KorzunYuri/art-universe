package yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.test.utils.TestStringUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

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
            .dueDttm(calcDueDttm(1))
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
                .url(TestStringUtils.randomString())
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
            .name(TestStringUtils.randomString())
            .url(TestStringUtils.randomString())
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
            .name(TestStringUtils.randomString())
            .url(TestStringUtils.randomString())
            .apiCall(createApiCall());
        customizer.accept(builder);
        return builder.build();
    }


    //  track

    public static LastfmTrack createTrack() {
        return createTrack(TestStringUtils.randomString(), createApiCall());
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
            .apiCall(createApiCall())
            .url(TestStringUtils.randomString())
            .name(TestStringUtils.randomString())
            .mbid(TestStringUtils.randomString());
        customizer.accept(builder);
        return builder.build();
    }

    // Relationship entity creation methods

    public static LastfmArtistTag createArtistTag() {
        return createArtistTag(builder -> {});
    }

    public static LastfmArtistTag createArtistTag(Consumer<LastfmArtistTag.LastfmArtistTagBuilder<?, ?>> customizer) {
        LastfmArtistTag.LastfmArtistTagBuilder<?, ?> builder = LastfmArtistTag.builder()
            .artist(createArtist())
            .tag(createTag())
            .apiCall(createApiCall())
            .usageCount(100);
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmArtistsRelation createArtistsRelation() {
        return createArtistsRelation(builder -> {});
    }

    public static LastfmArtistsRelation createArtistsRelation(Consumer<LastfmArtistsRelation.LastfmArtistsRelationBuilder<?, ?>> customizer) {
        LastfmArtistsRelation.LastfmArtistsRelationBuilder<?, ?> builder = LastfmArtistsRelation.builder()
            .sourceArtist(createArtist())
            .targetArtist(createArtist())
            .apiCall(createApiCall())
            .relationType(LastfmEntityRelationType.SIMILARITY)
            .matchScore(BigDecimal.valueOf(0.85));
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmArtistAlbum createArtistAlbum() {
        return createArtistAlbum(builder -> {});
    }

    public static LastfmArtistAlbum createArtistAlbum(Consumer<LastfmArtistAlbum.LastfmArtistAlbumBuilder<?, ?>> customizer) {
        LastfmArtistAlbum.LastfmArtistAlbumBuilder<?, ?> builder = LastfmArtistAlbum.builder()
            .artist(createArtist())
            .album(createAlbum())
            .apiCall(createApiCall());
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmArtistTrack createArtistTrack() {
        return createArtistTrack(builder -> {});
    }

    public static LastfmArtistTrack createArtistTrack(Consumer<LastfmArtistTrack.LastfmArtistTrackBuilder<?, ?>> customizer) {
        LastfmArtistTrack.LastfmArtistTrackBuilder<?, ?> builder = LastfmArtistTrack.builder()
            .artist(createArtist())
            .track(createTrack())
            .apiCall(createApiCall());
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmAlbumTag createAlbumTag() {
        return createAlbumTag(builder -> {});
    }

    public static LastfmAlbumTag createAlbumTag(Consumer<LastfmAlbumTag.LastfmAlbumTagBuilder<?, ?>> customizer) {
        LastfmAlbumTag.LastfmAlbumTagBuilder<?, ?> builder = LastfmAlbumTag.builder()
            .album(createAlbum())
            .tag(createTag())
            .apiCall(createApiCall())
            .usageCount(50);
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmTrackTag createTrackTag() {
        return createTrackTag(builder -> {});
    }

    public static LastfmTrackTag createTrackTag(Consumer<LastfmTrackTag.LastfmTrackTagBuilder<?, ?>> customizer) {
        LastfmTrackTag.LastfmTrackTagBuilder<?, ?> builder = LastfmTrackTag.builder()
            .track(createTrack())
            .tag(createTag())
            .apiCall(createApiCall())
            .usageCount(25);
        customizer.accept(builder);
        return builder.build();
    }

    public static LastfmAlbumTrack createAlbumTrack() {
        return createAlbumTrack(builder -> {});
    }

    public static LastfmAlbumTrack createAlbumTrack(Consumer<LastfmAlbumTrack.LastfmAlbumTrackBuilder<?, ?>> customizer) {
        LastfmAlbumTrack.LastfmAlbumTrackBuilder<?, ?> builder = LastfmAlbumTrack.builder()
            .album(createAlbum())
            .track(createTrack())
            .apiCall(createApiCall())
            .position(1);
        customizer.accept(builder);
        return builder.build();
    }

    private static Instant calcDueDttm(int dueDays) {
        return Instant.now()
            .plus(Duration.ofDays(dueDays))
            .truncatedTo(ChronoUnit.DAYS)
            .minus(Duration.ofMillis(1));
    }
}
