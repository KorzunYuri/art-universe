package yurykorzun.art.universe.common.domain.dto.lookup;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.dto.BaseEntityDto;

@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LookupResultDTO extends BaseEntityDto {
    public LookupResultDTO(Long id, String name) {
        super(id, name);
    }
}
