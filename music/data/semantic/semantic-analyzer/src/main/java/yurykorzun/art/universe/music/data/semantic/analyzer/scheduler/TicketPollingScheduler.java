package yurykorzun.art.universe.music.data.semantic.analyzer.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.semantic.analyzer.config.SemanticAnalyzerProperty;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisTicket;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.AnalysisTicketService;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.SemanticAnalyzer;

import java.util.List;

/**
 * Polls pending tickets and hands them off to {@link SemanticAnalyzer}.
 *
 * The poll-and-mark step runs in a short transaction inside
 * {@link AnalysisTicketService#pollAndMarkProcessing} so that
 * {@code FOR UPDATE SKIP LOCKED} row locks are released promptly.
 * Each ticket is then processed outside that transaction — the analyzer
 * opens its own per-ticket transactions for persisting results.
 */
@Component
public class TicketPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(TicketPollingScheduler.class);

    private final AnalysisTicketService ticketService;
    private final SemanticAnalyzer analyzer;
    private final ConfigPropertyHolder configPropertyHolder;

    public TicketPollingScheduler(
        AnalysisTicketService ticketService,
        SemanticAnalyzer analyzer,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.ticketService = ticketService;
        this.analyzer = analyzer;
        this.configPropertyHolder = configPropertyHolder;
    }

    public void pollAndProcess() {
        int batchSize = configPropertyHolder.getInt(SemanticAnalyzerProperty.BATCH_SIZE);
        List<AnalysisTicket> tickets = ticketService.pollAndMarkProcessing(batchSize);
        if (tickets.isEmpty()) {
            return;
        }
        log.info("Polled {} pending tickets", tickets.size());

        for (AnalysisTicket ticket : tickets) {
            try {
                analyzer.processTicket(ticket);
            } catch (Exception e) {
                log.error("Unhandled error processing ticket {}", ticket.getId(), e);
            }
        }
    }
}
