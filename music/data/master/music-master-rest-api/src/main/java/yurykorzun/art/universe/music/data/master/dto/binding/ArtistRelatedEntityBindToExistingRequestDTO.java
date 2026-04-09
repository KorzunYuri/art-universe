package yurykorzun.art.universe.music.data.master.dto.binding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Extended DTO for binding entities that are related to an artist (tracks, albums) to existing entities.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedEntityBindToExistingRequestDTO extends EntityBindToExistingRequestDTO {

    /**
     * @deprecated No longer used by {@code bindToExisting} — the service resolves the primary artist
     * from the existing master entity. Retained for backward compatibility with older API clients.
     * For {@code createAndBind}, use {@link ArtistRelatedEntityCreateAndBindRequestDTO#getMasterPrimaryArtistId()} instead.
     */
    @Deprecated(since = "2026-03-17")
    private Long masterPrimaryArtistId;
}
