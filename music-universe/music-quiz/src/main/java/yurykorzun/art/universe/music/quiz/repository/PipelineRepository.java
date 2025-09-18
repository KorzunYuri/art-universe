package yurykorzun.art.universe.music.quiz.repository;

public interface PipelineRepository {
    
    String approvedFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder);
    
    String blacklistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, String blacklistSchema, String blacklistTable);
    
    String recencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder);
    
    String artistRecencyPenalty(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder);
    
    String whitelistFilter(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, String whitelistSchema, String whitelistTable);
    
    String artistDiversity(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder);
    
    String finalSelection(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, Integer targetCount);
    
    String finalCategoriesBalancer(String inputSchema, String inputTable, Long gameId, Long generationId, Integer stepOrder, String quotaSchema, String quotaTable, Integer targetCount);

    String getTablenamePrefix(Long gameId, Long generationId, Integer stepOrder);
}
