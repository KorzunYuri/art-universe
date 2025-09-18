package yurykorzun.art.universe.music.quiz.service;

import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationStepDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;

import java.util.List;

public interface GenerationService {

    GenerationDto generateTracks(Long gameId, List<GenerationStepDto> steps);
    
    List<GenerationDto> getGenerations(Long gameId);
    
    List<GenerationTrackDto> getGenerationTracks(Long generationId);

    GenerationDto approveGeneration(Long generationId);
    
    GenerationDto disapproveGeneration(Long generationId);

    void removeTrackFromGeneration(Long generationId, Long trackId);
}
