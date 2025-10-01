package yurykorzun.art.universe.music.data.raw.lastfm.collectable.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup.LastfmTagLookupService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/tags")
public class LastfmTagController {

    private final LastfmTagService tagService;
    private final LastfmTagLookupService tagLookupService;

    public LastfmTagController(LastfmTagService tagService, LastfmTagLookupService tagLookupService) {
        this.tagService = tagService;
        this.tagLookupService = tagLookupService;
    }

    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Page<LastfmTagResponseDto> getTags(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @RequestParam(required = false) Integer minUsageCount,
        @RequestParam(required = false) Integer minUsageUsersCount,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        TagSearchParams params = new TagSearchParams(search, approvalStatuses, minUsageCount, minUsageUsersCount);
        return tagService.findAll(params, pageable);
    }

    @GetMapping(
        value = "/{id}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public LastfmTagResponseDto getTagById(@PathVariable Long id) {
        return tagService.findById(id);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public List<EntityTagDto> getEntityTags(
        @PathVariable String entityType,
        @PathVariable Long entityId,
        @RequestParam(required = false) Integer minUsageCount,
        @RequestParam(required = false) Set<Integer> approvalStatuses,
        @PageableDefault(size = 100, sort = "name") Pageable pageable
    ) {
        // Parse entity type from string
        LastfmEntityType parsedEntityType = parseEntityType(entityType);
        
        // Create search params
        EntityTagSearchParams searchParams = new EntityTagSearchParams(minUsageCount, approvalStatuses);
        
        // Get tags with pagination and filtering
        return tagService.findAllByEntity(parsedEntityType, entityId, searchParams, pageable);
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

    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupTags(
        @RequestParam String search,
        @RequestParam(required = false) Integer limit
    ) {
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();
        return tagLookupService.lookup(request);
    }
}
