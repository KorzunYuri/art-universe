package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;

/**
 * Base test class for LastFM JPA tests.
 */
@Import({
    DbConsistencyHelper.class,
})
@Tag("integration")
@Slf4j
public class LastfmJpaTestHelper extends LastfmJpaTest {

    @Autowired
    protected DbConsistencyHelper consistencyHelper;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void setUp() {
        log.info("Performing database cleanup");
        consistencyHelper.cleanup();
    }

}
