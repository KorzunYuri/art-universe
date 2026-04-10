package yurykorzun.art.universe.music.data.semantic.analyzer.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.TicketStatsDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.repository.AnalysisTicketRepository;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisTicketStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisStatusService {

    private final AnalysisTicketRepository ticketRepository;

    public AnalysisStatusService(AnalysisTicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public TicketStatsDto getTicketStats() {
        Map<AnalysisTicketStatus, Long> counts = new EnumMap<>(AnalysisTicketStatus.class);
        for (AnalysisTicketStatus status : AnalysisTicketStatus.values()) {
            counts.put(status, 0L);
        }
        List<Object[]> rows = ticketRepository.countByStatus();
        for (Object[] row : rows) {
            int statusCode = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            for (AnalysisTicketStatus status : AnalysisTicketStatus.values()) {
                if (status.getCode() == statusCode) {
                    counts.put(status, count);
                    break;
                }
            }
        }
        return new TicketStatsDto(
            counts.get(AnalysisTicketStatus.PENDING),
            counts.get(AnalysisTicketStatus.PROCESSING),
            counts.get(AnalysisTicketStatus.COMPLETED),
            counts.get(AnalysisTicketStatus.FAILED)
        );
    }
}
