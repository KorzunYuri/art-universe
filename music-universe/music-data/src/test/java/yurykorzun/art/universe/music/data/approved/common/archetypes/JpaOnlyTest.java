package yurykorzun.art.universe.music.data.approved.common.archetypes;


import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Base class for persistence layer testing. Embedded database is removed to make sure we use TestContainers
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class JpaOnlyTest extends BaseTest {
}
