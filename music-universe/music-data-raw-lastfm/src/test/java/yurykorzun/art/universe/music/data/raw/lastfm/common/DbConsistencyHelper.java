package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * While testing persistence/service layer, it's often needed to create related entities to maintain db consistency.
 * Current class provides methods to do this.
 */
@Component
@Profile("test")
@Slf4j
public class DbConsistencyHelper {

    private final LastfmDataSnapshotRepository snapshotRepository;
    private final LastfmApiCallRepository apiCallRepository;
    private final LastfmTagRepository tagRepository;

    public static final LastfmApiCallType DUMMY_API_CALL_TYPE = LastfmApiCallType.TAG_TOP_TAGS;

    public DbConsistencyHelper(LastfmDataSnapshotRepository snapshotRepository, LastfmApiCallRepository apiCallRepository, LastfmTagRepository tagRepository) {
        this.snapshotRepository = snapshotRepository;
        this.apiCallRepository = apiCallRepository;
        this.tagRepository = tagRepository;
    }

    public void cleanup() {
        snapshotRepository.deleteAll();
        apiCallRepository.deleteAll();
        tagRepository.deleteAll();
    }

    public LastfmDataSnapshot createDummyDataSnapshot(LastfmApiCallType apiCallType) {
        return snapshotRepository.save(new LastfmDataSnapshot(apiCallType, LocalDate.now()));
    }

    public LastfmDataSnapshot createDummyDataSnapshot() {
        return createDummyDataSnapshot(DUMMY_API_CALL_TYPE);
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

    public LastfmApiResponse createDummyApiResponse(String responseBody) {
        return createDummyApiResponse(responseBody, DUMMY_API_CALL_TYPE);
    }

    public BaseLastfmEntity createDummyEntity() {
        LastfmApiCall dummyApiCall = createDummyApiCall();
        LastfmTag tag = LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .apiCall(dummyApiCall)
            .build();
        return tagRepository.save(tag);
    }

}
