package yurykorzun.art.universe.art.data.models.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonSaveRequestDto {

    private Long id;

    @NotBlank(message = "Person name is required")
    private String name;
}
