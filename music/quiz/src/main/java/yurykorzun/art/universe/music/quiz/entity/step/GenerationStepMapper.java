package yurykorzun.art.universe.music.quiz.entity.step;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.quiz.dto.GenerationStepDto;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.step.finish.FinalCategoriesBalancerStep;
import yurykorzun.art.universe.music.quiz.entity.step.finish.FinalSelectionStep;
import yurykorzun.art.universe.music.quiz.entity.step.middle.*;
import yurykorzun.art.universe.music.quiz.entity.step.start.StartDatasourceStep;

import java.util.List;
import java.util.Map;

public class GenerationStepMapper {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static GenerationStep fromDto(GenerationStepDto dto) {
        return switch (dto.getType()) {
            case START_DATASOURCE -> new StartDatasourceStep();
            case APPROVED_FILTER -> new ApprovedFilterStep();
            case BLACKLIST_FILTER -> mapBlacklistFilter(dto);
            case WHITELIST_FILTER -> mapWhitelistFilter(dto);
            case TRACK_RECENCY_PENALTY -> new TrackRecencyPenaltyStep();
            case ARTIST_RECENCY_PENALTY -> new ArtistRecencyPenaltyStep();
            case ARTIST_DIVERSITY -> new ArtistDiversityStep();
            case FINAL_SELECTION -> mapFinalSelection(dto);
            case FINAL_CATEGORIES_BALANCER -> mapFinalCategoriesBalancer(dto);
        };
    }
    
    // Маппинг из entity в domain объект
    public static GenerationStep fromEntity(Step step) {
        try {
            Map<String, Object> params = step.getCfgData() != null ? 
                objectMapper.readValue(step.getCfgData(), Map.class) : Map.of();
            
            GenerationStepDto dto = new GenerationStepDto();
            dto.setType(step.getType());
            dto.setParams(params);
            
            return fromDto(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse step configuration", e);
        }
    }
    
    // Маппинг из domain объекта в JSON конфигурацию
    public static String toConfigJson(GenerationStep step) {
        try {
            Map<String, Object> config = switch (step.getType()) {
                case BLACKLIST_FILTER -> Map.of("categoryIds", ((BlacklistFilterStep) step).getCategoryIds());
                case WHITELIST_FILTER -> Map.of("categories", ((WhitelistFilterStep) step).getCategories());
                case FINAL_SELECTION -> Map.of("targetCount", ((FinalSelectionStep) step).getTargetCount());
                case FINAL_CATEGORIES_BALANCER -> {
                    var balancer = (FinalCategoriesBalancerStep) step;
                    yield Map.of(
                        "targetCount", balancer.getTargetCount(),
                        "categories", balancer.getCategories(),
                        "defaultQuota", balancer.getDefaultQuota()
                    );
                }
                default -> Map.of();
            };
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize step configuration", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static BlacklistFilterStep mapBlacklistFilter(GenerationStepDto dto) {
        List<Number> ids = (List<Number>) dto.getParams().get("categoryIds");
        List<Long> categoryIds = ids.stream().map(Number::longValue).toList();
        return new BlacklistFilterStep(categoryIds);
    }
    
    @SuppressWarnings("unchecked")
    private static WhitelistFilterStep mapWhitelistFilter(GenerationStepDto dto) {
        if (dto.getParams() == null || !dto.getParams().containsKey("categories")) {
            throw new IllegalArgumentException("Whitelist step requires 'categories' parameter");
        }
        List<Map<String, Object>> categoriesData = (List<Map<String, Object>>) dto.getParams().get("categories");
        List<WhitelistFilterStep.CategoryWeight> categories = categoriesData.stream()
            .map(data -> new WhitelistFilterStep.CategoryWeight(
                ((Number) data.get("id")).longValue(),
                ((Number) data.get("weight")).doubleValue()
            ))
            .toList();
        return new WhitelistFilterStep(categories);
    }
    
    private static FinalSelectionStep mapFinalSelection(GenerationStepDto dto) {
        if (dto.getParams() == null || !dto.getParams().containsKey("targetCount")) {
            throw new IllegalArgumentException("Final step must contain 'targetCount' parameter");
        }
        Integer targetCount = (Integer) dto.getParams().get("targetCount");
        return new FinalSelectionStep(targetCount);
    }
    
    @SuppressWarnings("unchecked")
    private static FinalCategoriesBalancerStep mapFinalCategoriesBalancer(GenerationStepDto dto) {
        if (dto.getParams() == null || !dto.getParams().containsKey("targetCount")) {
            throw new IllegalArgumentException("Final step must contain 'targetCount' parameter");
        }

        Integer targetCount = (Integer) dto.getParams().get("targetCount");
        Number defaultQuotaNum = ((Number) dto.getParams().get("defaultQuota"));
        if (defaultQuotaNum == null) {
            throw new IllegalArgumentException("Final categories balancer step requires 'defaultQuota' parameter");
        }
        Double defaultQuota = defaultQuotaNum.doubleValue();

        List<Map<String, Object>> categoriesData = (List<Map<String, Object>>) dto.getParams().get("categories");
        if (categoriesData == null) {
            throw new IllegalArgumentException("Final categories balancer step requires 'categories' parameter");
        }
        List<FinalCategoriesBalancerStep.CategoryWeight> categories = categoriesData.stream()
            .map(data -> new FinalCategoriesBalancerStep.CategoryWeight(
                ((Number) data.get("id")).longValue(),
                ((Number) data.get("weight")).doubleValue()
            ))
            .toList();

        return new FinalCategoriesBalancerStep(targetCount, categories, defaultQuota);
    }
}
