package yurykorzun.art.universe.music.data.semantic.parser.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.semantic.parser.config.SemanticParserProperty;
import yurykorzun.art.universe.music.data.semantic.parser.parser.LlmResponseParser;
import yurykorzun.art.universe.music.data.semantic.parser.parser.ParsedProposal;
import yurykorzun.art.universe.music.data.semantic.parser.persistence.AnalysisRequestDao;
import yurykorzun.art.universe.music.data.semantic.parser.writer.ProposalWriter;

import java.util.List;

@Component
public class ResponseParsingScheduler {

    private static final Logger log = LoggerFactory.getLogger(ResponseParsingScheduler.class);

    private final AnalysisRequestDao requestDao;
    private final LlmResponseParser parser;
    private final ProposalWriter writer;
    private final ConfigPropertyHolder configPropertyHolder;

    public ResponseParsingScheduler(
        AnalysisRequestDao requestDao,
        LlmResponseParser parser,
        ProposalWriter writer,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.requestDao = requestDao;
        this.parser = parser;
        this.writer = writer;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Transactional
    public void pollAndParse() {
        int batchSize = configPropertyHolder.getInt(SemanticParserProperty.BATCH_SIZE);
        List<AnalysisRequestDao.CompletedRequest> requests = requestDao.pollCompletedRequests(batchSize);

        if (!requests.isEmpty()) {
            log.info("Processing {} completed analysis requests", requests.size());
        }

        for (AnalysisRequestDao.CompletedRequest request : requests) {
            try {
                List<ParsedProposal> proposals = parser.parse(request.getRawResponse());
                writer.writeProposals(
                    request.getId(), request.getSubjectType(), request.getSubjectId(), proposals
                );
            } catch (Exception e) {
                log.error("Failed to parse response for request {}", request.getId(), e);
            }
        }
    }
}
