package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.MaintenanceException;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.DbMaintenanceService;

@RestController
@RequestMapping("maintenance")
@Slf4j
public class MaintenanceController {

    private final DbMaintenanceService maintenanceService;

    public MaintenanceController(DbMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("/trigger")
    public String triggerDbMaintenance() {
        if (maintenanceService.enqueueMaintenance()) {
            return "maintenance requested";
        } else {
            log.error("Failed to request maintenance");
            throw new MaintenanceException("Failed to request maintenance");
        }
    }
}
