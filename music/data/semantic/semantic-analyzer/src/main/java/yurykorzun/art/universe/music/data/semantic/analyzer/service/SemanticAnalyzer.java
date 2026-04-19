package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.semantic.analyzer.config.LlmClientRegistry;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisRequest;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisTicket;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmRequest;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmResponse;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ClientSelection;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureType;
import yurykorzun.art.universe.music.data.semantic.analyzer.prompt.PromptBuilder;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisVersions;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SemanticAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SemanticAnalyzer.class);

    private final AnalysisTicketService ticketService;
    private final AnalysisRequestService requestService;
    private final LlmClientRegistry clientRegistry;
    private final PromptBuilder promptBuilder;

    public SemanticAnalyzer(
        AnalysisTicketService ticketService,
        AnalysisRequestService requestService,
        LlmClientRegistry clientRegistry,
        PromptBuilder promptBuilder
    ) {
        this.ticketService = ticketService;
        this.requestService = requestService;
        this.clientRegistry = clientRegistry;
        this.promptBuilder = promptBuilder;
    }

    public void processTicket(AnalysisTicket ticket) {
        AnalysisMode mode = ticket.getAnalysisMode();
        String analysisVersion = AnalysisVersions.currentVersionFor(mode);

        ClientSelection selection = clientRegistry.acquireClient(mode);
        if (selection == null) {
            ticketService.resetToPending(ticket);
            log.warn("No healthy LLM clients for mode {} — deferring ticket {}", mode.getName(), ticket.getId());
            return;
        }

        Set<ProposalType> expectedTypes = resolveExpectedProposalTypes(ticket.getExpectedProposalTypes());
        Set<MasterEntityType> expectedEntityTypes = resolveExpectedEntityTypes(ticket.getExpectedEntityTypes());

        String systemPrompt = promptBuilder.buildSystemPrompt(mode);
        String userPrompt = promptBuilder.buildUserPrompt(
            mode,
            ticket.getSubjectType(),
            ticket.getSubjectName(),
            ticket.getSubjectId(),
            ticket.getTextSamplesJson(),
            expectedTypes,
            expectedEntityTypes
        );
        String inputHash = computeHash(userPrompt);

        AnalysisRequest request = requestService.createRequest(ticket, inputHash, analysisVersion);

        try {
            LlmResponse response = selection.client().analyze(LlmRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .jsonMode(true)
                .build());

            if (response.isSuccess()) {
                clientRegistry.reportSuccess(selection.clientName());
                requestService.completeRequest(request.getId(), response);
                ticketService.markCompleted(ticket);
                log.info("Ticket {} analyzed successfully via client '{}' (mode={})",
                    ticket.getId(), selection.clientName(), mode.getName());
            } else {
                LlmFailureType failureType = clientRegistry.reportFailure(selection.clientName(), response);
                if (isTicketRetriable(failureType)) {
                    // Discard the request row so the retry can re-insert under the same
                    // (input_hash, analysis_version) without tripping the unique constraint.
                    requestService.discardRequest(request.getId());
                    ticketService.resetToPending(ticket);
                    log.warn("Ticket {} deferred — client '{}' failure: {} ({})",
                        ticket.getId(), selection.clientName(), failureType, response.getErrorMessage());
                } else {
                    requestService.failRequest(request.getId(), response.getProvider(), response.getErrorMessage());
                    ticketService.markFailed(ticket, response.getErrorMessage());
                    log.warn("Ticket {} FAILED permanently — client '{}' {}: {}",
                        ticket.getId(), selection.clientName(), failureType, response.getErrorMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error processing ticket {}", ticket.getId(), e);
            requestService.failRequest(request.getId(), null, e.getMessage());
            ticketService.markFailed(ticket, e.getMessage());
        }
    }

    /**
     * A ticket is retriable (reset to PENDING) when the failure is tied to the
     * specific client rather than the ticket content. A ban or rate-limit on
     * this client doesn't mean another client in the priority list will fail.
     * CLIENT_ERROR (e.g. 400 from a bad prompt) is intrinsic to the ticket.
     */
    private static boolean isTicketRetriable(LlmFailureType type) {
        return switch (type) {
            case BANNED, RATE_LIMITED, SERVER_ERROR, NETWORK_ERROR -> true;
            case CLIENT_ERROR -> false;
        };
    }

    private Set<ProposalType> resolveExpectedProposalTypes(Integer[] codes) {
        if (codes == null || codes.length == 0) {
            return null;
        }
        return Arrays.stream(codes)
            .map(code -> EnumSet.allOf(ProposalType.class).stream()
                .filter(t -> t.getCode() == code)
                .findFirst()
                .orElse(null))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(ProposalType.class)));
    }

    private Set<MasterEntityType> resolveExpectedEntityTypes(Integer[] codes) {
        if (codes == null || codes.length == 0) {
            return null;
        }
        return Arrays.stream(codes)
            .map(code -> EnumSet.allOf(MasterEntityType.class).stream()
                .filter(t -> t.getCode() == code)
                .findFirst()
                .orElse(null))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(MasterEntityType.class)));
    }

    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
