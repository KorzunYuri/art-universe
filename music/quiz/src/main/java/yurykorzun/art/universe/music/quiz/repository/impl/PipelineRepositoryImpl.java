package yurykorzun.art.universe.music.quiz.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Repository
public class PipelineRepositoryImpl implements PipelineRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public String approvedFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_approved_filter(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .getSingleResult();
    }

    @Override
    public String blacklistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, String blacklistSchema, String blacklistTable) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_categories_blacklist_filter(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder, :blacklistSchema, :blacklistTable)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .setParameter("blacklistSchema", blacklistSchema)
            .setParameter("blacklistTable", blacklistTable)
            .getSingleResult();
    }

    @Override
    public String recencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_track_recency_penalty(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .getSingleResult();
    }

    @Override
    public String artistRecencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_artist_recency_penalty(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .getSingleResult();
    }

    @Override
    public String whitelistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, String whitelistSchema, String whitelistTable) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_categories_whitelist_filter(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder, :whitelistSchema, :whitelistTable)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .setParameter("whitelistSchema", whitelistSchema)
            .setParameter("whitelistTable", whitelistTable)
            .getSingleResult();
    }

    @Override
    public String artistDiversity(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_artist_diversity(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .getSingleResult();
    }

    @Override
    public String finalSelection(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, Integer targetCount) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_final_selection(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder, :targetCount)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .setParameter("targetCount", targetCount)
            .getSingleResult();
    }

    @Override
    public String finalCategoriesBalancer(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, String quotaSchema, String quotaTable, Integer targetCount, Double defaultQuota) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_final_categories_balancer(:inputSchema, :inputTable, :gameId, :generationId, :stepOrder, :quotaSchema, :quotaTable, :targetCount, :defaultQuota)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .setParameter("quotaSchema", quotaSchema)
            .setParameter("quotaTable", quotaTable)
            .setParameter("targetCount", targetCount)
            .setParameter("defaultQuota", defaultQuota)
            .getSingleResult();
    }

    @Override
    public String getTablenamePrefix(Long gameId, Long generationId, Integer stepOrder) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_get_tablename_prefix(:gameId, :generationId, :stepOrder)")
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepOrder", stepOrder)
            .getSingleResult();
    }
}
