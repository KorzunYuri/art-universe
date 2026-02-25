package yurykorzun.art.universe.music.quiz.dto.step.config;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StepConfigTest {

    @Test
    void finalLimiterStepConfig_shouldReturnCorrectType() {
        FinalLimiterStepConfig config = new FinalLimiterStepConfig(20);
        assertEquals(StepType.FINAL_LIMITER, config.getType());
        assertEquals(20, config.targetCount());
    }

    @Test
    void finalCategoriesBalancerConfig_shouldReturnCorrectType() {
        FinalCategoriesBalancerConfig config = new FinalCategoriesBalancerConfig(20, 0.5, List.of());
        assertEquals(StepType.FINAL_CATEGORIES_BALANCER, config.getType());
        assertEquals(20, config.targetCount());
        assertEquals(0.5, config.defaultQuota());
    }

    @Test
    void startDatasourceStepConfig_shouldReturnCorrectTypeAndDatasource() {
        StartDatasourceStepConfig config = new StartDatasourceStepConfig();
        assertEquals(StepType.START_DATASOURCE, config.getType());
        assertEquals("mu_quiz.ds_mu_v_track", config.getDatasource());
    }

    @Test
    void whitelistFilterStepConfig_shouldReturnCorrectType() {
        WhitelistFilterStepConfig config = new WhitelistFilterStepConfig(List.of(new CategoryWeight(1L, 1.0)));
        assertEquals(StepType.WHITELIST_FILTER, config.getType());
        assertEquals(1, config.categories().size());
    }

    @Test
    void blacklistFilterStepConfig_shouldReturnCorrectType() {
        BlacklistFilterStepConfig config = new BlacklistFilterStepConfig(List.of(1L, 2L));
        assertEquals(StepType.BLACKLIST_FILTER, config.getType());
        assertEquals(2, config.categoryIds().size());
    }
}
