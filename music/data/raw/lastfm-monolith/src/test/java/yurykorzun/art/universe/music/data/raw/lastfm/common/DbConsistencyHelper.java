package yurykorzun.art.universe.music.data.raw.lastfm.common;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.BlacklistedEntityUrlRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTrackRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.*;

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
    
    // Relationship repositories
    private final LastfmArtistTagRepository artistTagRepository;
    private final LastfmArtistsRelationRepository artistsRelationRepository;
    private final LastfmArtistAlbumRepository artistAlbumRepository;
    private final LastfmArtistTrackRepository artistTrackRepository;
    private final LastfmAlbumTagRepository albumTagRepository;
    private final LastfmTrackTagRepository trackTagRepository;
    private final LastfmAlbumTrackRepository albumTrackRepository;
    
    private final EntityManager entityManager;
    private final BlacklistedEntityUrlRepository blacklistedEntityUrlRepository;

    public DbConsistencyHelper(
        LastfmDataSnapshotRepository snapshotRepository,
        LastfmApiCallRepository apiCallRepository,
        LastfmApiResponseRepository apiResponseRepository,
        LastfmArtistRepository artistRepository,
        LastfmAlbumRepository albumRepository,
        LastfmTrackRepository trackRepository,
        LastfmTagRepository tagRepository,
        LastfmArtistsRelationRepository artistsRelationRepository,
        LastfmArtistAlbumRepository artistAlbumRepository,
        LastfmArtistTrackRepository artistTrackRepository,
        LastfmArtistTagRepository artistTagRepository,
        LastfmAlbumTrackRepository albumTrackRepository,
        LastfmAlbumTagRepository albumTagRepository,
        LastfmTrackTagRepository trackTagRepository,
        EntityManager entityManager,
        BlacklistedEntityUrlRepository blacklistedEntityUrlRepository
    ) {
        this.snapshotRepository = snapshotRepository;
        this.apiCallRepository = apiCallRepository;
        this.apiResponseRepository = apiResponseRepository;

        this.artistRepository = artistRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
        this.tagRepository = tagRepository;

        this.artistsRelationRepository = artistsRelationRepository;
        this.artistAlbumRepository = artistAlbumRepository;
        this.artistTrackRepository = artistTrackRepository;
        this.artistTagRepository = artistTagRepository;
        this.albumTrackRepository = albumTrackRepository;
        this.albumTagRepository = albumTagRepository;
        this.trackTagRepository = trackTagRepository;

        this.entityManager = entityManager;
        this.blacklistedEntityUrlRepository = blacklistedEntityUrlRepository;
    }

    public void cleanup() {
        entityManager.clear();
        entityManager.flush();

        trackRepository.deleteAll();
        artistRepository.deleteAll();
        tagRepository.deleteAll();
        apiResponseRepository.deleteAll();
        apiCallRepository.deleteAll();
        snapshotRepository.deleteAll();

        artistsRelationRepository.deleteAll();
        artistAlbumRepository.deleteAll();
        artistTrackRepository.deleteAll();
        artistTagRepository.deleteAll();
        albumTrackRepository.deleteAll();
        albumTagRepository.deleteAll();
        trackTagRepository.deleteAll();

        blacklistedEntityUrlRepository.deleteAll();
    }

    public void flush() {
        entityManager.flush();
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
        // First create and save a data snapshot
        LastfmDataSnapshot snapshot = createAndSaveDataSnapshot(EntityCreationHelper.DEFAULT_API_CALL_TYPE);
        
        // Create a new customizer that sets the dataSnapshotId to the saved snapshot's ID
        Consumer<LastfmApiCall.LastfmApiCallBuilder<?, ?>> wrappedCustomizer = builder -> {
            builder.dataSnapshotId(snapshot.getId());
            customizer.accept(builder);
        };
        
        // Create the API call with the wrapped customizer and save it
        LastfmApiCall apiCall = EntityCreationHelper.createApiCall(wrappedCustomizer);
        return apiCallRepository.save(apiCall);
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
    
    public LastfmTag createAndSaveTag(Consumer<LastfmTag.LastfmTagBuilder<?, ?>> customizer) {
        LastfmApiCall dummyApiCall = createAndSaveApiCall();
        Consumer<LastfmTag.LastfmTagBuilder<?, ?>> apiCallSetter = builder -> builder
            .apiCall(dummyApiCall);
        LastfmTag tag = EntityCreationHelper.createTag(apiCallSetter.andThen(customizer));
        return tagRepository.save(tag);
    }


    //  artist

    public LastfmArtist createArtistForPersistence() {
        return createArtistForPersistence(builder -> {});
    }

    public LastfmArtist createArtistForPersistence(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> apiCallSetter = builder -> builder
            .apiCall(createAndSaveApiCall());
        return EntityCreationHelper.createArtist(customizer.andThen(apiCallSetter));
    }

    public LastfmArtist createAndSaveArtist() {
        return createAndSaveArtist(builder -> {});
    }

    public LastfmArtist createAndSaveArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?, ?>> customizer) {
        LastfmArtist artist = createArtistForPersistence(customizer);
        return artistRepository.save(artist);
    }


    //  album

    public LastfmAlbum createAlbumForPersistence() {
        return createAlbumForPersistence(builder -> {});
    }

    public LastfmAlbum createAlbumForPersistence(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> apiCallSetter = builder -> builder
            .apiCall(createAndSaveApiCall());
        return EntityCreationHelper.createAlbum(customizer.andThen(apiCallSetter));
    }

    public LastfmAlbum createAndSaveAlbum() {
        return createAndSaveAlbum(builder -> {});
    }

    public LastfmAlbum createAndSaveAlbum(Consumer<LastfmAlbum.LastfmAlbumBuilder<?, ?>> customizer) {
        LastfmAlbum album = createAlbumForPersistence(customizer);
        return albumRepository.save(album);
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

    // ArtistTag relationship

    public LastfmArtistTag createArtistTagForPersistence() {
        return createArtistTagForPersistence(builder -> {});
    }

    public LastfmArtistTag createArtistTagForPersistence(Consumer<LastfmArtistTag.LastfmArtistTagBuilder<?, ?>> customizer) {
        LastfmArtist artist = createAndSaveArtist();
        LastfmTag tag = createAndSaveTag();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();
        
        Consumer<LastfmArtistTag.LastfmArtistTagBuilder<?, ?>> defaultSetter = builder -> builder
            .artist(artist)
            .tag(tag)
            .apiCall(apiCall);
        
        return EntityCreationHelper.createArtistTag(defaultSetter.andThen(customizer));
    }

    public LastfmArtistTag createAndSaveArtistTag() {
        return createAndSaveArtistTag(builder -> {});
    }

    public LastfmArtistTag createAndSaveArtistTag(Consumer<LastfmArtistTag.LastfmArtistTagBuilder<?, ?>> customizer) {
        LastfmArtistTag artistTag = createArtistTagForPersistence(customizer);
        return artistTagRepository.save(artistTag);
    }

    public LastfmArtistTag createAndSaveArtistTag(LastfmArtist artist, LastfmTag tag) {
        return createAndSaveArtistTag(builder -> builder
            .artist(artist)
            .tag(tag));
    }

    // ArtistsRelation relationship

    public LastfmArtistsRelation createArtistsRelationForPersistence() {
        return createArtistsRelationForPersistence(builder -> {});
    }

    public LastfmArtistsRelation createArtistsRelationForPersistence(Consumer<LastfmArtistsRelation.LastfmArtistsRelationBuilder<?, ?>> customizer) {
        LastfmArtist sourceArtist = createAndSaveArtist();
        LastfmArtist targetArtist = createAndSaveArtist();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();

        Consumer<LastfmArtistsRelation.LastfmArtistsRelationBuilder<?, ?>> defaultSetter = builder -> builder
            .sourceArtist(sourceArtist)
            .targetArtist(targetArtist)
            .apiCall(apiCall)
            .relationType(LastfmEntityRelationType.SIMILARITY);
        
        return EntityCreationHelper.createArtistsRelation(defaultSetter.andThen(customizer));
    }

    public LastfmArtistsRelation createAndSaveArtistsRelation() {
        return createAndSaveArtistsRelation(builder -> {});
    }

    public LastfmArtistsRelation createAndSaveArtistsRelation(Consumer<LastfmArtistsRelation.LastfmArtistsRelationBuilder<?, ?>> customizer) {
        LastfmArtistsRelation artistsRelation = createArtistsRelationForPersistence(customizer);
        return artistsRelationRepository.save(artistsRelation);
    }

    public LastfmArtistsRelation createAndSaveArtistsRelation(LastfmArtist sourceArtist, LastfmArtist targetArtist) {
        return createAndSaveArtistsRelation(builder -> builder
            .sourceArtist(sourceArtist)
            .targetArtist(targetArtist)
            .relationType(LastfmEntityRelationType.SIMILARITY));
    }

    // ArtistAlbum relationship

    public LastfmArtistAlbum createArtistAlbumForPersistence() {
        return createArtistAlbumForPersistence(builder -> {});
    }

    public LastfmArtistAlbum createArtistAlbumForPersistence(Consumer<LastfmArtistAlbum.LastfmArtistAlbumBuilder<?, ?>> customizer) {
        LastfmArtist artist = createAndSaveArtist();
        LastfmAlbum album = createAndSaveAlbum();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();
        
        Consumer<LastfmArtistAlbum.LastfmArtistAlbumBuilder<?, ?>> defaultSetter = builder -> builder
            .artist(artist)
            .album(album)
            .apiCall(apiCall);
        return EntityCreationHelper.createArtistAlbum(defaultSetter.andThen(customizer));
    }

    public LastfmArtistAlbum createAndSaveArtistAlbum() {
        return createAndSaveArtistAlbum(builder -> {});
    }

    public LastfmArtistAlbum createAndSaveArtistAlbum(Consumer<LastfmArtistAlbum.LastfmArtistAlbumBuilder<?, ?>> customizer) {
        LastfmArtistAlbum artistAlbum = createArtistAlbumForPersistence(customizer);
        return artistAlbumRepository.save(artistAlbum);
    }

    public LastfmArtistAlbum createAndSaveArtistAlbum(LastfmArtist artist, LastfmAlbum album) {
        return createAndSaveArtistAlbum(builder -> builder
            .artist(artist)
            .album(album));
    }

    // ArtistTrack relationship

    public LastfmArtistTrack createArtistTrackForPersistence() {
        return createArtistTrackForPersistence(builder -> {});
    }

    public LastfmArtistTrack createArtistTrackForPersistence(Consumer<LastfmArtistTrack.LastfmArtistTrackBuilder<?, ?>> customizer) {
        LastfmArtist artist = createAndSaveArtist();
        LastfmTrack track = createAndSaveTrack();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();
        
        Consumer<LastfmArtistTrack.LastfmArtistTrackBuilder<?, ?>> defaultSetter = builder -> builder
            .artist(artist)
            .track(track)
            .apiCall(apiCall);
        return EntityCreationHelper.createArtistTrack(defaultSetter.andThen(customizer));
    }

    public LastfmArtistTrack createAndSaveArtistTrack() {
        return createAndSaveArtistTrack(builder -> {});
    }

    public LastfmArtistTrack createAndSaveArtistTrack(Consumer<LastfmArtistTrack.LastfmArtistTrackBuilder<?, ?>> customizer) {
        LastfmArtistTrack artistTrack = createArtistTrackForPersistence(customizer);
        return artistTrackRepository.save(artistTrack);
    }

    public LastfmArtistTrack createAndSaveArtistTrack(LastfmArtist artist, LastfmTrack track) {
        return createAndSaveArtistTrack(builder -> builder
            .artist(artist)
            .track(track));
    }

    // AlbumTag relationship

    public LastfmAlbumTag createAlbumTagForPersistence() {
        return createAlbumTagForPersistence(builder -> {});
    }

    public LastfmAlbumTag createAlbumTagForPersistence(Consumer<LastfmAlbumTag.LastfmAlbumTagBuilder<?, ?>> customizer) {
        LastfmAlbum album = createAndSaveAlbum();
        LastfmTag tag = createAndSaveTag();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();
        
        Consumer<LastfmAlbumTag.LastfmAlbumTagBuilder<?, ?>> defaultSetter = builder -> builder
            .album(album)
            .tag(tag)
            .apiCall(apiCall)
            .usageCount(50);
        return EntityCreationHelper.createAlbumTag(defaultSetter.andThen(customizer));
    }

    public LastfmAlbumTag createAndSaveAlbumTag() {
        return createAndSaveAlbumTag(builder -> {});
    }

    public LastfmAlbumTag createAndSaveAlbumTag(Consumer<LastfmAlbumTag.LastfmAlbumTagBuilder<?, ?>> customizer) {
        LastfmAlbumTag albumTag = createAlbumTagForPersistence(customizer);
        return albumTagRepository.save(albumTag);
    }

    public LastfmAlbumTag createAndSaveAlbumTag(LastfmAlbum album, LastfmTag tag) {
        return createAndSaveAlbumTag(builder -> builder
            .album(album)
            .tag(tag)
            .usageCount(50));
    }

    // TrackTag relationship

    public LastfmTrackTag createTrackTagForPersistence() {
        return createTrackTagForPersistence(builder -> {});
    }

    public LastfmTrackTag createTrackTagForPersistence(Consumer<LastfmTrackTag.LastfmTrackTagBuilder<?, ?>> customizer) {
        LastfmTrack track = createAndSaveTrack();
        LastfmTag tag = createAndSaveTag();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();
        
        Consumer<LastfmTrackTag.LastfmTrackTagBuilder<?, ?>> defaultSetter = builder -> builder
            .track(track)
            .tag(tag)
            .apiCall(apiCall)
            .usageCount(25);
        return EntityCreationHelper.createTrackTag(defaultSetter.andThen(customizer));
    }

    public LastfmTrackTag createAndSaveTrackTag() {
        return createAndSaveTrackTag(builder -> {});
    }

    public LastfmTrackTag createAndSaveTrackTag(Consumer<LastfmTrackTag.LastfmTrackTagBuilder<?, ?>> customizer) {
        LastfmTrackTag trackTag = createTrackTagForPersistence(customizer);
        return trackTagRepository.save(trackTag);
    }

    public LastfmTrackTag createAndSaveTrackTag(LastfmTrack track, LastfmTag tag) {
        return createAndSaveTrackTag(builder -> builder
            .track(track)
            .tag(tag)
            .usageCount(25));
    }

    // AlbumTrack relationship

    public LastfmAlbumTrack createAlbumTrackForPersistence() {
        return createAlbumTrackForPersistence(builder -> {});
    }

    public LastfmAlbumTrack createAlbumTrackForPersistence(Consumer<LastfmAlbumTrack.LastfmAlbumTrackBuilder<?, ?>> customizer) {
        LastfmAlbum album = createAndSaveAlbum();
        LastfmTrack track = createAndSaveTrack();
        LastfmApiCall apiCall = createAndSaveApiCall();
        entityManager.flush();
        
        Consumer<LastfmAlbumTrack.LastfmAlbumTrackBuilder<?, ?>> defaultSetter = builder -> builder
            .album(album)
            .track(track)
            .apiCall(apiCall)
            .position(1);
        return EntityCreationHelper.createAlbumTrack(defaultSetter.andThen(customizer));
    }

    public LastfmAlbumTrack createAndSaveAlbumTrack() {
        return createAndSaveAlbumTrack(builder -> {});
    }

    public LastfmAlbumTrack createAndSaveAlbumTrack(Consumer<LastfmAlbumTrack.LastfmAlbumTrackBuilder<?, ?>> customizer) {
        LastfmAlbumTrack albumTrack = createAlbumTrackForPersistence(customizer);
        return albumTrackRepository.save(albumTrack);
    }

    public LastfmAlbumTrack createAndSaveAlbumTrack(LastfmAlbum album, LastfmTrack track) {
        return createAndSaveAlbumTrack(builder -> builder
            .album(album)
            .track(track)
            .position(1));
    }

    // Blacklist helper methods

    public void addToBlacklist(LastfmEntityType entityType, String url) {
        blacklistedEntityUrlRepository.insertIgnoreDuplicate(entityType, url);
    }
}
