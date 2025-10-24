package yurykorzun.art.universe.music.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.quiz.dto.GenerationDto;
import yurykorzun.art.universe.music.quiz.dto.GenerationTrackDto;
import yurykorzun.art.universe.music.quiz.entity.*;
import yurykorzun.art.universe.music.quiz.repository.*;
import yurykorzun.art.universe.music.quiz.service.GenerationService;
import yurykorzun.art.universe.music.quiz.service.PipelineService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationServiceImpl implements GenerationService {

    private final GenerationRepository generationRepository;
    private final GenerationTrackRepository generationTrackRepository;
    private final GameRepository gameRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStepRepository pipelineStepRepository;
    private final StepRepository stepRepository;
    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineService pipelineService;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public GenerationDto generateTracks(Long gameId) {
        log.info("Generating tracks for game {}", gameId);
        
        // Get game and its pipeline
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));
        
        // Validate pipeline for generation
        pipelineService.validatePipelineForGeneration(game.getPipelineId());
        
        // Create immutable copy of pipeline
        Pipeline originalPipeline = pipelineRepository.findById(game.getPipelineId())
            .orElseThrow(() -> new IllegalArgumentException("Pipeline not found: " + game.getPipelineId()));
        
        Pipeline immutablePipeline = Pipeline.builder()
            .immutable(true)
            .build();
        Pipeline savedPipeline = pipelineRepository.save(immutablePipeline);
        
        // Copy pipeline steps and steps
        List<PipelineStep> originalSteps = pipelineStepRepository.findByPipelineIdOrderByOrd(originalPipeline.getId());
        for (PipelineStep originalPipelineStep : originalSteps) {
            Step originalStep = stepRepository.findById(originalPipelineStep.getStepId())
                .orElseThrow(() -> new IllegalArgumentException("Step not found: " + originalPipelineStep.getStepId()));
            
            // Create immutable copy of step
            Step immutableStep = Step.builder()
                .type(originalStep.getType())
                .algVersion(originalStep.getAlgVersion())
                .cfgData(originalStep.getCfgData())
                .deleted(false)
                .immutable(true)
                .build();
            Step savedStep = stepRepository.save(immutableStep);
            
            // Create pipeline step for immutable pipeline
            PipelineStep immutablePipelineStep = PipelineStep.builder()
                .pipelineId(savedPipeline.getId())
                .stepId(savedStep.getId())
                .ord(originalPipelineStep.getOrd())
                .build();
            pipelineStepRepository.save(immutablePipelineStep);
        }
        
        // Create pipeline run
        PipelineRun pipelineRun = PipelineRun.builder()
            .pipelineId(savedPipeline.getId())
            .build();
        PipelineRun savedPipelineRun = pipelineRunRepository.save(pipelineRun);
        
        // Create generation
        Generation generation = Generation.builder()
            .gameId(gameId)
            .pipelineRunId(savedPipelineRun.getId())
            .status(GenerationStatus.PENDING)
            .build();
        Generation savedGeneration = generationRepository.save(generation);
        
        try {
            // Execute pipeline
            PipelineRun completedPipelineRun = pipelineService.executePipeline(savedPipeline.getId(), savedPipelineRun.getId());
            
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
                """.formatted(completedPipelineRun.getResultTableName()))
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
            savedGeneration.setResultTableName(completedPipelineRun.getResultTableName());
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
    
    private GenerationDto mapToDto(Generation generation) {
        return GenerationDto.builder()
            .id(generation.getId())
            .gameId(generation.getGameId())
            .pipelineRunId(generation.getPipelineRunId())
            .status(generation.getStatus())
            .approved(generation.getApproved())
            .resultTableName(generation.getResultTableName())
            .createdAt(generation.getCreatedAt())
            .build();
    }
}
