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
    public String approvedFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_approved_filter(:inputSchema, :inputTable, :gameId, :generationId, :stepId)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .getSingleResult();
    }

    @Override
    public String blacklistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId, String blacklistSchema, String blacklistTable) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_categories_blacklist_filter(:inputSchema, :inputTable, :gameId, :generationId, :stepId, :blacklistSchema, :blacklistTable)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .setParameter("blacklistSchema", blacklistSchema)
            .setParameter("blacklistTable", blacklistTable)
            .getSingleResult();
    }

    @Override
    public String recencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_recency_penalty(:inputSchema, :inputTable, :gameId, :generationId, :stepId)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .getSingleResult();
    }

    @Override
    public String artistRecencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_artist_recency_penalty(:inputSchema, :inputTable, :gameId, :generationId, :stepId)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .getSingleResult();
    }

    @Override
    public String whitelistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId, String whitelistSchema, String whitelistTable) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_categories_whitelist_filter(:inputSchema, :inputTable, :gameId, :generationId, :stepId, :whitelistSchema, :whitelistTable)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .setParameter("whitelistSchema", whitelistSchema)
            .setParameter("whitelistTable", whitelistTable)
            .getSingleResult();
    }

    @Override
    public String artistDiversity(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_artist_diversity(:inputSchema, :inputTable, :gameId, :generationId, :stepId)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .getSingleResult();
    }

    @Override
    public String finalSelection(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId, Integer targetCount) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_final_selection(:inputSchema, :inputTable, :gameId, :generationId, :stepId, :targetCount)")
            .setParameter("inputSchema", inputSchema)
            .setParameter("inputTable", inputTable)
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("stepId", stepId)
            .setParameter("targetCount", targetCount)
            .getSingleResult();
    }

    @Override
    public String runPipeline(Long gameId, Long generationId, Integer targetCount) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_pipeline(:gameId, :generationId, :targetCount)")
            .setParameter("gameId", gameId)
            .setParameter("generationId", generationId)
            .setParameter("targetCount", targetCount)
            .getSingleResult();
    }
}
