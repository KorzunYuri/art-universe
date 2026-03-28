package yurykorzun.art.universe.music.data.master.dto.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeComputationType;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeDataType;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTemporalType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDefDto {

    private Long id;
    private String code;
    private String name;
    private String description;
    private AttributeDataType dataType;
    private AttributeTemporalType temporalType;
    private AttributeComputationType computationType;
    private String dataSource;
    private String formula;
    private boolean multiValue;
    private boolean system;
    private List<AttributeTargetType> applicableTo;
}
