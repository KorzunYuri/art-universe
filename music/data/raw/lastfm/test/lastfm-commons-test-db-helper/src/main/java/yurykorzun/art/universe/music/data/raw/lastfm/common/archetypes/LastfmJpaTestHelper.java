package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;

/**
 * Base test class for LastFM JPA tests.
 */
@Import({
    DbConsistencyHelper.class,
})
public class LastfmJpaTestHelper extends LastfmJpaTest {
}
