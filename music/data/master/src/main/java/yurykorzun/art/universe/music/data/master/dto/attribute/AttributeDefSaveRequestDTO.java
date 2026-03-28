package yurykorzun.art.universe.music.data.master.dto.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class AttributeDefSaveRequestDTO {

    @NotBlank(message = "Attribute code is required")
    private String code;

    @NotBlank(message = "Attribute name is required")
    private String name;

    private String description;

    @NotNull(message = "Data type is required")
    private AttributeDataType dataType;

    @NotNull(message = "Temporal type is required")
    private AttributeTemporalType temporalType;

    private AttributeComputationType computationType;

    private String dataSource;

    private String formula;

    private boolean multiValue;

    @NotEmpty(message = "At least one applicable target type is required")
    private List<AttributeTargetType> applicableTo;
}
