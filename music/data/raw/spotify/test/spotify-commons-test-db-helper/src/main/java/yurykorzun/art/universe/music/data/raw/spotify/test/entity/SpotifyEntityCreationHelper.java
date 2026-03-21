package yurykorzun.art.universe.music.data.raw.spotify.test.entity;

import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Consumer;

public class SpotifyEntityCreationHelper {

    public static final SpotifyApiCallType DEFAULT_API_CALL_TYPE = SpotifyApiCallType.ARTIST_GET;

    private SpotifyEntityCreationHelper() {
    }

    // API call

    public static SpotifyApiCall createApiCall(Consumer<SpotifyApiCall.SpotifyApiCallBuilder<?, ?>> customizer) {
        SpotifyApiCall.SpotifyApiCallBuilder<?, ?> builder = SpotifyApiCall.builder()
            .type(DEFAULT_API_CALL_TYPE)
            .dueDttm(Instant.now().plus(1, ChronoUnit.HOURS))
            .spotifyId(UUID.randomUUID().toString());
        customizer.accept(builder);
        return builder.build();
    }

    public static SpotifyApiCall createApiCall() {
        return createApiCall(builder -> {});
    }

    public static SpotifyApiCall createApiCall(SpotifyApiCallType type) {
        return createApiCall(builder -> builder.type(type));
    }

    // Artist

    public static SpotifyArtist createArtist(SpotifyApiCall apiCall, Consumer<SpotifyArtist.SpotifyArtistBuilder<?, ?>> customizer) {
        SpotifyArtist.SpotifyArtistBuilder<?, ?> builder = SpotifyArtist.builder()
            .apiCall(apiCall)
            .spotifyId(UUID.randomUUID().toString())
            .name("Test Artist " + UUID.randomUUID().toString().substring(0, 8))
            .approvalStatus(ApprovalStatus.PENDING);
        customizer.accept(builder);
        return builder.build();
    }

    public static SpotifyArtist createArtist(SpotifyApiCall apiCall) {
        return createArtist(apiCall, builder -> {});
    }

    // Search attempt

    public static SpotifySearchAttempt createSearchAttempt() {
        return SpotifySearchAttempt.builder()
            .entityType(SpotifyEntityType.ARTIST)
            .masterEntityId(1L)
            .searchString("Test Artist")
            .status(SearchAttemptStatus.PENDING)
            .build();
    }
}
