package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;

import java.util.HashMap;
import java.util.Map;

public abstract class LastfmTagApiCallGenerator extends EntityScopedApiCallGenerator<LastfmTag> {

    protected LastfmTagApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.TAG;
    }

    @Override
    protected LastfmApiCallEntityQueryConfig getUnprocessedEntitiesQueryConfig() {
        return LastfmApiCallEntityQueryConfig.builder()
                .sort(Sort.by(Sort.Direction.DESC, "usageCount"))
            .build();
    }

    @Override
    protected boolean isValidForApiCall(LastfmTag entity) {
        return StringUtils.isNotBlank(entity.getName());
    }

    @Nullable
    @Override
    protected String getApiCallUniqueKey(LastfmTag entity) {
        return entity.getName();
    }

    @Override
    protected boolean hasHigherPriority(LastfmTag candidate, @Nullable LastfmTag existing) {
        if (existing == null) {
            return true;
        }

        // Compare approval statuses
        int existingStatusPriority = getApprovalStatusPriority(existing.getApprovalStatus());
        int candidateStatusPriority = getApprovalStatusPriority(candidate.getApprovalStatus());
        
        if (candidateStatusPriority > existingStatusPriority) {
            return true;
        } else if (candidateStatusPriority < existingStatusPriority) {
            return false;
        }
        
        // Compare tag usage users count
        if (isHigherValue(candidate.getUsageUsersCount(), existing.getUsageUsersCount())) {
            return true;
        } else if (isHigherValue(existing.getUsageUsersCount(), candidate.getUsageUsersCount())) {
            return false;
        }
        
        // Compare tag usage count
        if (isHigherValue(candidate.getUsageCount(), existing.getUsageCount())) {
            return true;
        } else if (isHigherValue(existing.getUsageCount(), candidate.getUsageCount())) {
            return false;
        }
        
        // If everything is equal - choose the older one
        return candidate.getId() < existing.getId();
    }

    @Override
    protected Map<String, String> getCommonApiCallParameters(LastfmTag tag) {
        Map<String, String> params = new HashMap<>();

        params.put(LastfmApiConstants.PARAM_NAME_TAG, tag.getName());

        return params;
    }
}
