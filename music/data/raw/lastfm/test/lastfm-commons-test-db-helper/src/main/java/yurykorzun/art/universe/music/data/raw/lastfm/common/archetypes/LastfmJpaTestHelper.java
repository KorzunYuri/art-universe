package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;

/**
 * Base test class for LastFM JPA tests.
 */
@Import({
    DbConsistencyHelper.class,
})
@Tag("integration")
public class LastfmJpaTestHelper extends LastfmJpaTest {
}
