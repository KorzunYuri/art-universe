package yurykorzun.art.universe.music.data.master.dto.lookup;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupRequestDTO;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedBatchLookupRequestDTO extends BatchLookupRequestDTO<ArtistRelatedLookupRequestDTO> {
}
