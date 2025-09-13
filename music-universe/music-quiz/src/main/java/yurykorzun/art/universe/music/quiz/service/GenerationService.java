package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;

import java.util.List;

public interface GenerationService {
    
    GenerationDto generateTracks(Long gameId, Integer targetCount);
    
    GenerationDto generateTracks(Long gameId, Integer targetCount, List<GenerationStep> steps);
    
    List<GenerationDto> getGenerations(Long gameId);
    
    List<GenerationTrackDto> getGenerationTracks(Long generationId);

    GenerationDto approveGeneration(Long generationId);
    
    GenerationDto disapproveGeneration(Long generationId);

    void removeTrackFromGeneration(Long generationId, Long trackId);
}
