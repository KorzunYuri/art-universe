package yurykorzun.art.universe.music.quiz.service.step.process.procedures;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class EnsureChanceColumnTest extends JpaOnlyTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void ensureChanceColumn_shouldReturnOriginal_whenTableHasChanceColumn() {
        // given - create table with chance column
        entityManager.createNativeQuery("CREATE TABLE mu_quiz_stg.test_table_with_chance (id BIGINT, chance DECIMAL)").executeUpdate();
        
        // when
        String result = (String) entityManager.createNativeQuery("SELECT p_ensure_chance_column('mu_quiz_stg.test_table_with_chance')")
            .getSingleResult();
        
        // then
        assertEquals("mu_quiz_stg.test_table_with_chance", result);
        
        // cleanup
        entityManager.createNativeQuery("DROP TABLE mu_quiz_stg.test_table_with_chance").executeUpdate();
    }

    @Test
    void ensureChanceColumn_shouldAddColumn_whenTableMissingChanceColumn() {
        // given - create table without chance column
        entityManager.createNativeQuery("CREATE TABLE mu_quiz_stg.test_table_no_chance (id BIGINT)").executeUpdate();
        
        // when
        String result = (String) entityManager.createNativeQuery("SELECT p_ensure_chance_column('mu_quiz_stg.test_table_no_chance')")
            .getSingleResult();
        
        // then
        assertEquals("mu_quiz_stg.test_table_no_chance", result);
        
        // verify chance column was added
        Boolean hasChance = (Boolean) entityManager.createNativeQuery(
            "SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema = 'mu_quiz_stg' AND table_name = 'test_table_no_chance' AND column_name = 'chance')")
            .getSingleResult();
        assertTrue(hasChance);
        
        // cleanup
        entityManager.createNativeQuery("DROP TABLE mu_quiz_stg.test_table_no_chance").executeUpdate();
    }

    @Test
    void ensureChanceColumn_shouldReturnOriginal_whenViewHasChanceColumn() {
        // given - create view with chance column
        entityManager.createNativeQuery("CREATE VIEW mu_quiz_stg.test_view_with_chance AS SELECT 1 as id, 1.0 as chance").executeUpdate();
        
        // when
        String result = (String) entityManager.createNativeQuery("SELECT p_ensure_chance_column('mu_quiz_stg.test_view_with_chance')")
            .getSingleResult();
        
        // then
        assertEquals("mu_quiz_stg.test_view_with_chance", result);
        
        // cleanup
        entityManager.createNativeQuery("DROP VIEW mu_quiz_stg.test_view_with_chance").executeUpdate();
    }

    @Test
    void ensureChanceColumn_shouldCreateNewTable_whenViewMissingChanceColumn() {
        // given - create view without chance column
        entityManager.createNativeQuery("CREATE VIEW mu_quiz_stg.test_view_no_chance AS SELECT 1 as id").executeUpdate();
        
        // when
        String result = (String) entityManager.createNativeQuery("SELECT p_ensure_chance_column('mu_quiz_stg.test_view_no_chance')")
            .getSingleResult();
        
        // then
        assertEquals("mu_quiz_stg.test_view_no_chance_chance", result);
        
        // verify new table was created with chance column
        Long count = (Long) entityManager.createNativeQuery("SELECT COUNT(*) FROM mu_quiz_stg.test_view_no_chance_chance")
            .getSingleResult();
        assertEquals(1L, count);
        
        // cleanup
        entityManager.createNativeQuery("DROP TABLE mu_quiz_stg.test_view_no_chance_chance").executeUpdate();
        entityManager.createNativeQuery("DROP VIEW mu_quiz_stg.test_view_no_chance").executeUpdate();
    }
}
