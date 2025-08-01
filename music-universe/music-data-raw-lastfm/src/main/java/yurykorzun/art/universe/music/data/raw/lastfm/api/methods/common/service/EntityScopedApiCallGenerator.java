package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>Represents common flow of generating API calls of some type for specific entity.
 * Generated API calls are entity-scoped, i.e. a call will be created for every entity. Despite that,
 * data snapshots don't have to be created on per-entity basis.
 * E.g. for artist.getInfo it's meaningless to create a data snapshot per entity</p>
 *
 * @param <SE> scope entity type
 */
@Slf4j
public abstract class EntityScopedApiCallGenerator<SE extends BaseLastfmEntity> extends LastfmApiCallGenerator {

    protected final LastfmApiCallService apiCallService;
    protected final LastfmDataSnapshotService snapshotService;
    protected final LastfmEntityService entityService;

    protected EntityScopedApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        this.apiCallService = lastfmApiCallService;
        this.snapshotService = snapshotService;
        this.entityService = entityService;
    }

    protected abstract LastfmEntityType getScopeEntityType();

    protected LastfmEntityQueryConfig getUnprocessedEntitiesQueryConfig() {
        return LastfmEntityQueryConfig.builder().build();
    }

    /**
     * Scope of api call implies data snapshots number.
     * If api call is entity scope, we will get as many data snapshots as there are entities.
     * Otherwise, it will be a one snapshot per period per api call type.
     *
     * @return true by default, as some of the methods produce 'scoped' attributes, in example, rank of tag within artist.
     */
    protected boolean isApiCallEntityScoped() {
        return getApiCallType().getScopeEntityType() != null;
    };

    /**
     * Return number of days created api call will stay actual
     */
    protected abstract int getDueDurationDays();

    @Override
    @Transactional
    public void createApiCalls() {
        // create api calls
        List<LastfmApiCallCreateRequest> creationRequests = generateApiCallCreationRequests();
        apiCallService.createApiCalls(creationRequests);
        log.info("created {} API calls for method {}", creationRequests.size(), getApiCallType().getMethod());

        // update 'created' counter in corresponding snapshots
        Map<Long, List<LastfmApiCallCreateRequest>> snapshotGroups = creationRequests.stream()
            .collect(Collectors.groupingBy(LastfmApiCallCreateRequest::getDataSnapshotId));
        if (snapshotGroups.size() == creationRequests.size()) {
            // each snapshot corresponds to a single api call so we can increment all by 1
            snapshotService.incCreatedCount(snapshotGroups.keySet().stream().toList());
        } else {
            snapshotGroups.forEach((k, v) -> snapshotService.incCreatedCountByNumber(k, v.size()));
        }
    }

    /**
     * Template method that sets the order of operations when generating api calls
     * @return list of requests for API calls creation
     */
    protected List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {

        // get entities to create api calls for
        List<SE> unprocessed = selectEntitiesForApiCalls();
        log.info("Found {} entities for method {}", unprocessed.size(), getApiCallType().getMethod());

        // filter out those we cannot build the request for
        List<SE> validForApiCalls = unprocessed.stream()
            .filter(this::isValidForApiCall)
            .toList();
        if (validForApiCalls.size() != unprocessed.size()) {
            log.warn("Filtered out {} invalid entities for method {}", unprocessed.size() - validForApiCalls.size(), getApiCallType().getMethod());
        }

        // filter out duplicates
        List<SE> dedupedEntities = deduplicateEntitiesForApiCalls(validForApiCalls);
        if (dedupedEntities.size() != validForApiCalls.size()) {
            log.warn("Filtered out {} duplicated entities for method {}", validForApiCalls.size() - dedupedEntities.size(), getApiCallType().getMethod());
        }

        // pre-generate type-scoped snapshot to reduce number of calls to snapshot service
        final LastfmDataSnapshot typeLevelSnapshot = isApiCallEntityScoped() ? null : getOrCreateSnapshot();

        // generate API call creation requests
        return dedupedEntities.stream()
            .flatMap(entity -> {
                LastfmDataSnapshot snapshot = isApiCallEntityScoped() ? getOrCreateSnapshotForEntity(entity) : typeLevelSnapshot;
                return this.generateApiCallCreationRequests(entity, snapshot).stream();
            })
            .toList();
    }

    /**
     * <p>Returns entities for API calls generation, following the most common logic -
     * return those not having API calls of corresponding type, ordered by popularity.</p>
     * <p>For most advanced retrieval/ordering/limiting logic subclasses should override this method.</p>
     */
    protected List<SE> selectEntitiesForApiCalls() {
        return entityService.findAllUnprocessed(getScopeEntityType(), getApiCallType(), getUnprocessedEntitiesQueryConfig());
    }

    /**
     * Decides whether we should proceed with the scope entity.
     * Should be aligned with {@link EntityScopedApiCallGenerator#getApiCallParameters(SE)}
     * Should log the reason why the entity is invalid for API call creation.
     * @param se scope entity
     * @return true, if the entity is valid for API call creation
     */
    protected abstract boolean isValidForApiCall(SE se);

    /**
     * Deduplicates the entities to avoid requests with the same parameters
     */
    protected List<SE> deduplicateEntitiesForApiCalls(List<SE> entities) {
        Map<String, SE> uniqueEntities = new HashMap<>();

        for (SE entity : entities) {
            String key = getApiCallUniqueKey(entity);
            if (key == null) {
                // skip invalid entity (however, they should be filtered out earlier)
                continue;
            }

            SE existing = uniqueEntities.get(key);
            if (existing != null && !hasHigherPriority(entity, existing)) {
                continue;
            }
            uniqueEntities.put(key, entity);
        }

        return new ArrayList<>(uniqueEntities.values());
    }

    /**
     * Helper method for deduplication. Determines whether the entity has higher priority for an api call
     */
    protected abstract boolean hasHigherPriority(SE candidate, @Nullable SE existing);

    /**
     * Returns key for entities deduplication, or null, if entity is not eligible for api call
     */
    protected abstract @Nullable String getApiCallUniqueKey(SE entity);

    /**
     * Helper method to set approval status priority during deduplication
     */
    protected final int getApprovalStatusPriority(ApprovalStatus status) {
        return switch (status) {
            case APPROVED -> 3;
            case PRE_APPROVED -> 2;
            case PENDING -> 1;
            case DECLINED -> 0;
        };
    }
    
    /**
     * Helper method to compare numeric values with null handling
     * @return true if first value is higher than second, false otherwise
     */
    protected final <T extends Comparable<T>> boolean isHigherValue(T value1, T value2) {
        if (value1 == null && value2 == null) return false;
        if (value1 == null) return false;
        if (value2 == null) return true;
        return value1.compareTo(value2) > 0;
    }

    /**
     * Returns a list with a single api call. If for a single entity several api calls have to be created,
     * this method should be overridden along with the downstream logic.
     */
    protected List<LastfmApiCallCreateRequest> generateApiCallCreationRequests(SE entity, LastfmDataSnapshot dataSnapshot) {
        List<LastfmAttributeSnapshot> attributeSnapshots = getOrCreateAttributeSnapshots(entity, dataSnapshot);
        if (!attributeSnapshots.isEmpty()) {
            log.info("created {} attribute snapshots for API method {}", attributeSnapshots.size(), getApiCallType().getMethod());
        }
        return List.of(buildApiCallCreationRequest(entity, dataSnapshot));
    }

    /**
     * If API call type produces 'snapshot' attributes, this method should be overridden to create them on this stage.
     */
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(SE entity, LastfmDataSnapshot dataSnapshot) {
        return List.of();
    }

    /**
     * Returns non-scoped data snapshot. If none existed, creates one.
     */
    protected LastfmDataSnapshot getOrCreateSnapshotForEntity(SE entity) {
        return snapshotService.getOrCreateSnapshotFor(getApiCallType(), entity);
    }

    /**
     * Returns scoped data snapshot. If none existed, creates one.
     */
    protected LastfmDataSnapshot getOrCreateSnapshot() {
        return snapshotService.getOrCreateSnapshotFor(getApiCallType());
    }

    /**
     * <p>Builds a DTO for API call creation.</p>
     *
     * @param entity If provided, makes the call entity-scoped. If not, call will be type-scoped, i.e. not bound to any specific entity
     */
    protected LastfmApiCallCreateRequest buildApiCallCreationRequest(
        SE entity,
        LastfmDataSnapshot dataSnapshot
    ) {
        return LastfmApiCallCreateRequest.builder()
                .type(getApiCallType())
                .dataSnapshotId(dataSnapshot.getId())
                .dueDttm(TimeUtil.calcDueDttm(getDueDurationDays()))
                .entityType(entity.getType())
                .entityId(entity.getId())
                .params(getApiCallParameters(entity))
            .build();
    }

    /**
     * Build api call parameters map for scoped request by applying specific method parameters on top of common parameters.
     */
    protected Map<String, String> getApiCallParameters(SE entity) {
        return applyCustomApiCallParameters(getCommonApiCallParameters(entity));
    }

    protected abstract Map<String, String> getCommonApiCallParameters(SE entity);

    protected Map<String, String> applyCustomApiCallParameters(Map<String, String> params) {
        return params;
    }

}
