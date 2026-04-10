package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisRequest;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisTicket;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmResponse;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.AnalysisRequestRepository;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisRequestStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the lifecycle of analysis requests: created when processing starts,
 * completed with the LLM response, or failed with an error message.
 */
@Service
public class AnalysisRequestService {

    private final AnalysisRequestRepository requestRepository;

    public AnalysisRequestService(AnalysisRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Transactional
    public AnalysisRequest createRequest(AnalysisTicket ticket, String inputHash, String analysisVersion) {
        AnalysisRequest request = AnalysisRequest.builder()
            .id(UUID.randomUUID())
            .ticketId(ticket.getId())
            .inputHash(inputHash)
            .analysisVersion(analysisVersion)
            .dataSource(ticket.getDataSource())
            .subjectType(ticket.getSubjectType())
            .subjectId(ticket.getSubjectId())
            .analysisMode(ticket.getAnalysisMode())
            .status(AnalysisRequestStatus.PROCESSING)
            .createdAt(Instant.now())
            .build();
        return requestRepository.save(request);
    }

    @Transactional
    public void completeRequest(UUID requestId, LlmResponse response) {
        AnalysisRequest request = requestRepository.getReferenceById(requestId);
        request.setStatus(AnalysisRequestStatus.COMPLETED);
        request.setLlmProvider(response.getProvider());
        request.setLlmModel(response.getModel());
        request.setPromptTokens(response.getPromptTokens());
        request.setCompletionTokens(response.getCompletionTokens());
        request.setRawResponse(response.getContent());
        request.setCompletedAt(Instant.now());
    }

    @Transactional
    public void failRequest(UUID requestId, String provider, String errorMessage) {
        AnalysisRequest request = requestRepository.getReferenceById(requestId);
        request.setStatus(AnalysisRequestStatus.FAILED);
        request.setLlmProvider(provider);
        request.setErrorMessage(errorMessage);
        request.setCompletedAt(Instant.now());
    }
}
