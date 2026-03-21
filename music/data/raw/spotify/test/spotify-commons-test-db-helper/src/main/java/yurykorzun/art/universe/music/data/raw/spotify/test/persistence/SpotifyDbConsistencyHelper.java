package yurykorzun.art.universe.music.data.raw.spotify.test.persistence;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.test.entity.SpotifyEntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.spotify.test.repository.TestSpotifyApiCallRepository;
import yurykorzun.art.universe.music.data.raw.spotify.test.repository.TestSpotifyApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.spotify.test.repository.TestSpotifyArtistRepository;
import yurykorzun.art.universe.music.data.raw.spotify.test.repository.TestSpotifyEntityRelationRepository;
import yurykorzun.art.universe.music.data.raw.spotify.test.repository.TestSpotifySearchAttemptRepository;
import yurykorzun.art.universe.music.data.raw.spotify.test.repository.TestSpotifyStagingIterationRepository;

import java.util.function.Consumer;

/**
 * Provides helper methods for creating and cleaning up Spotify test entities
 * while maintaining DB consistency.
 */
@Component
@Profile("test")
@Slf4j
public class SpotifyDbConsistencyHelper {

    private final TestSpotifyEntityRelationRepository entityRelationRepository;
    private final TestSpotifyArtistRepository artistRepository;
    private final TestSpotifySearchAttemptRepository searchAttemptRepository;
    private final TestSpotifyStagingIterationRepository stagingIterationRepository;
    private final TestSpotifyApiResponseRepository apiResponseRepository;
    private final TestSpotifyApiCallRepository apiCallRepository;
    private final EntityManager entityManager;

    public SpotifyDbConsistencyHelper(
        TestSpotifyEntityRelationRepository entityRelationRepository,
        TestSpotifyArtistRepository artistRepository,
        TestSpotifySearchAttemptRepository searchAttemptRepository,
        TestSpotifyStagingIterationRepository stagingIterationRepository,
        TestSpotifyApiResponseRepository apiResponseRepository,
        TestSpotifyApiCallRepository apiCallRepository,
        EntityManager entityManager
    ) {
        this.entityRelationRepository = entityRelationRepository;
        this.artistRepository = artistRepository;
        this.searchAttemptRepository = searchAttemptRepository;
        this.stagingIterationRepository = stagingIterationRepository;
        this.apiResponseRepository = apiResponseRepository;
        this.apiCallRepository = apiCallRepository;
        this.entityManager = entityManager;
    }

    public void cleanup() {
        entityManager.clear();

        entityRelationRepository.deleteAll();
        artistRepository.deleteAll();
        searchAttemptRepository.deleteAll();
        stagingIterationRepository.deleteAll();
        apiResponseRepository.deleteAll();
        apiCallRepository.deleteAll();
    }

    public void flush() {
        entityManager.flush();
    }

    // API call

    public SpotifyApiCall createAndSaveApiCall() {
        return createAndSaveApiCall(builder -> {});
    }

    public SpotifyApiCall createAndSaveApiCall(SpotifyApiCallType type) {
        return createAndSaveApiCall(builder -> builder.type(type));
    }

    public SpotifyApiCall createAndSaveApiCall(Consumer<SpotifyApiCall.SpotifyApiCallBuilder<?, ?>> customizer) {
        SpotifyApiCall apiCall = SpotifyEntityCreationHelper.createApiCall(customizer);
        return apiCallRepository.save(apiCall);
    }

    // Artist

    public SpotifyArtist createAndSaveArtist() {
        return createAndSaveArtist(builder -> {});
    }

    public SpotifyArtist createAndSaveArtist(Consumer<SpotifyArtist.SpotifyArtistBuilder<?, ?>> customizer) {
        SpotifyApiCall apiCall = createAndSaveApiCall();
        SpotifyArtist artist = SpotifyEntityCreationHelper.createArtist(apiCall, customizer);
        return artistRepository.save(artist);
    }

    // Staging iteration

    public StagingIteration createAndSaveStagingIteration() {
        StagingIteration iteration = StagingIteration.builder().build();
        return stagingIterationRepository.save(iteration);
    }

    // Search attempt

    public SpotifySearchAttempt createAndSaveSearchAttempt() {
        SpotifySearchAttempt attempt = SpotifyEntityCreationHelper.createSearchAttempt();
        return searchAttemptRepository.save(attempt);
    }
}
