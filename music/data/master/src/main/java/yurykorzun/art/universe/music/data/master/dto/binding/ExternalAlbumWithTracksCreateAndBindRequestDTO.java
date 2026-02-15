package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating a new master album from an external album together with its tracklist.
 *
 * <p>Flow:</p>
 * <ol>
 *   <li>Creates a new master album and binds the external album to it.</li>
 *   <li>For each track item:
 *     <ul>
 *       <li><b>Bound</b> ({@code masterTrackId} present): verifies/creates the external↔master
 *           track binding, then creates the album-track relation.</li>
 *       <li><b>Unbound</b> ({@code masterTrackId} absent): creates a new master track, binds the
 *           external track to it, creates the ARTIST→TRACK internal relation, then creates the
 *           album-track relation.</li>
 *     </ul>
 *   </li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAlbumWithTracksCreateAndBindRequestDTO {

    @NotBlank(message = "Album name is required")
    private String albumName;

    @NotNull(message = "Primary artist ID is required")
    private Long primaryArtistId;

    @NotEmpty(message = "Tracks list must not be empty")
    @Valid
    private List<ExternalAlbumTrackItemDTO> tracks;
}
