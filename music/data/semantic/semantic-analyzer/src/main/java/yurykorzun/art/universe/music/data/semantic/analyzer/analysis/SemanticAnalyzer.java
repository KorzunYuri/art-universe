package yurykorzun.art.universe.music.data.semantic.analyzer.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmRequest;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmResponse;
import yurykorzun.art.universe.music.data.semantic.analyzer.persistence.AnalysisTicketDao;
import yurykorzun.art.universe.music.data.semantic.analyzer.persistence.TicketRow;
import yurykorzun.art.universe.music.data.semantic.analyzer.prompt.PromptBuilder;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisVersions;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SemanticAnalyzer {

    private static final Logger log = LoggerFactory.getLogger(SemanticAnalyzer.class);

    private final AnalysisTicketDao ticketDao;
    private final LlmClient llmClient;
    private final PromptBuilder promptBuilder;

    public SemanticAnalyzer(AnalysisTicketDao ticketDao, LlmClient llmClient, PromptBuilder promptBuilder) {
        this.ticketDao = ticketDao;
        this.llmClient = llmClient;
        this.promptBuilder = promptBuilder;
    }

    public void processTicket(TicketRow ticket) {
        String analysisVersion = AnalysisVersions.CURRENT_UNIFIED;
        UUID requestId = UUID.randomUUID();

        try {
            // Mark as processing
            ticketDao.updateStatus(ticket.getId(), 2, analysisVersion, null);

            // Resolve expected types
            Set<ProposalType> expectedTypes = resolveExpectedTypes(ticket.getExpectedProposalTypes());
            Set<Integer> expectedEntityTypes = resolveEntityTypes(ticket.getExpectedEntityTypes());

            // Build prompt
            String subjectTypeName = resolveSubjectTypeName(ticket.getSubjectType());
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(
                subjectTypeName, ticket.getSubjectName(), ticket.getSubjectId(),
                ticket.getTextSamplesJson(), expectedTypes, expectedEntityTypes
            );

            // Compute input hash
            String inputHash = computeHash(userPrompt);

            // Call LLM
            LlmResponse response = llmClient.analyze(LlmRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .jsonMode(true)
                .build());

            if (response.isSuccess()) {
                // Save successful response
                ticketDao.saveRawResponse(
                    requestId, ticket.getId(), inputHash, analysisVersion,
                    ticket.getDataSource(), ticket.getSubjectType(), ticket.getSubjectId(),
                    response.getProvider(), response.getModel(),
                    response.getPromptTokens(), response.getCompletionTokens(),
                    response.getContent()
                );
                ticketDao.updateStatus(ticket.getId(), 3, null, null);
                log.info("Successfully analyzed ticket {}", ticket.getId());
            } else {
                ticketDao.saveFailedRequest(
                    requestId, ticket.getId(), inputHash, analysisVersion,
                    ticket.getDataSource(), ticket.getSubjectType(), ticket.getSubjectId(),
                    response.getErrorMessage()
                );
                ticketDao.updateStatus(ticket.getId(), 4, null, response.getErrorMessage());
                log.warn("LLM analysis failed for ticket {}: {}", ticket.getId(), response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("Error processing ticket {}", ticket.getId(), e);
            ticketDao.updateStatus(ticket.getId(), 4, null, e.getMessage());
        }
    }

    private Set<ProposalType> resolveExpectedTypes(int[] codes) {
        if (codes == null || codes.length == 0) return null;
        return Arrays.stream(codes)
            .mapToObj(code -> {
                for (ProposalType t : ProposalType.values()) {
                    if (t.getCode() == code) return t;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Set<Integer> resolveEntityTypes(int[] codes) {
        if (codes == null || codes.length == 0) return null;
        return Arrays.stream(codes).boxed().collect(Collectors.toSet());
    }

    private String resolveSubjectTypeName(int code) {
        return switch (code) {
            case 1 -> "ARTIST";
            case 2 -> "ALBUM";
            case 3 -> "TRACK";
            case 4 -> "CATEGORY";
            case 101 -> "PERSON";
            default -> "UNKNOWN";
        };
    }

    private String computeHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute hash", e);
        }
    }
}
