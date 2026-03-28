package yurykorzun.art.universe.music.data.semantic.analyzer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.semantic.analyzer.reprocessing.ReprocessingService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisStatusController {

    private final JdbcTemplate jdbcTemplate;
    private final ReprocessingService reprocessingService;

    public AnalysisStatusController(JdbcTemplate jdbcTemplate, ReprocessingService reprocessingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.reprocessingService = reprocessingService;
    }

    @GetMapping("/tickets/stats")
    public Map<String, Object> getTicketStats() {
        Map<String, Object> stats = new HashMap<>();
        jdbcTemplate.query(
            "SELECT status, COUNT(*) as cnt FROM mu_semantic_analysis.analysis_ticket GROUP BY status",
            rs -> {
                int status = rs.getInt("status");
                long count = rs.getLong("cnt");
                String statusName = switch (status) {
                    case 1 -> "pending";
                    case 2 -> "processing";
                    case 3 -> "completed";
                    case 4 -> "failed";
                    default -> "unknown_" + status;
                };
                stats.put(statusName, count);
            }
        );
        return stats;
    }

    @PostMapping("/reprocess")
    public Map<String, Object> triggerReprocessing(@RequestBody Map<String, String> request) {
        String fromVersion = request.get("from_version");
        String toVersion = request.get("to_version");
        int batchSize = Integer.parseInt(request.getOrDefault("batch_size", "500"));

        ReprocessingService.ReprocessingResult result = reprocessingService.triggerReprocessing(
            fromVersion, toVersion, batchSize
        );

        Map<String, Object> response = new HashMap<>();
        response.put("tickets_created", result.ticketsCreated());
        response.put("proposals_superseded", result.proposalsSuperseded());
        response.put("from_version", fromVersion);
        response.put("to_version", toVersion);
        return response;
    }
}
