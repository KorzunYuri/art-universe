package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.exception.MaintenanceException;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.MaintenanceOrchestrator;

@RestController
@RequestMapping("/api/v1/maintenance")
@Slf4j
public class MaintenanceController {

    private final MaintenanceOrchestrator maintenanceService;

    public MaintenanceController(MaintenanceOrchestrator maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping(value = "/trigger")
    @PreAuthorize("hasRole('MAINTAINER')")
    public String triggerDbMaintenance() {
        if (maintenanceService.requestMaintenance()) {
            return "maintenance requested";
        } else {
            log.warn("Maintenance already in progress");
            throw new MaintenanceException("Maintenance already in progress");
        }
    }
}
