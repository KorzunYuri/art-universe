package yurykorzun.art.universe.music.data.semantic.analyzer.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisStatusController {

    private final JdbcTemplate jdbcTemplate;

    public AnalysisStatusController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
