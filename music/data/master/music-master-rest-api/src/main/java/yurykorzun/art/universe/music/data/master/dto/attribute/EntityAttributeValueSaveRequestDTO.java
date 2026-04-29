package yurykorzun.art.universe.music.data.master.dto.attribute;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeSourceType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityAttributeValueSaveRequestDTO {

    @NotNull(message = "Attribute definition ID is required")
    private Long attributeDefId;

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
}
