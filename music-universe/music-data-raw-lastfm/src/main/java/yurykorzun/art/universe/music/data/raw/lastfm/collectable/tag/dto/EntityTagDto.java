package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

/**
 * DTO for tags associated with a specific entity.
 * Contains minimal information needed for entity-tag relationships.
 */
public record EntityTagDto(
    long id,
    String name
) {

    public static EntityTagDto from(LastfmTag tag) {
        return new EntityTagDto(
            tag.getId(),
            tag.getName()
        );
    }
}
