package yurykorzun.art.universe.music.data.master.dto.binding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Extended DTO for creating and binding entities that are related to an artist (tracks, albums).
 * {@code masterPrimaryArtistId} is required.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedEntityCreateAndBindRequestDTO extends EntityCreateAndBindRequestDTO {

    /** Master artist ID — used directly to set the entity's primary artist. */
    private Long masterPrimaryArtistId;
}
