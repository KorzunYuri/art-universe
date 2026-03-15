package yurykorzun.art.universe.common.config.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.dto.BulkRegisterRequest;
import yurykorzun.art.universe.common.config.client.dto.BulkRegisterResponse;
import yurykorzun.art.universe.common.config.client.dto.PropertyResponse;
import yurykorzun.art.universe.common.config.client.dto.RegisterPropertyRequest;
import yurykorzun.art.universe.common.config.service.dto.UpdatePropertyRequest;
import yurykorzun.art.universe.common.config.service.entity.ConfigPropertyEntity;
import yurykorzun.art.universe.common.config.service.repository.ConfigPropertyRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigPropertyService {

    private final ConfigPropertyRepository repository;
    private final ConstraintValidator constraintValidator;

    /**
     * Idempotent bulk registration.
     * Creates each property with its default value if it doesn't already exist.
     * Always returns the current persisted value regardless.
     */
    @Transactional
    public BulkRegisterResponse registerAll(BulkRegisterRequest request) {
        List<RegisterPropertyRequest> incoming = request.getProperties();
        Set<String> incomingKeys = incoming.stream()
            .map(RegisterPropertyRequest::getKey)
            .collect(Collectors.toSet());

        // Load all existing keys in one query
        Map<String, ConfigPropertyEntity> existing = repository.findAllById(incomingKeys).stream()
            .collect(Collectors.toMap(ConfigPropertyEntity::getKey, e -> e));

        // Insert only the new ones
        List<ConfigPropertyEntity> toSave = incoming.stream()
            .filter(req -> !existing.containsKey(req.getKey()))
            .map(req -> {
                ConfigPropertyEntity entity = new ConfigPropertyEntity();
                entity.setKey(req.getKey());
                entity.setPropertyType(req.getPropertyType());
                entity.setCurrentValue(req.getDefaultValue());
                entity.setDefaultValue(req.getDefaultValue());
                entity.setDescription(req.getDescription());
                entity.setConstraintsJson(req.getConstraintsJson());
                return entity;
            })
            .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
            log.info("Registered {} new config properties", toSave.size());
            toSave.forEach(e -> existing.put(e.getKey(), e));
        }

        // Return current state for all requested keys
        List<PropertyResponse> responses = incomingKeys.stream()
            .filter(existing::containsKey)
            .map(key -> toResponse(existing.get(key)))
            .collect(Collectors.toList());

        return new BulkRegisterResponse(responses);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> findAll() {
        return repository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PropertyResponse findByKey(String key) {
        return repository.findById(key)
            .map(this::toResponse)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Config property '" + key + "' not found"
            ));
    }

    @Transactional
    public PropertyResponse updateValue(String key, UpdatePropertyRequest request) {
        ConfigPropertyEntity entity = repository.findById(key)
            .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                "Config property '" + key + "' not found"
            ));

        constraintValidator.validate(entity, request.getValue());
        entity.setCurrentValue(request.getValue());
        repository.save(entity);
        log.info("Config property '{}' updated to '{}'", key, request.getValue());
        return toResponse(entity);
    }

    private PropertyResponse toResponse(ConfigPropertyEntity entity) {
        PropertyResponse dto = new PropertyResponse();
        dto.setKey(entity.getKey());
        dto.setPropertyType(entity.getPropertyType());
        dto.setCurrentValue(entity.getCurrentValue());
        dto.setDefaultValue(entity.getDefaultValue());
        dto.setDescription(entity.getDescription());
        return dto;
    }
}
