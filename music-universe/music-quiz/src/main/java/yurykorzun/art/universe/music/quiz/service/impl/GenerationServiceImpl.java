package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.Generation;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.entity.GenerationTrack;
import yurykorzun.art.universe.music.quiz.repository.GenerationRepository;
import yurykorzun.art.universe.music.quiz.repository.GenerationTrackRepository;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;
import yurykorzun.art.universe.music.quiz.service.GenerationService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationServiceImpl implements GenerationService {

    private final GenerationRepository generationRepository;
    private final GenerationTrackRepository generationTrackRepository;
    private final PipelineRepository pipelineRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public GenerationDto generateTracks(Long gameId, Integer targetCount) {
        log.debug("Generating {} tracks for game {}", targetCount, gameId);
        
        // Create generation record
        Generation generation = Generation.builder()
            .gameId(gameId)
            .targetCount(targetCount)
            .status(GenerationStatus.PENDING)
            .build();
        
        Generation savedGeneration = generationRepository.save(generation);
        
        try {
            // Call pipeline procedure
            String resultTableName = pipelineRepository.runPipeline(gameId, savedGeneration.getId(), targetCount);
            
            // Read results and save to GenerationTrack
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery("""
                    SELECT
                        vt.id as track_id,
                        vt.primary_artist_id,
                        vt.name as track_name,
                        va.name as artist_name
                    FROM %s rt
                    JOIN mu_view.v_track vt
                        ON rt.track_id = vt.id
                    JOIN mu_view.v_artist va
                        ON rt.primary_artist_id = va.id
                    ORDER BY RANDOM()
                """.formatted(resultTableName))
                .getResultList();
            
            AtomicInteger orderIndex = new AtomicInteger(1);
            final var generationId = savedGeneration.getId();
            List<GenerationTrack> tracks = results.stream()
                .map(row -> GenerationTrack.builder()
                    .generationId(generationId)
                    .trackId(((Number) row[0]).longValue())
                    .primaryArtistId(((Number) row[1]).longValue())
                    .trackName((String) row[2])
                    .artistName((String) row[3])
                    .orderIndex(orderIndex.getAndIncrement())
                    .build())
                .collect(Collectors.toList());

            generationTrackRepository.saveAll(tracks);
            
            // Update generation status
            savedGeneration.setStatus(GenerationStatus.COMPLETED);
            savedGeneration.setResultTableName(resultTableName);
            savedGeneration = generationRepository.save(savedGeneration);
            
            log.debug("Generated {} tracks for game {}", tracks.size(), gameId);
            
        } catch (Exception e) {
            log.error("Failed to generate tracks for game {}", gameId, e);
            savedGeneration.setStatus(GenerationStatus.FAILED);
            generationRepository.save(savedGeneration);
            throw new RuntimeException("Track generation failed", e);
        }
        
        return mapToDto(savedGeneration);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenerationDto> getGenerations(Long gameId) {
        log.debug("Getting generations for game {}", gameId);
        
        return generationRepository.findByGameIdOrderByCreatedAtDesc(gameId)
            .stream()
            .map(this::mapToDto)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenerationTrackDto> getGenerationTracks(Long generationId) {
        log.debug("Getting tracks for generation {}", generationId);
        
        return generationTrackRepository.findByGenerationIdOrderByOrderIndex(generationId)
            .stream()
            .map(track -> GenerationTrackDto.builder()
                .trackId(track.getTrackId())
                .trackName(track.getTrackName())
                .artistName(track.getArtistName())
                .orderIndex(track.getOrderIndex())
                .build())
            .toList();
    }
    
    private GenerationDto mapToDto(Generation generation) {
        return GenerationDto.builder()
            .id(generation.getId())
            .gameId(generation.getGameId())
            .targetCount(generation.getTargetCount())
            .status(generation.getStatus())
            .resultTableName(generation.getResultTableName())
            .createdAt(generation.getCreatedAt())
            .build();
    }
}
