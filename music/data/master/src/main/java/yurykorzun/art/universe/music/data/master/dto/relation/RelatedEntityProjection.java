package yurykorzun.art.universe.music.data.master.dto.relation;

/**
 * Spring Data projection interface for related entity queries.
 * Column aliases in native @Query methods must match these getter names (camelCase).
 */
public interface RelatedEntityProjection {
    Long getId();
    String getName();
    Long getRelationId();
    Long getRelationTypeId();
    String getRelationTypeName();
    Integer getTrackOrder();
}
