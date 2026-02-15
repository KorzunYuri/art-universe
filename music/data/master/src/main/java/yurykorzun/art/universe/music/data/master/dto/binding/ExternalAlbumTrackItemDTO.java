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
 *       {@code primaryArtistId} defaults to the album's primary artist when absent.</li>
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

    @NotNull(message = "Track order is required")
    private Integer trackOrder;

    /**
     * External artist ID for the track's primary artist. Only relevant for unbound tracks.
     * When absent the album's primary artist is used instead.
     */
    private Long primaryArtistId;

    /** Optional relation type override. Falls back to the default ALBUM→TRACK type when absent. */
    private Long relationTypeId;
}
