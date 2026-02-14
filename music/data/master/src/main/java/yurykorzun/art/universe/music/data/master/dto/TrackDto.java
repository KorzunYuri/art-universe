package yurykorzun.art.universe.music.data.master.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.dto.BaseEntityDto;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TrackDto extends BaseEntityDto {
    private Long primaryArtistId;
}
