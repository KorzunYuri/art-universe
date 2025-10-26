package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovedFilterProcessorTest {

    @Mock
    private StepRunRepository stepRunRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private ApprovedFilterProcessor processor;

    @Test
    void processStep_shouldCallProcedure_whenValidInput() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        processor = new ApprovedFilterProcessor(stepRunRepository, stepRepository, objectMapper);
        ReflectionTestUtils.setField(processor, "entityManager", entityManager);
        
        Step step = Step.builder()
            .id(1L)
            .type(StepType.APPROVED_FILTER)
            .cfgData("{}")
            .build();
        
        StepRun stepRun = StepRun.builder().id(1L).build();
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        // when
        StepRunResult result = ReflectionTestUtils.invokeMethod(processor, "processStep", 
            step, "input.table", "output.table", stepRun);

        // then
        assertNotNull(result);
        assertEquals("output.table", result.getOutputTableName());
        verify(entityManager).createNativeQuery(contains("p_quiz_gen_tracks_step_approved_filter"));
        verify(query).setParameter("inputTable", "input.table");
        verify(query).setParameter("outputTable", "output.table");
        verify(query).executeUpdate();
    }

    @Test
    void getStepSuffix_shouldReturnApproved() {
        // given
        ObjectMapper objectMapper = new ObjectMapper();
        processor = new ApprovedFilterProcessor(stepRunRepository, stepRepository, objectMapper);

        // when
        String result = ReflectionTestUtils.invokeMethod(processor, "getStepSuffix");

        // then
        assertEquals("approved", result);
    }
}
