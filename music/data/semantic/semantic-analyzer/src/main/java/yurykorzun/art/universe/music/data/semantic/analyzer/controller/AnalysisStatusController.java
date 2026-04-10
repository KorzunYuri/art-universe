package yurykorzun.art.universe.music.data.semantic.analyzer.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.ReprocessingRequestDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.ReprocessingResultDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.TicketStatsDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.AnalysisStatusService;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.ReprocessingService;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisStatusController {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final AnalysisStatusService statusService;
    private final ReprocessingService reprocessingService;

    public AnalysisStatusController(
        AnalysisStatusService statusService,
        ReprocessingService reprocessingService
    ) {
        this.statusService = statusService;
        this.reprocessingService = reprocessingService;
    }

    @GetMapping("/tickets/stats")
    public TicketStatsDto getTicketStats() {
        return statusService.getTicketStats();
    }

    @PostMapping("/reprocess")
    public ReprocessingResultDto triggerReprocessing(@Valid @RequestBody ReprocessingRequestDto request) {
        return reprocessingService.triggerReprocessing(
            request.fromVersion(),
            request.toVersion(),
            request.batchSizeOrDefault(DEFAULT_BATCH_SIZE)
        );
    }
}
