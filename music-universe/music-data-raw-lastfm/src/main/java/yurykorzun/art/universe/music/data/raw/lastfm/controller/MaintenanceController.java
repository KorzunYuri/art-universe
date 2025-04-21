package yurykorzun.art.universe.music.data.raw.lastfm.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbMaintenanceService;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final DbMaintenanceService maintenanceService;

    public MaintenanceController(DbMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("trigger")
    public ResponseEntity<String> triggerDbMaintenance() {
        if (maintenanceService.enqueueMaintenance()) {
            return ResponseEntity.ok().body("maintenance requested");
        } else {
            return ResponseEntity.internalServerError().body("Failed to request maintenance");
        }
    }

}
