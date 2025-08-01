package yurykorzun.art.universe.music.data.master.dto.lookup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedBatchLookupRequestDTO extends BatchLookupRequestDTO<ArtistRelatedLookupRequestDTO> {
}
