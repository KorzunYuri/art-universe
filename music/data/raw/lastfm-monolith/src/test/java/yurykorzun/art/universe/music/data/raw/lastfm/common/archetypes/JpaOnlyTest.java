package yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes;


import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Base class for persistence layer testing. Embedded database is removed to make sure we use TestContainers
 */
@DataJpaTest
@Import(DbConsistencyHelper.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class JpaOnlyTest extends BaseTest {
}
