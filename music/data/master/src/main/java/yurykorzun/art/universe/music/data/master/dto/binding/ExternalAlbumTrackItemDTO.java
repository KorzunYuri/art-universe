package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single track item in an external album creation+binding request.
 *
 * <p>Bound vs unbound is determined by the presence of {@code masterTrackId}:</p>
 * <ul>
 *   <li><b>Bound</b> ({@code masterTrackId != null}): the external track is already (or will be)
 *       bound to an existing master track. Only {@code externalTrackId}, {@code masterTrackId}
 *       and {@code trackOrder} are required.</li>
 *   <li><b>Unbound</b> ({@code masterTrackId == null}): a new master track will be created and the
 *       external track will be bound to it. {@code trackName} and {@code trackOrder} are required;
 *       the album's primary artist is used.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAlbumTrackItemDTO {

    @NotNull(message = "External track ID is required")
    private Long externalTrackId;

    private Long masterTrackId;

    /**
     * Name of the new master track to create. Required only for unbound tracks
     * (i.e. when {@code masterTrackId} is null).
     */
    private String trackName;

    /**
     * Optional per-track artist override.
     * When present, this artist is used instead of the album-level {@code masterPrimaryArtistId}
     * for creating new master tracks.
     */
    private Long masterPrimaryArtistId;

    @NotNull(message = "Track order is required")
    private Integer trackOrder;

    /** Optional relation type override. Falls back to the default ALBUM→TRACK type when absent. */
    private Long relationTypeId;
}
