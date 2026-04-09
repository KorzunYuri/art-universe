package yurykorzun.art.universe.music.data.master.dto.attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeDataType;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeSourceType;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTemporalType;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityAttributeValueDto {

    private Long id;
    private AttributeTargetType targetType;
    private Long targetId;
    private Long attributeDefId;
    private String attributeCode;
    private String attributeName;
    private AttributeDataType attributeDataType;
    private AttributeTemporalType attributeTemporalType;
    private Long numericValue;
    private String stringValue;
    private LocalDate dateValue;
    private Boolean boolValue;
    private LocalDate eventDate;
    private LocalDate validFrom;
    private LocalDate validTill;
    private AttributeSourceType sourceType;
    private String sourceRef;
    private Short confidence;
    private Instant createdAt;
    private Instant updatedAt;
}
