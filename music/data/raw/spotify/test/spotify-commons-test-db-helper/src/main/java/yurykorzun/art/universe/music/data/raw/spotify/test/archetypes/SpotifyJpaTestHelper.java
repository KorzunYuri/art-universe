package yurykorzun.art.universe.music.data.raw.spotify.test.archetypes;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.test.persistence.SpotifyDbConsistencyHelper;

/**
 * Base test class for Spotify JPA tests.
 */
@Import({
    SpotifyDbConsistencyHelper.class,
})
@Tag("integration")
@Slf4j
public class SpotifyJpaTestHelper extends SpotifyJpaTest {

    @Autowired
    protected SpotifyDbConsistencyHelper consistencyHelper;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void setUp() {
        log.info("Performing database cleanup");
        consistencyHelper.cleanup();
    }
}
