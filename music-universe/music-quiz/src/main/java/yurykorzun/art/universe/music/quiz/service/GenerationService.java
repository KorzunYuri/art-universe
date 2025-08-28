package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;

import java.util.List;

public interface GenerationService {
    
    GenerationDto generateTracks(Long gameId, Integer targetCount);
    
    List<GenerationDto> getGenerations(Long gameId);
    
    List<GenerationTrackDto> getGenerationTracks(Long generationId);
}
