package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationStepDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.entity.step.*;
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
    public GenerationDto generateTracks(Long gameId, List<GenerationStepDto> configuredSteps) {
        log.info("Generating tracks for game {} with {} configured steps", gameId, configuredSteps != null ? configuredSteps.size() : 0);
        
        // Convert DTOs to typed steps
        List<GenerationStep> typedSteps = configuredSteps != null ?
            configuredSteps.stream().map(GenerationStep::fromDto).toList() :
            List.of();
        
        // Validate step configuration
        validateStepConfiguration(typedSteps);
        
        // Extract target count from final step
        Integer actualTargetCount = extractTargetCountFromFinalStep(typedSteps);
        
        // Create generation record
        Generation generation = Generation.builder()
            .gameId(gameId)
            .targetCount(actualTargetCount)
            .status(GenerationStatus.PENDING)
            .build();
        
        Generation savedGeneration = generationRepository.save(generation);
        
        try {
            // Build complete step chain
            List<GenerationStep> allSteps = buildStepChain(typedSteps);
            
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
            
            log.info("Generated {} tracks for game {}", tracks.size(), gameId);
            
        } catch (Exception e) {
            log.error("Failed to generate tracks for game {}", gameId, e);
            savedGeneration.setStatus(GenerationStatus.FAILED);
            generationRepository.save(savedGeneration);
            throw new RuntimeException("Track generation failed", e);
        }
        
        return mapToDto(savedGeneration);
    }

    private void validateStepConfiguration(List<GenerationStep> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("At least one final step is required");
        }
        
        List<GenerationStep> finalSteps = steps.stream()
            .filter(GenerationStep::isFinal)
            .toList();
        
        if (finalSteps.isEmpty()) {
            throw new IllegalArgumentException("No final step found. One of FINAL_SELECTION or FINAL_CATEGORIES_BALANCER is required");
        }
        
        if (finalSteps.size() > 1) {
            throw new IllegalArgumentException("Multiple final steps found. Only one final step is allowed");
        }
        
        // Check that final step is the last step
        GenerationStep lastStep = steps.getLast();
        if (!lastStep.isFinal()) {
            throw new IllegalArgumentException("Final step must be the last step in the configuration");
        }
    }
    
    private Integer extractTargetCountFromFinalStep(List<GenerationStep> steps) {
        GenerationStep finalStep = steps.stream()
            .filter(GenerationStep::isFinal)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No final step found"));
        
        if (finalStep instanceof FinalGenerationStep finalGenerationStep) {
            return finalGenerationStep.getTargetCount();
        }
        
        throw new IllegalArgumentException("Final step must extend FinalGenerationStep");
    }

    private List<GenerationStep> buildStepChain(List<GenerationStep> uiSteps) {
        List<GenerationStep> allSteps = new ArrayList<>();
        
        // Fixed steps at the beginning
        allSteps.add(new ApprovedFilterStep());
        allSteps.add(new TrackRecencyPenaltyStep());
        
        // UI steps (including final step)
        if (uiSteps != null) {
            allSteps.addAll(uiSteps);
        }
        
        return allSteps;
    }
    
    private String executeStepChain(List<GenerationStep> steps, Long gameId, Long generationId) {
        String currentTable = "mu_view.v_track";
        
        for (int i = 0; i < steps.size(); i++) {
            GenerationStep step = steps.get(i);
            currentTable = processTypedStep(step, currentTable, gameId, generationId, i + 1);
        }
        
        return currentTable;
    }
    
    private <T extends GenerationStep> String processTypedStep(T step, String currentTable, Long gameId, Long generationId, Integer stepOrder) {
        GenerationStepProcessor<T> processor = GenerationStepProcessorRegistry.get(step.getType());
        return processor.process(currentTable, gameId, generationId, stepOrder, step);
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