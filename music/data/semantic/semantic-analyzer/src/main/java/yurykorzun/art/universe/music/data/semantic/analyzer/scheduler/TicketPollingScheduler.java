package yurykorzun.art.universe.music.data.semantic.analyzer.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.semantic.analyzer.analysis.SemanticAnalyzer;
import yurykorzun.art.universe.music.data.semantic.analyzer.config.SemanticAnalyzerProperty;
import yurykorzun.art.universe.music.data.semantic.analyzer.persistence.AnalysisTicketDao;
import yurykorzun.art.universe.music.data.semantic.analyzer.persistence.TicketRow;

import java.util.List;

@Component
public class TicketPollingScheduler {

    private static final Logger log = LoggerFactory.getLogger(TicketPollingScheduler.class);

    private final AnalysisTicketDao ticketDao;
    private final SemanticAnalyzer analyzer;
    private final ConfigPropertyHolder configPropertyHolder;

    public TicketPollingScheduler(
        AnalysisTicketDao ticketDao,
        SemanticAnalyzer analyzer,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.ticketDao = ticketDao;
        this.analyzer = analyzer;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Transactional
    public void pollAndProcess() {
        int batchSize = configPropertyHolder.getInt(SemanticAnalyzerProperty.BATCH_SIZE);
        List<TicketRow> tickets = ticketDao.pollPendingTickets(batchSize);

        if (!tickets.isEmpty()) {
            log.info("Polled {} pending tickets", tickets.size());
            for (TicketRow ticket : tickets) {
                analyzer.processTicket(ticket);
            }
        }
    }
}
