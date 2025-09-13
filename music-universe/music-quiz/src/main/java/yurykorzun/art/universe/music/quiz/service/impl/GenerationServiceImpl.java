package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.Generation;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.GenerationTrack;
import yurykorzun.art.universe.music.quiz.repository.GenerationRepository;
import yurykorzun.art.universe.music.quiz.repository.GenerationTrackRepository;
import yurykorzun.art.universe.music.quiz.service.GenerationService;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.GenerationStepProcessorRegistry;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
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
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public GenerationDto generateTracks(Long gameId, Integer targetCount) {
        return generateTracks(gameId, targetCount, null);
    }

    @Override
    @Transactional
    public GenerationDto generateTracks(Long gameId, Integer targetCount, List<GenerationStep> steps) {
        log.debug("Generating {} tracks for game {} with {} UI steps", targetCount, gameId, steps != null ? steps.size() : 0);
        
        // Create generation record
        Generation generation = Generation.builder()
            .gameId(gameId)
            .targetCount(targetCount)
            .status(GenerationStatus.PENDING)
            .build();
        
        Generation savedGeneration = generationRepository.save(generation);
        
        try {
            // Build complete step chain
            List<GenerationStep> allSteps = buildStepChain(steps, targetCount);
            
            // Execute step chain
            String resultTableName = executeStepChain(allSteps, gameId, savedGeneration.getId());
            
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

    private List<GenerationStep> buildStepChain(List<GenerationStep> uiSteps, Integer targetCount) {
        List<GenerationStep> allSteps = new ArrayList<>();
        
        // Fixed steps at the beginning
        allSteps.add(createStep(GenerationStepType.APPROVED_FILTER));
        allSteps.add(createStep(GenerationStepType.TRACK_RECENCY_PENALTY));
        allSteps.add(createStep(GenerationStepType.ARTIST_RECENCY_PENALTY));
        allSteps.add(createStep(GenerationStepType.ARTIST_DIVERSITY));
        
        // UI steps in the middle
        if (uiSteps != null) {
            allSteps.addAll(uiSteps);
        }
        
        // Final selection at the end
        allSteps.add(createFinalSelectionStep(targetCount));
        
        return allSteps;
    }
    
    private GenerationStep createStep(GenerationStepType type) {
        GenerationStep step = new GenerationStep();
        step.setType(type);
        return step;
    }
    
    private GenerationStep createFinalSelectionStep(Integer targetCount) {
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.FINAL_SELECTION);
        step.setParams(Map.of("targetCount", targetCount));
        return step;
    }
    
    private String executeStepChain(List<GenerationStep> steps, Long gameId, Long generationId) {
        String currentTable = "mu_view.v_track";
        
        for (int i = 0; i < steps.size(); i++) {
            GenerationStep step = steps.get(i);
            GenerationStepProcessor processor = GenerationStepProcessorRegistry.get(step.getType());
            
            currentTable = processor.process(currentTable, gameId, generationId, i + 1, step);
        }
        
        return currentTable;
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
            .approved(generation.getApproved())
            .resultTableName(generation.getResultTableName())
            .createdAt(generation.getCreatedAt())
            .build();
    }

    @Override
    @Transactional
    public GenerationDto approveGeneration(Long generationId) {
        log.debug("Approving generation {}", generationId);

        Generation generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException("Generation not found: " + generationId));

        generation.setApproved(true);
        Generation savedGeneration = generationRepository.save(generation);

        log.debug("Approved generation {}", generationId);
        return mapToDto(savedGeneration);
    }

    @Override
    @Transactional
    public GenerationDto disapproveGeneration(Long generationId) {
        log.debug("Disapproving generation {}", generationId);

        Generation generation = generationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalArgumentException("Generation not found: " + generationId));

        generation.setApproved(false);
        Generation savedGeneration = generationRepository.save(generation);

        log.debug("Disapproved generation {}", generationId);
        return mapToDto(savedGeneration);
    }

    @Override
    @Transactional
    public void removeTrackFromGeneration(Long generationId, Long trackId) {
        log.debug("Removing track {} from generation {}", trackId, generationId);
        
        // Проверяем что генерация существует
        if (!generationRepository.existsById(generationId)) {
            throw new IllegalArgumentException("Generation not found: " + generationId);
        }
        
        // Удаляем трек из генерации
        generationTrackRepository.deleteByGenerationIdAndTrackId(generationId, trackId);
        
        log.debug("Removed track {} from generation {}", trackId, generationId);
    }
}