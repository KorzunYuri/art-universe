package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;

import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;

@Import({
    DbConsistencyHelper.class,
})
public class JpaTestWithHelper extends JpaOnlyTest {
}
