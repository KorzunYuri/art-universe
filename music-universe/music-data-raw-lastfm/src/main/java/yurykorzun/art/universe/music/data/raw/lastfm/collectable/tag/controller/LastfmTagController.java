package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.dto.ApprovalStatusRequestDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.EntityTagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto.TagSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.DataFetchException;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.DataUpdateException;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.common.exception.ValidationException;

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
    public Page<LastfmTagResponseDto> getTags(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            TagSearchParams params = new TagSearchParams(search, approvalStatuses);
            return tagService.findAll(params, pageable);
        } catch (Exception e) {
            log.error("Failed to fetch tags: {}", e.getMessage(), e);
            throw new DataFetchException("Failed to fetch tags: service error occurred", e);
        }
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmTagResponseDto getTagById(@PathVariable Long id) {
        try {
            return tagService.findById(id)
                .map(LastfmTagResponseDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch tag with id {}: {}", id, e.getMessage(), e);
            throw new DataFetchException("Failed to fetch tag: service error occurred", e);
        }
    }

    @PatchMapping("/{id}/approval")
    public LastfmTagResponseDto updateApprovalStatus(
        @PathVariable Long id,
        @RequestBody ApprovalStatusRequestDto request
    ) {
        try {
            return tagService.updateApprovalStatus(id, request.approvalStatus());
        } catch (Exception e) {
            log.error("Failed to update approval status: {}", e.getMessage(), e);
            throw new DataUpdateException("Failed to update approval status: service error occurred", e);
        }
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public List<EntityTagDto> getEntityTags(
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
            return tagService.findAllByEntity(parsedEntityType, entityId, searchParams, pageable);
        } catch (IllegalArgumentException e) {
            log.info("Invalid entity type: {}", e.getMessage());
            throw new ValidationException("Invalid entity type: " + e.getMessage());
        } catch (Exception e) {
            log.error("Failed to fetch entity tags: {}", e.getMessage(), e);
            throw new DataFetchException("Failed to fetch entity tags: service error occurred", e);
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
