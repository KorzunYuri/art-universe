package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisTicket;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.AnalysisTicketRepository;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisTicketStatus;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisVersions;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing analysis ticket lifecycle.
 * Encapsulates polling and status transitions for tickets.
 */
@Service
public class AnalysisTicketService {

    private final AnalysisTicketRepository ticketRepository;

    public AnalysisTicketService(AnalysisTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Atomically polls pending tickets and marks them as PROCESSING.
     * The analysis version for each ticket is resolved from its analysis mode.
     * Runs in a single short transaction so row locks are released quickly.
     */
    @Transactional
    public List<AnalysisTicket> pollAndMarkProcessing(int batchSize) {
        List<AnalysisTicket> tickets = ticketRepository.pollPendingTickets(batchSize);
        Instant now = Instant.now();
        for (AnalysisTicket ticket : tickets) {
            ticket.setStatus(AnalysisTicketStatus.PROCESSING);
            ticket.setAnalysisVersion(AnalysisVersions.currentVersionFor(ticket.getAnalysisMode()));
            ticket.setPickedUpAt(now);
        }
        return tickets;
    }

    @Transactional
    public void markCompleted(AnalysisTicket ticket) {
        AnalysisTicket managed = ticketRepository.getReferenceById(ticket.getId());
        managed.setStatus(AnalysisTicketStatus.COMPLETED);
        managed.setCompletedAt(Instant.now());
    }

    @Transactional
    public void markFailed(AnalysisTicket ticket, String errorMessage) {
        AnalysisTicket managed = ticketRepository.getReferenceById(ticket.getId());
        managed.setStatus(AnalysisTicketStatus.FAILED);
        managed.setErrorMessage(errorMessage);
        managed.setCompletedAt(Instant.now());
    }
}
