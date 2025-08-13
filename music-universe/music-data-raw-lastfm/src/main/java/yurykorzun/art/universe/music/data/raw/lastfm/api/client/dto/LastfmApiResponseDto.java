package yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.time.Instant;
import java.util.Map;

public record LastfmApiResponseDto(
    // API Response fields
    Long id,
    ApiResponseStatus status,
    Instant createdAt,
    Instant updatedAt,
    
    // API Call fields
    Long apiCallId,
    LastfmApiCallType apiCallType,
    String apiCallMethod,
    ApiCallStatus apiCallStatus,
    Map<String, String> apiCallParams,
    Instant apiCallDueDttm,
    Instant apiCallCreatedAt,
    Instant apiCallUpdatedAt,
    
    // API Call scope fields
    Long dataSnapshotId,
    LastfmEntityType entityType,
    Long entityId
) {
    
    public static LastfmApiResponseDto from(LastfmApiResponse response) {
        LastfmApiCall apiCall = response.getApiCall();
        
        return new LastfmApiResponseDto(
            // API Response fields
            response.getId(),
            response.getStatus(),
            response.getCreatedAt(),
            response.getUpdatedAt(),
            
            // API Call fields
            apiCall.getId(),
            apiCall.getType(),
            apiCall.getType().getMethod(),
            apiCall.getStatus(),
            apiCall.getParams(),
            apiCall.getDueDttm(),
            apiCall.getCreatedAt(),
            apiCall.getUpdatedAt(),
            
            // API Call scope fields
            apiCall.getDataSnapshotId(),
            apiCall.getEntityType(),
            apiCall.getEntityId()
        );
    }
}
