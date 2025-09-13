package yurykorzun.art.universe.music.quiz.service.step;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistRecencyPenaltyProcessorTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private ArtistRecencyPenaltyProcessor processor;

    @Test
    void process_shouldCallRepositoryWithCorrectParams() {
        // given
        GenerationStep step = new GenerationStep();
        step.setType(GenerationStepType.ARTIST_RECENCY_PENALTY);

        when(pipelineRepository.artistRecencyPenalty(anyString(), anyString(), anyLong(), anyLong(), anyInt()))
            .thenReturn("result_table");

        // when
        String result = processor.process("schema.table", 1L, 2L, 3, step);

        // then
        assertEquals("result_table", result);
        verify(pipelineRepository).artistRecencyPenalty("schema", "table", 1L, 2L, 3);
    }
}
