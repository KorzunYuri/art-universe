package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for binding an external album (with its tracklist) to master entities.
 *
 * <p>Album resolution:</p>
 * <ul>
 *   <li>If {@code masterAlbumId} is provided, the album is already bound — skip creation.</li>
 *   <li>Otherwise, create a new master album using {@code albumName} and bind it.</li>
 * </ul>
 *
 * <p>For each track item:</p>
 * <ul>
 *   <li><b>Bound</b> ({@code masterTrackId} present): verifies/creates the external↔master
 *       track binding, then creates the album-track relation.</li>
 *   <li><b>Unbound</b> ({@code masterTrackId} absent): creates a new master track, binds the
 *       external track to it, creates the ARTIST→TRACK internal relation, then creates the
 *       album-track relation.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAlbumWithTracksCreateAndBindRequestDTO {

    /** Name for the new master album. Required when {@code masterAlbumId} is absent. */
    private String albumName;

    /** Master artist ID — required, used directly to set the album's and tracks' primary artist. */
    private Long masterPrimaryArtistId;

    /** Existing master album ID. When provided, album creation is skipped. */
    private Long masterAlbumId;

    @NotEmpty(message = "Tracks list must not be empty")
    @Valid
    private List<ExternalAlbumTrackItemDTO> tracks;
}
