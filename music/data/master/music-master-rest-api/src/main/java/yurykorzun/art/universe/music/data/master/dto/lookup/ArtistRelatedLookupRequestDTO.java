package yurykorzun.art.universe.music.data.master.dto.lookup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArtistRelatedLookupRequestDTO extends LookupRequestDTO {

    private Long masterArtistId;
    private DataSource dataSource;
    private Long externalArtistId;
}
