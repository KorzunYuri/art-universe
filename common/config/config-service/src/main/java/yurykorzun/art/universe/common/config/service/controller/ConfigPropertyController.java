package yurykorzun.art.universe.common.config.service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yurykorzun.art.universe.common.config.client.dto.BulkRegisterRequest;
import yurykorzun.art.universe.common.config.client.dto.BulkRegisterResponse;
import yurykorzun.art.universe.common.config.client.dto.PropertyResponse;
import yurykorzun.art.universe.common.config.service.dto.UpdatePropertyRequest;
import yurykorzun.art.universe.common.config.service.service.ConfigPropertyService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/config/properties")
@RequiredArgsConstructor
public class ConfigPropertyController {

    private final ConfigPropertyService service;

    /**
     * Idempotent bulk registration.
     * Creates each property with its default value if it does not already exist.
     * Always returns the current persisted value.
     */
    @PostMapping("/register")
    public ResponseEntity<BulkRegisterResponse> register(@RequestBody BulkRegisterRequest request) {
        return ResponseEntity.ok(service.registerAll(request));
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{key}")
    public ResponseEntity<PropertyResponse> getOne(@PathVariable String key) {
        return ResponseEntity.ok(service.findByKey(key));
    }

    /**
     * Updates the current value of a property.
     * Validates the new value against the property's type and constraints.
     */
    @PutMapping("/{key}")
    @PreAuthorize("hasRole('CONFIG_MANAGER')")
    public ResponseEntity<PropertyResponse> update(@PathVariable String key,
                                                    @Valid @RequestBody UpdatePropertyRequest request) {
        return ResponseEntity.ok(service.updateValue(key, request));
    }
}
