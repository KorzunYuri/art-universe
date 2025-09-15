package yurykorzun.art.universe.common.dto.lookup;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.dto.BaseEntityDto;

@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LookupResultDTO extends BaseEntityDto {
    public LookupResultDTO(Long id, String name) {
        super(id, name);
    }
}
