package yurykorzun.art.universe.music.data.semantic.analyzer.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.music.data.semantic.analyzer.config.LlmClientRegistry;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.ReprocessingRequestDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.ReprocessingResultDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.dto.TicketStatsDto;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.AnalysisStatusService;
import yurykorzun.art.universe.music.data.semantic.analyzer.service.ReprocessingService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisStatusController {

    private static final int DEFAULT_BATCH_SIZE = 500;

    private final AnalysisStatusService statusService;
    private final ReprocessingService reprocessingService;
    private final LlmClientRegistry clientRegistry;

    public AnalysisStatusController(
        AnalysisStatusService statusService,
        ReprocessingService reprocessingService,
        LlmClientRegistry clientRegistry
    ) {
        this.statusService = statusService;
        this.reprocessingService = reprocessingService;
        this.clientRegistry = clientRegistry;
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

    @GetMapping("/clients")
    public Map<String, LlmClientRegistry.ClientStatusInfo> getClientStatuses() {
        return clientRegistry.getClientStatuses();
    }

    @PostMapping("/clients/{name}/disable")
    public Map<String, LlmClientRegistry.ClientStatusInfo> disableClient(
        @PathVariable String name,
        @RequestParam(defaultValue = "Manually disabled") String reason
    ) {
        clientRegistry.forceDisable(name, reason);
        return clientRegistry.getClientStatuses();
    }

    @PostMapping("/clients/{name}/enable")
    public Map<String, LlmClientRegistry.ClientStatusInfo> enableClient(@PathVariable String name) {
        clientRegistry.forceEnable(name);
        return clientRegistry.getClientStatuses();
    }
}
