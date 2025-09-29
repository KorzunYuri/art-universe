package yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto;

/**
 * DTO for tags associated with a specific entity.
 * Contains minimal information needed for entity-tag relationships.
 */
public record EntityTagDto(
    long id,
    String name,
    Integer relationApprovalStatus,
    Integer tagApprovalStatus,
    Integer entityApprovalStatus,
    Integer usageCount
) {
}
