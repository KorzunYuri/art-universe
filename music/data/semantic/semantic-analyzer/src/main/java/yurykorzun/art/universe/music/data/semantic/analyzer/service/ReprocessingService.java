package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.ReprocessingResultDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.entity.AnalysisTicket;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.AnalysisRequestRepository;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.AnalysisTicketRepository;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisTicketStatus;

import java.util.List;
import java.util.UUID;

/**
 * Triggers reprocessing of analyzed subjects when the analysis version advances.
 * For each subject that was processed under an older version and not yet under the
 * new one, pending proposals from the old run are superseded and a fresh ticket is
 * created (cloned from the source ticket) so the polling scheduler will pick it up.
 */
@Service
public class ReprocessingService {

    private static final Logger log = LoggerFactory.getLogger(ReprocessingService.class);

    private final AnalysisTicketRepository ticketRepository;
    private final AnalysisRequestRepository requestRepository;

    public ReprocessingService(
        AnalysisTicketRepository ticketRepository,
        AnalysisRequestRepository requestRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public ReprocessingResultDto triggerReprocessing(String fromVersion, String toVersion, int batchSize) {
        List<AnalysisTicket> sourceTickets =
            ticketRepository.findEligibleForReprocessing(fromVersion, toVersion, batchSize);

        if (sourceTickets.isEmpty()) {
            log.info("No entities eligible for reprocessing from {} to {}", fromVersion, toVersion);
            return new ReprocessingResultDto(fromVersion, toVersion, 0, 0);
        }

        int superseded = 0;
        int ticketsCreated = 0;
        for (AnalysisTicket source : sourceTickets) {
            superseded += requestRepository.supersedePendingProposals(
                fromVersion,
                source.getSubjectType().getCode(),
                source.getSubjectId()
            );

            AnalysisTicket fresh = AnalysisTicket.builder()
                .id(UUID.randomUUID())
                .dataSource(source.getDataSource())
                .subjectType(source.getSubjectType())
                .subjectId(source.getSubjectId())
                .subjectName(source.getSubjectName())
                .textSamplesJson(source.getTextSamplesJson())
                .expectedProposalTypes(source.getExpectedProposalTypes())
                .expectedEntityTypes(source.getExpectedEntityTypes())
                .analysisMode(source.getAnalysisMode())
                .status(AnalysisTicketStatus.PENDING)
                .build();
            ticketRepository.save(fresh);
            ticketsCreated++;
        }

        log.info(
            "Reprocessing {} -> {}: created {} tickets, superseded {} pending proposals",
            fromVersion, toVersion, ticketsCreated, superseded
        );
        return new ReprocessingResultDto(fromVersion, toVersion, ticketsCreated, superseded);
    }
}
