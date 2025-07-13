package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/tags")
@Slf4j
public class LastfmTagController {

    private final LastfmTagService tagService;

    public LastfmTagController(LastfmTagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseWrapper<Page<LastfmTagResponseDto>>> getTags(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            TagSearchParams params = new TagSearchParams(search, approvalStatuses);
            Page<LastfmTagResponseDto> page = tagService.findAll(params, pageable);
            return ResponseWrapper.success(page);
        } catch (Exception e) {
            log.error("Failed to fetch tags: {}", e.getMessage(), e);
            return ResponseWrapper.failure("Failed to fetch tags: service error occurred");
        }
    }

    @PatchMapping("/{id}/approval")
    public ResponseEntity<ResponseWrapper<LastfmTagResponseDto>> updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        try {
            LastfmTagResponseDto tag = tagService.updateApprovalStatus(id, request.approvalStatus());
            return ResponseWrapper.success(tag);
        } catch (Exception e) {
            log.error("Failed to update approval status: {}", e.getMessage(), e);
            return ResponseWrapper.failure("Failed to update approval status: service error occurred");
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ResponseWrapper<List<EntityTagDto>>> getEntityTags(
        @PathVariable String entityType,
        @PathVariable Long entityId,
        @RequestParam(required = false) Integer minUsageCount,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 100, sort = "name") Pageable pageable
    ) {
        try {
            // Parse entity type from string
            LastfmEntityType parsedEntityType = parseEntityType(entityType);
            
            // Create search params
            EntityTagSearchParams searchParams = new EntityTagSearchParams(minUsageCount, approvalStatuses);
            
            // Get tags with pagination and filtering
            List<EntityTagDto> tags = tagService.findAllByEntity(parsedEntityType, entityId, searchParams, pageable);
            return ResponseWrapper.success(tags);
        } catch (IllegalArgumentException e) {
            log.error("Invalid entity type: {}", e.getMessage(), e);
            return ResponseWrapper.failure("Invalid entity type: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to fetch entity tags: {}", e.getMessage(), e);
            return ResponseWrapper.failure("Failed to fetch entity tags: service error occurred");
        }
    }

    private LastfmEntityType parseEntityType(String entityTypeStr) {
        // Try to parse as string (name)
        try {
            return LastfmEntityType.valueOf(entityTypeStr.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown entity type: " + entityTypeStr +
                ". Expected one of: ARTIST, ALBUM, TRACK, TAG or their numeric codes (1, 2, 3, 4)");
        }
    }
}
