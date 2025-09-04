package yurykorzun.art.universe.music.quiz.repository;

public interface PipelineRepository {
    
    String approvedFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId);
    
    String blacklistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId, String blacklistSchema, String blacklistTable);
    
    String recencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId);
    
    String artistRecencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId);
    
    String whitelistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId, String whitelistSchema, String whitelistTable);
    
    String artistDiversity(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId);
    
    String finalSelection(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepId, Integer targetCount);
    
    String runPipeline(Long gameId, Long generationId, Integer targetCount);
}
