package yurykorzun.art.universe.music.data.master.dto.binding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Extended DTO for binding entities that are related to an artist (tracks, albums) to existing entities.
 * {@code masterPrimaryArtistId} is required.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedEntityBindToExistingRequestDTO extends EntityBindToExistingRequestDTO {

    /** Master artist ID — used directly to set the entity's primary artist. */
    private Long masterPrimaryArtistId;
}
