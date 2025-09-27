package yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArtistSearchRequestDto(
    @NotBlank(message = "Search string cannot be blank")
    @Size(max = 255, message = "Search string cannot exceed 255 characters")
    String searchString
) {
}
